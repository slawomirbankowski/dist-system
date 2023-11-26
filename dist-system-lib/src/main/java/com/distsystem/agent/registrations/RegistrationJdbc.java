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
        var agentRegisterTable = dao.executeSelectQuery(dialect.selectTable(), new Object[] { JdbcTables.distagentregister.name() });
        if (agentRegisterTable.size() == 0) {
            log.info("Try to create agent registering table in JDBC, dialect: " + dialect.dialectName);
            dao.executeAnyQuery(dialect.createAgentRegister());
            dao.executeAnyQuery(dialect.createAgentRegisterIndex());
        }
        var agentServerTable = dao.executeSelectQuery(dialect.selectTable(), new Object[] {JdbcTables.distagentserver.name() });
        if (agentServerTable.size() == 0) {
            log.info("Try to create distagentserver table in JDBC, dialect: " + dialect.dialectName);
            dao.executeAnyQuery(dialect.createAgentServer());
            dao.executeAnyQuery(dialect.createAgentServerIndex());
        }
        var agentServiceTable = dao.executeSelectQuery(dialect.selectTable(), new Object[] {JdbcTables.distagentservice.name() });
        if (agentServiceTable.size() == 0) {
            log.info("Try to create distagentservice table in JDBC, dialect: " + dialect.dialectName);
            dao.executeAnyQuery(dialect.createAgentService());
            dao.executeAnyQuery(dialect.createAgentServiceIndex());
        }
        var agentConfigTable = dao.executeSelectQuery(dialect.selectTable(), new Object[] {JdbcTables.distagentconfig.name() });
        if (agentConfigTable.size() == 0) {
            log.info("Try to create distagentconfig table in JDBC, dialect: " + dialect.dialectName);
            dao.executeAnyQuery(dialect.createAgentConfig());
            dao.executeAnyQuery(dialect.createAgentConfigIndex());
        }
        var agentIssueTable = dao.executeSelectQuery(dialect.selectTable(), new Object[] {JdbcTables.distagentissue.name() });
        if (agentIssueTable.size() == 0) {
            log.info("Try to create distagentissue table in JDBC, dialect: " + dialect.dialectName);
            dao.executeAnyQuery(dialect.createAgentIssue());
        }










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
        dao.executeAnyQuery(dialect.insertAgentRegister(), new Object[] {register.getAgentGuid(), register.getHostName(), register.getHostIp(), register.getPort(), register.getCreateDate(), register.getLastPingDate(), 0, 1});
        parentAgent.getConfig().getHashMap(false).entrySet().stream().forEach(cfg -> {
            dao.executeUpdateQuery(dialect.insertAgentConfig(), new Object[] { register.getAgentGuid(), cfg.getKey(), cfg.getValue(), register.getCreateDate(), register.getCreateDate() });
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
            dao.executeUpdateQuery(dialect.insertAgentIssue(), new Object[] { parentAgent.getAgentGuid(), issue.getMethodName(), issue.getExceptionMessage(), "", "", new java.util.Date() });
        } catch (Exception ex) {
            log.warn("Cannot register issue at JDBC, reason: " + ex.getMessage());
        }
    }
    /** register server for communication */
    public void addServer(DistAgentServerRow serv) {
        try {
            log.info("Registering server in JDBC, dialect: " + dialect.dialectName + ", server: " + serv.getServerGuid());
            // distagentserver(agentguid text, servertype text, serverhost text, serverip text, serverport int, serverurl text, createddate timestamp, isactive int, lastpingdate timestamp)
            dao.executeUpdateQuery(dialect.insertAgentServer(),
                    serv.toInsertRow());
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
        return dao.executeSelectQuery(dialect.selectAgentServersActive(), new Object[0], x -> DistAgentServerRow.fromMap(x));
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
        return dao.executeSelectQuery(dialect.selectAgentServersActive(), new Object[0], x -> DistAgentRegisterRow.fromMap(x));
    }

    /** get list of active agents from JDBC table */
    public List<DistAgentRegisterRow> getAgentsActive() {
        return dao.executeSelectQuery(dialect.selectActiveAgentRegisters(), new Object[0], x -> DistAgentRegisterRow.fromMap(x));
    }
    /** register service */
    public void registerService(DistAgentServiceRow service) {
        log.info("Registering service in JDBC registration, service: " + service.getServiceGuid() + ", agent: " + service.getAgentGuid() + ", JDBC: " + jdbcUrl);
        // insert into distagentservice(agentguid, serviceguid, servicetype, createddate, isactive, lastpingdate) values (?, ?, ?, ?, ?, ?)
        dao.executeAnyQuery(dialect.insertAgentService(), service.getObjectRow());
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
        log.info("Closing JDBC registration object, unregistering agent");
        dao.executeSelectQuery(dialect.removeAgentRegister(), new Object[0]);
    }

}
