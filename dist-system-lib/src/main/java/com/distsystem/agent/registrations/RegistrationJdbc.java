package com.distsystem.agent.registrations;

import com.distsystem.api.*;
import com.distsystem.api.dtos.*;
import com.distsystem.dao.DaoJdbcBase;
import com.distsystem.base.RegistrationBase;
import com.distsystem.jdbc.JdbcDialect;
import com.distsystem.jdbc.JdbcTables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * global agent registration in JDBC database
 * */
public class RegistrationJdbc extends RegistrationBase {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(RegistrationJdbc.class);

    private DaoJdbcBase dao;
    private JdbcDialect dialect;
    private String jdbcUrl;

    public RegistrationJdbc(ServiceObjectParams params) {
        super(params);
    }

    /** count objects in this agentable object including this object */
    public long countObjectsAgentable() {
        return 3L;
    }
    /** run for initialization in classes */
    @Override
    public void onInitialize() {
        jdbcUrl =  getConfigProperty(DistConfig.URL, ""); // parentAgent.getConfig().getProperty(DistConfig.AGENT_REGISTRATION_OBJECT_JDBC_URL);
        var jdbcDriver = getConfigProperty(DistConfig.DRIVER, ""); // parentAgent.getConfig().getProperty(DistConfig.AGENT_REGISTRATION_OBJECT_JDBC_DRIVER, "");
        var jdbcUser = getConfigProperty(DistConfig.USER, ""); // parentAgent.getConfig().getProperty(DistConfig.AGENT_REGISTRATION_OBJECT_JDBC_USER, "");
        var jdbcPass = getConfigProperty(DistConfig.PASS, ""); // parentAgent.getConfig().getProperty(DistConfig.AGENT_REGISTRATION_OBJECT_JDBC_PASS, "");
        var jdbcDialect = getConfigProperty(DistConfig.DIALECT, ""); // parentAgent.getConfig().getProperty(DistConfig.AGENT_REGISTRATION_OBJECT_JDBC_DIALECT, "");
        dialect = JdbcDialect.getDialect(jdbcDriver, jdbcDialect);
        log.info("Initializing of JDBC registration with URL: " + jdbcUrl + ", user: " + jdbcUser + ", agent: " + parentAgent.getAgentGuid() + ", dialect: " + dialect.dialectName);
        var initConnections = parentAgent.getConfig().getPropertyAsInt(DistConfig.AGENT_REGISTRATION_OBJECT_JDBC_INIT, 2);
        var maxActiveConnections = parentAgent.getConfig().getPropertyAsInt(DistConfig.AGENT_REGISTRATION_OBJECT_JDBC_MAXCONNECTIONS, 10);
        DaoParams params = DaoParams.jdbcParams(jdbcUrl, jdbcDriver, jdbcUser, jdbcPass, initConnections, maxActiveConnections);
        dao = parentAgent.getAgentDao().getOrCreateDaoOrError(DaoJdbcBase.class, params);
        dao.usedByComponent(this);
        tryCreateAgentTable();
    }
    /** read configuration and re-initialize this component */
    public boolean componentReinitialize() {
        // TODO: implement reinitialization
        onInitialize();
        return true;
    }
    /** if needed - create SQL agent table */
    private void tryCreateAgentTable() {
        dao.checkAndCreateModel(DaoModel.authAccount);
        dao.checkAndCreateModel(DaoModel.authDomain);
        dao.checkAndCreateModel(DaoModel.authIdentity);
        dao.checkAndCreateModel(DaoModel.authKey);
        dao.checkAndCreateModel(DaoModel.authRole);
        dao.checkAndCreateModel(DaoModel.authTokenParser);
        dao.checkAndCreateModel(DaoModel.cacheItem);
        dao.checkAndCreateModel(DaoModel.configInit);
        dao.checkAndCreateModel(DaoModel.config);
        dao.checkAndCreateModel(DaoModel.dao);
        dao.checkAndCreateModel(DaoModel.event);
        dao.checkAndCreateModel(DaoModel.issue);
        dao.checkAndCreateModel(DaoModel.measure);
        dao.checkAndCreateModel(DaoModel.monitorCheck);
        dao.checkAndCreateModel(DaoModel.monitor);
        dao.checkAndCreateModel(DaoModel.notification);
        dao.checkAndCreateModel(DaoModel.query);
        dao.checkAndCreateModel(DaoModel.register);
        dao.checkAndCreateModel(DaoModel.report);
        dao.checkAndCreateModel(DaoModel.reportRun);
        dao.checkAndCreateModel(DaoModel.resource);
        dao.checkAndCreateModel(DaoModel.schedule);
        dao.checkAndCreateModel(DaoModel.scheduleExecution);
        dao.checkAndCreateModel(DaoModel.server);
        dao.checkAndCreateModel(DaoModel.service);
        dao.checkAndCreateModel(DaoModel.setting);
        dao.checkAndCreateModel(DaoModel.space);
        dao.checkAndCreateModel(DaoModel.storage);
    }
    /** get normalized URL for this registration */
    public String getUrl() {
        return jdbcUrl;
    }

    @Override
    protected boolean onIsConnected() {
        // return true if there is connection to JDBC database, otherwise falsedao
        return dao.isConnected();
    }
    @Override
    protected AgentConfirmation onAgentRegister(AgentRegister register) {
        // register this agent in JDBC
        log.info("Registering new agent in JDBC, GUID: " + this.parentAgent.getAgentGuid() + ", dialect: " + dialect.dialectName);
        dao.executeInsertRowForModel(DaoModel.register, register.toRegister());
        parentAgent.getConfig().getHashMap(false).entrySet().stream().forEach(cfg -> {
            dao.executeInsertRowForModel(DaoModel.config, new DistAgentConfigRow(register.getAgentGuid(), cfg.getKey(), cfg.getValue()));
        });
        return new AgentConfirmation(register.getAgentGuid(), true, false, 0, List.of());
    }

    /** unregistering Agent from JDBC */
    protected AgentConfirmation onAgentUnregister(AgentRegister register) {
        log.info("Unregistering agent in JDBC: " + register.getAgentGuid());
        dao.executeAnyQuery(dialect.removeAgentRegister(), new Object[] {new java.util.Date(), register.getAgentGuid()});
        return new AgentConfirmation(register.getAgentGuid(), true, false, 0, List.of());
    }

    /** */
    @Override
    protected AgentPingResponse onAgentPing(AgentPing ping) {
        log.info("====> PINGing agent in JDBC, guid: " + ping.getAgentGuid());
        dao.executeUpdateQuery(dialect.pingAgentRegister(),
                new Object[] {new java.util.Date(), ping.getAgentsConnected(), ping.getThreadsCount(), ping.getServicesCount(),
                        ping.getServersCount(), ping.getClientsCount(), ping.getAgentGuid()});
        dao.executeUpdateQuery(dialect.checkAgentRegisters(), new Object[0]);
        return new AgentPingResponse();
    }
    /** remove active agents without last ping date for more than X minutes */
    public boolean removeInactiveAgents(LocalDateTime beforeDate) {
        try {
            dao.executeUpdateQuery(dialect.updateInactiveAgentRegisters(), new Object[] { beforeDate });
            return true;
        } catch (Exception ex) {
            log.warn("Cannot remove inactive agents at JDBC, reason: " + ex.getMessage());
            addIssueToAgent("removeInactiveAgents", ex);
            return false;
        }
    }
    /** remove inactive agents with last ping date for more than X minutes */
    public boolean deleteInactiveAgents(LocalDateTime beforeDate) {
        try {
            dao.executeUpdateQuery(dialect.deleteInactiveAgentRegisters(), new Object[] { beforeDate });
            return true;
        } catch (Exception ex) {
            log.warn("Cannot remove inactive agents at JDBC, reason: " + ex.getMessage());
            addIssueToAgent("deleteInactiveAgents", ex);
            return false;
        }
    }
    /** add issue for registration */
    public void addIssue(DistIssue issue) {
        try {
            dao.executeInsertRowForModel(DaoModel.issue, issue.toRow());
        } catch (Exception ex) {
            log.warn("Cannot register issue at JDBC, reason: " + ex.getMessage());
        }
    }
    /** register server for communication */
    public void addServer(DistAgentServerRow serv) {
        try {
            log.info("Registering server in JDBC, dialect: " + dialect.dialectName + ", server: " + serv.getServerGuid());
            // distagentserver(agentguid text, servertype text, serverhost text, serverip text, serverport int, serverurl text, createddate timestamp, isactive int, lastpingdate timestamp)
            dao.executeInsertRowForModel(DaoModel.server, serv);
        } catch (Exception ex) {
            log.warn("Cannot register server at JDBC, reason: " + ex.getMessage(), ex);
            addIssueToAgent("addServer", ex);
        }
    }
    /** unregister server for communication */
    public void unregisterServer(DistAgentServerRow serv) {
        try {
            log.info("Unregistering server in JDBC, dialect: " + dialect.dialectName + ", server: " + serv.getServerGuid());
            dao.executeUpdateQuery(dialect.deleteAgentServer(),
                    new Object[] { serv.getAgentGuid(), serv.getServerGuid() });
        } catch (Exception ex) {
            log.warn("Cannot unregister server at JDBC, reason: " + ex.getMessage(), ex);
            addIssueToAgent("unregisterServer", ex);
        }
    }
    /** get all communication servers */
    public  List<DistAgentServerRow> getServers() {
        return dao.executeSelectAllModel(DaoModel.server, 100);
    }
    /** ping given server by GUID */
    public boolean serverPing(DistAgentServerRow serv) {
        dao.executeUpdateQuery(dialect.pingAgentServer(),
               new Object[] { serv.getAgentGuid(), serv.getServerGuid() });
        return true;
    }
    /** set active servers with last ping date before given date as inactive */
    public boolean serversCheck(LocalDateTime inactivateBeforeDate, LocalDateTime deleteBeforeDate) {
        dao.executeUpdateQuery(dialect.checkAgentServer(),
                new Object[] { inactivateBeforeDate });
        dao.executeUpdateQuery(dialect.deleteAgentServers(),
                new Object[] { deleteBeforeDate });
        return true;
    }
    /** get agents from registration services */
    public  List<DistAgentRegisterRow> getAgents() {
        return dao.executeSelectAllModel(DaoModel.register, 1000);
    }
    /** get list of active agents from JDBC table */
    public List<DistAgentRegisterRow> getAgentsActive() {
        return dao.executeSelectAllActiveModel(DaoModel.register, 1000);
    }
    /** register service */
    public void registerService(DistAgentServiceRow service) {
        log.info("Registering service in JDBC registration, service: " + service.getServiceGuid() + ", agent: " + service.getAgentGuid() + ", JDBC: " + jdbcUrl);
        dao.executeInsertRowForModel(DaoModel.service, service);
    }

    /** get all shared storages */
    public List<DistAgentStorageRow> getStorages() {
        return dao.executeSelectAllActiveModel(DaoModel.storage, 1000);
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
        return dao.executeSelectAllActiveModel(DaoModel.report, 1000);
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
        return dao.executeSelectAllActiveModel(DaoModel.monitor, 1000);
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
        return dao.executeSelectAllActiveModel(DaoModel.notification, 1000);
    }
    /** add notification */
    public boolean addNotification(DistAgentNotificationRow notif) {
        return false;
    }

    /** get all schedules */
    public List<DistAgentScheduleRow> getSchedules() {
        return dao.executeSelectAllActiveModel(DaoModel.schedule, 1000);
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
        return dao.executeSelectAllActiveModel(DaoModel.space, 1000);
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
        return dao.executeSelectAllActiveModel(DaoModel.measure, 1000);
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
        return dao.executeSelectAllActiveModel(DaoModel.query, 1000);
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
    public boolean addQuery(DistAgentQueryRow query) {
        dao.executeInsertRowForModel(DaoModel.query, query);
        return true;
    }

    /** add DAO row */
    public boolean addDao(DistAgentDaoRow daoRow) {
        dao.executeInsertRowForModel(DaoModel.dao, daoRow);
        return true;
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
        return dao.executeSelectAllActiveModel(DaoModel.setting, 1000);
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
        log.info("Closing JDBC registration object, unregistering agent");
        // dao.executeSelectQuery(dialect.removeAgentRegister(), new Object[0]);
    }

}
