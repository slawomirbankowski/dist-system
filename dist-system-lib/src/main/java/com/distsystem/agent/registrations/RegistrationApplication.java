package com.distsystem.agent.registrations;

import com.distsystem.api.*;
import com.distsystem.api.dtos.*;
import com.distsystem.base.RegistrationBase;
import com.distsystem.interfaces.HttpCallable;
import com.distsystem.utils.HttpConnectionHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/** connector to global dist-cache application - central point with registering/unregistering agents  */
public class RegistrationApplication extends RegistrationBase {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(RegistrationApplication.class);

    /** */
    private String urlString;
    /** HTTP connection helper */
    private HttpCallable applicationConn = null;

    public RegistrationApplication(ServiceObjectParams params) {
        super(params);
    }
    /** run for initialization in classes */
    @Override
    public void onInitialize() {
        urlString = parentAgent.getConfig().getProperty(DistConfig.AGENT_CACHE_APPLICATION_URL);
        try {
            log.info("Connecting to dist-cache application, URL: " + urlString);
            applicationConn = HttpConnectionHelper.createHttpClient(urlString);
        } catch (Exception ex) {
            parentAgent.getIssues().addIssue("AgentTimersImpl.onInitialize", ex);
            log.warn("Cannot connect to dist-cache application, reason: " + ex.getMessage(), ex);
        }
    }

    /** count objects in this agentable object including this object */
    public long countObjectsAgentable() {
        return 2L;
    }
    /** read configuration and re-initialize this component */
    public boolean componentReinitialize() {
        // TODO: implement reinitialization

        if (applicationConn != null) {

        }
        onInitialize();

        return true;
    }
    @Override
    protected boolean onIsConnected() {
        return false;
    }
    @Override
    protected AgentConfirmation onAgentRegister(AgentRegister register) {
        try {
            log.info("Try to register agent as dist-cache application on URL: " + urlString + ", agent: " + register.getAgentGuid());
            ObjectMapper mapper = JsonMapper.builder()
                    .findAndAddModules()
                    .build();
            String registerBody = mapper.writeValueAsString(register);
            applicationConn = HttpConnectionHelper.createHttpClient(urlString);
            log.info("Try to register agent with endpoint /agent and body: " + registerBody);
            var response = applicationConn.callPut("/v1/agent", registerBody);
            // TODO: save response from application
            log.info("Got registration response from APP: " + response.getInfo());
            return null;
        } catch (Exception ex) {
            parentAgent.getIssues().addIssue("RegistrationApplication.onAgentRegister", ex);
            log.warn("Cannot connect to dist-cache application, reason: " + ex.getMessage(), ex);
            return null;
        }
    }
    protected AgentConfirmation onAgentUnregister(AgentRegister register) {

        return new AgentConfirmation(register.getAgentGuid(), true, false, 0, List.of());
    }
    @Override
    protected AgentPingResponse onAgentPing(AgentPing ping) {
        // TODO: implement ping to connector from this agent

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
        return urlString;
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
    public  List<DistAgentServerRow> getServers() {
        return new LinkedList<>();
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
        return new LinkedList<>();
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
    public List<DistAgentStorageRow> getReports() {
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
        // TODO: implement closing this connector
    }

}
