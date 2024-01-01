package com.distsystem.agent.registrations;

import com.distsystem.api.*;
import com.distsystem.api.dtos.*;
import com.distsystem.base.RegistrationBase;
import com.distsystem.dao.DaoElasticsearchBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** connector to Elasticsearch as agent manager - central point with registering/unregistering agents
 * */
public class RegistrationMongodb extends RegistrationBase {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(RegistrationMongodb.class);

    /** DAO to MongoDB */
    // private DaoElasticsearchBase elasticDao;

    public RegistrationMongodb(ServiceObjectParams params) {
        super(params);
    }


    /** count objects in this agentable object including this object */
    public long countObjectsAgentable() {
        return 8L;
    }
    /** run for initialization in classes */
    @Override
    protected void onInitialize() {
        String mongodbUrl = getConfigProperty(DistConfig.URL, "http://localhost:9200");
        String mongodbUser = getConfigProperty(DistConfig.USER, "");
        String mongodbPass = getConfigProperty(DistConfig.PASS, "");
        //
    }
    /** read configuration and re-initialize this component */
    public boolean componentReinitialize() {
        // TODO: implement reinitialization
        return true;
    }

    @Override
    protected boolean onIsConnected() {
        return false;
    }
    /** */
    @Override
    protected AgentConfirmation onAgentRegister(AgentRegister register) {
        return null; // new AgentConfirmation(register.getAgentGuid(), true, false, 0, List.of());
    }
    /** */
    protected AgentConfirmation onAgentUnregister(AgentRegister register) {
        return null;
    }
    @Override
    protected AgentPingResponse onAgentPing(AgentPing ping) {
        return null;
    }
    /** remove active agents without last ping date for more than X minutes */
    public boolean removeInactiveAgents(LocalDateTime beforeDate) {
        return true;
    }

    /** remove inactive agents with last ping date for more than X minutes */
    public boolean deleteInactiveAgents(LocalDateTime beforeDate) {

        return true;
    }
    /** get normalized URL for this registration */
    public String getUrl() {
        return "";
    }
    /** add issue for registration */
    public void addIssue(DistIssue issue) {

    }
    /** register server for communication */
    public void addServer(DistAgentServerRow serv) {
    }
    /** unregister server for communication */
    public void unregisterServer(DistAgentServerRow serv) {
    }
    /** get all communication servers */
    public List<DistAgentServerRow> getServers() {
        return null;
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
    }
    /** get agents from registration services */
    public List<DistAgentRegisterRow> getAgents() {
        return null;
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
    }
}
