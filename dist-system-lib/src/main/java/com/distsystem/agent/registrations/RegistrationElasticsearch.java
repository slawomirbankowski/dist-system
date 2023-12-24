package com.distsystem.agent.registrations;

import com.distsystem.api.*;
import com.distsystem.api.dtos.*;
import com.distsystem.dao.DaoElasticsearchBase;
import com.distsystem.base.RegistrationBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/** connector to Elasticsearch as agent manager - central point with registering/unregistering agents
 * */
public class RegistrationElasticsearch extends RegistrationBase {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(RegistrationElasticsearch.class);

    private String elasticUrl;
    private String elasticUser;
    private String elasticPass;
    private String registerIndexName;
    private String serverIndexName;
    private String serviceIndexName;
    private String issueIndexName;
    /** DAO to Elasticsearch */
    private DaoElasticsearchBase elasticDao;

    public RegistrationElasticsearch(ServiceObjectParams params) {
        super(params);
    }


    /** count objects in this agentable object including this object */
    public long countObjectsAgentable() {
        return 8L;
    }
    /** run for initialization in classes */
    @Override
    protected void onInitialize() {
        elasticUrl = getConfigProperty(DistConfig.URL, "http://localhost:9200"); //parentAgent.getConfig().getProperty(DistConfig.AGENT_REGISTRATION_OBJECT_ELASTICSEARCH_URL, "");
        elasticUser = getConfigProperty(DistConfig.USER, ""); //parentAgent.getConfig().getProperty(DistConfig.AGENT_REGISTRATION_OBJECT_ELASTICSEARCH_USER, "");
        elasticPass = getConfigProperty(DistConfig.PASS, ""); //parentAgent.getConfig().getProperty(DistConfig.AGENT_REGISTRATION_OBJECT_ELASTICSEARCH_PASS, "");
        registerIndexName = "distagentregister";
        serverIndexName = "distagentserver";
        issueIndexName = "distagentissue";
        serviceIndexName = "distagentservice";
        // parentAgent.getConfig().getPropertyAsLong()
        log.info("Initializing of Elasticsearch registration with URL: " + elasticUrl + ", agent: " + parentAgent.getAgentGuid());
        elasticDao = parentAgent.getAgentDao().getOrCreateDaoOrError(DaoElasticsearchBase.class, DaoParams.elasticsearchParams(elasticUrl, elasticUser, elasticPass));
        elasticDao.usedByComponent(this);
        elasticDao.toRow();
        // check connection to Elasticsearch, if needed - create index with default name
        log.info("Connected to Elasticsearch, url: " + elasticUrl + ", indices: " + elasticDao.getIndices().size() + ", agent: " + parentAgent.getAgentGuid());
        elasticDao.createIndicesWithCheck(Set.of(registerIndexName, serverIndexName, issueIndexName, serviceIndexName));
    }
    /** read configuration and re-initialize this component */
    public boolean componentReinitialize() {
        // TODO: implement reinitialization
        return true;
    }

    @Override
    protected boolean onIsConnected() {
        return elasticDao.getClusterInfo().size() > 0;
    }
    /** */
    @Override
    protected AgentConfirmation onAgentRegister(AgentRegister register) {
        log.info("Registering Agent at Elasticsearch, URL: " + elasticUrl + ", index: " + registerIndexName);
        elasticDao.addOrUpdateDocument(registerIndexName, register.getAgentGuid(), register.toAgentRegisterRow().toMap());
        return new AgentConfirmation(register.getAgentGuid(), true, false, 0, List.of());
    }
    /** */
    protected AgentConfirmation onAgentUnregister(AgentRegister register) {
        log.info("Unregistering Agent at Elasticsearch for GUID: " + register.getAgentGuid() + ", Elastic: " + elasticUrl);
        elasticDao.addOrUpdateDocument(registerIndexName, register.getAgentGuid(), register.toAgentRegisterRow().toMap());
        return new AgentConfirmation(register.getAgentGuid(), false, true, 0, List.of());
    }
    @Override
    protected AgentPingResponse onAgentPing(AgentPing ping) {
        log.info("Registration Elasticsearch ping agent for GUID: " + ping.getAgentGuid());
        Map<String, String> doc = ping.toMap();
        elasticDao.addOrUpdateDocument(registerIndexName, ping.getAgentGuid(), doc);
        return new AgentPingResponse(ping.getAgentGuid());
    }
    /** remove active agents without last ping date for more than X minutes */
    public boolean removeInactiveAgents(LocalDateTime beforeDate) {
        var agentsToInactivate = getAgents().stream().filter(a -> a.getActive() == 1 && a.getLastPingDate().isBefore(beforeDate)).collect(Collectors.toList());
        log.info("Deactivating old agent without ping before: " + beforeDate.toString() +", by agent:" + parentAgent.getAgentGuid() +", to deactivate: " + agentsToInactivate.size());
        agentsToInactivate.stream().forEach(a -> {
            log.info("Deactivating old agent without ping for GUID: " + a.getAgentGuid() +", by agent: " + parentAgent.getAgentGuid() + ", lastPing: " + a.getLastPingDate().toString() + ", active: " + a.getActive());
            a.deactivate();
            elasticDao.addOrUpdateDocument(registerIndexName, a.getAgentGuid(), a.toMap());
        });
        return true;
    }

    /** remove inactive agents with last ping date for more than X minutes */
    public boolean deleteInactiveAgents(LocalDateTime beforeDate) {
        var agentsDelete = getAgents().stream().filter(a -> a.getLastPingDate().isBefore(beforeDate)).collect(Collectors.toList());
        log.info("Deleting old agent without ping before: " + beforeDate.toString() +", by agent:" + parentAgent.getAgentGuid() + ", to delete: " + agentsDelete.size());
        agentsDelete.stream().forEach(a -> {
            log.info("Deleting old agent without ping for GUID: " + a.getAgentGuid() +", by agent: " + parentAgent.getAgentGuid() + ", lastPing: " + a.getLastPingDate().toString());
            elasticDao.deleteDocument(registerIndexName, a.getAgentGuid());
        });
        return true;
    }
    /** get normalized URL for this registration */
    public String getUrl() {
        return elasticUrl;
    }
    /** add issue for registration */
    public void addIssue(DistIssue issue) {
        DistAgentIssueRow row = issue.toRow();
        elasticDao.addOrUpdateDocument(issueIndexName, row.getGuid(), row.toMap());
    }
    /** register server for communication */
    public void addServer(DistAgentServerRow serv) {
        elasticDao.addOrUpdateDocument(serverIndexName, serv.getServerGuid(), serv.toMap());
    }
    /** unregister server for communication */
    public void unregisterServer(DistAgentServerRow serv) {
        serv.deactivate();
        elasticDao.addOrUpdateDocument(serverIndexName, serv.getServerGuid(), serv.toMap());
    }
    /** get all communication servers */
    public List<DistAgentServerRow> getServers() {
        return elasticDao.searchComplexToMaps(serverIndexName, "match", "type", "server", DistAgentServerRow::fromMap);
    }
    /** ping given server by GUID */
    public boolean serverPing(DistAgentServerRow serv) {

        return false;
    }
    /** set active servers with last ping date before given date as inactive */
    public boolean serversCheck(LocalDateTime inactivateBeforeDate, LocalDateTime deleteBeforeDate) {

        return false;
    }
    /** register service */
    public void registerService(DistAgentServiceRow service) {
        log.info("Registering service in Elasticsearch, GUID: " + service.getServiceGuid() + ", type: " + service.getServiceType());
        elasticDao.addOrUpdateDocument(serviceIndexName, service.getServiceGuid(), service.toMap());
    }
    /** get agents from registration services */
    public List<DistAgentRegisterRow> getAgents() {
        return elasticDao.searchComplexToMaps(registerIndexName, "match", "type", "agent", DistAgentRegisterRow::fromMap);
    }

    /** get all shared storages */
    public List<DistAgentStorageRow> getStorages() {
        return List.of();
    }
    /** get all shared storage names */
    public List<String> getStorageNames() {
        return List.of();
    }
    /** get storage by name */
    public Optional<DistAgentStorageRow> getStorageByName(String storageName) {
        return Optional.empty();
    }
    /** add storage */
    public boolean addStorage(DistAgentStorageRow storage) {
        return false;
    }

    /** get all shared reports */
    public List<DistAgentReportRow> getReports() {
        return List.of();
    }
    /** get all shared report names */
    public List<String> getReportNames() {
        return List.of();
    }
    /** get report by name */
    public Optional<DistAgentReportRow> getReportByName(String reportName) {
        return Optional.empty();
    }
    /** add report */
    public boolean addReport(DistAgentReportRow report) {
        return false;
    }
    /** add report run */
    public boolean addReportRun(DistAgentReportRunRow reportRun) {
        return false;
    }

    /** get all monitors */
    public List<DistAgentMonitorRow> getMonitors() {
        return List.of();
    }
    /** get all monitor names */
    public List<String> getMonitorNames() {
        return List.of();
    }
    /** get monitor by name */
    public Optional<DistAgentMonitorRow> getMonitorByName(String reportName) {
        return Optional.empty();
    }
    /** add monitor */
    public boolean addMonitor(DistAgentMonitorRow monitor) {
        return false;
    }
    /** add monitor check */
    public boolean addMonitorCheck(DistAgentMonitorCheckRow monitorCheck) {
        return false;
    }

    /** get all notifications */
    public List<DistAgentNotificationRow> getNotifications() {
        return List.of();
    }
    /** add notification */
    public boolean addNotification(DistAgentNotificationRow notif) {
        return false;
    }

    /** get all schedules */
    public List<DistAgentScheduleRow> getSchedules() {
        return List.of();
    }
    /** add schedule */
    public boolean addSchedule(DistAgentScheduleRow schedule) {
        return false;
    }
    /** add schedule */
    public boolean addScheduleExecution(DistAgentScheduleRow schedule) {
        return false;
    }

    /** get all spaces */
    public List<DistAgentSpaceRow> getSpaces() {
        return List.of();
    }
    /** get all space names */
    public List<String> getSpaceNames() {
        return List.of();
    }
    /** get space by name */
    public Optional<DistAgentSpaceRow> getSpaceByName(String spaceName) {
        return Optional.empty();
    }
    /** add space */
    public boolean addSpace(DistAgentSpaceRow space) {
        return false;
    }
    /** remove space */
    public boolean removeSpace(String spaceName) {
        return false;
    }

    /** get all measures */
    public List<DistAgentMeasureRow> getMeasures() {
        return List.of();
    }
    /** get all measure names */
    public List<String> getMeasureNames() {
        return List.of();
    }
    /** add measure */
    public boolean addMeasure(DistAgentMeasureRow measure) {
        return false;
    }

    /** get all queries */
    public List<DistAgentQueryRow> getQueries() {
        return List.of();
    }
    /** get all query names */
    public List<String> getQueryNames() {
        return List.of();
    }
    /** get query by name */
    public Optional<DistAgentQueryRow> getQueryByName(String queryName) {
        return Optional.empty();
    }
    /** add or edit query */
    public boolean addQuery(DistAgentQueryRow measure) {
        return false;
    }

    /** add DAO row */
    public boolean addDao(DistAgentDaoRow dao) {
        return false;
    }

    /** get all global resources for given type */
    public List<DistAgentResourceRow> getResourcesForType(String resourceType) {
        return List.of();
    }
    /** get all global resource names for given type */
    public List<String> getResourceNamesForType(String resourceType) {
        return List.of();
    }
    /** get resource by name */
    public Optional<DistAgentResourceRow> getResourceByName(String queryName) {
        return Optional.empty();
    }
    /** add new resources */
    public boolean addResources(List<DistAgentResourceRow> resources) {
        return false;
    }

    /** get all settings */
    public List<DistAgentSettingRow> getSettings() {
        return List.of();
    }
    /** search for settings */
    public List<DistAgentSettingRow> searchSettings(String findStr) {
        return List.of();
    }
    /** add settings */
    public boolean addSettings(List<DistAgentSettingRow> settings) {
        return false;
    }

    /** get resource by name */
    public List<String> getScriptNames() {
        return List.of();
    }
    /** get script by name */
    public Optional<DistAgentScriptRow> getScriptForName(String scriptName) {
        return Optional.empty();
    }
    /** add script */
    public boolean addScript(DistAgentScriptRow script) {
        return false;
    }

    /** close current connector */
    @Override
    protected void onClose() {
        try {
            log.warn("Closing Elasticsearch registration");
            elasticDao.close();
        } catch (Exception ex) {
        }
    }
}
