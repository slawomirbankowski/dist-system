package com.distsystem.agent.registrations;

import com.distsystem.agent.AgentInstance;
import com.distsystem.api.*;
import com.distsystem.base.dtos.DistAgentRegisterRow;
import com.distsystem.base.dtos.DistAgentServerRow;
import com.distsystem.base.dtos.DistAgentServiceRow;
import com.distsystem.dao.DaoJdbcBase;
import com.distsystem.base.RegistrationBase;
import com.distsystem.jdbc.JdbcDialect;
import com.distsystem.jdbc.JdbcTables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * global agent registration in JDBC database
 * */
public class RegistrationJdbc extends RegistrationBase {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(RegistrationJdbc.class);

    private DaoJdbcBase dao;
    private JdbcDialect dialect;
    private String jdbcUrl;

    public RegistrationJdbc(AgentInstance parentAgent) {
        super(parentAgent);
    }
    /** run for initialization in classes */
    @Override
    public void onInitialize() {
        jdbcUrl = parentAgent.getConfig().getProperty(DistConfig.AGENT_REGISTRATION_OBJECT_JDBC_URL);
        var jdbcDriver = parentAgent.getConfig().getProperty(DistConfig.AGENT_REGISTRATION_OBJECT_JDBC_DRIVER, "");
        var jdbcUser = parentAgent.getConfig().getProperty(DistConfig.AGENT_REGISTRATION_OBJECT_JDBC_USER, "");
        var jdbcPass = parentAgent.getConfig().getProperty(DistConfig.AGENT_REGISTRATION_OBJECT_JDBC_PASS, "");
        var jdbcDialect = parentAgent.getConfig().getProperty(DistConfig.AGENT_REGISTRATION_OBJECT_JDBC_DIALECT, "");
        dialect = JdbcDialect.getDialect(jdbcDriver, jdbcDialect);
        log.info("Initializing of JDBC registration with URL: " + jdbcUrl + ", agent: " + parentAgent.getAgentGuid() + ", dialect: " + dialect.dialectName);
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
            log.info("Registering server in JDBC, dialect: " + dialect.dialectName + ", server: " + serv.serverguid);
            // distagentserver(agentguid text, servertype text, serverhost text, serverip text, serverport int, serverurl text, createddate timestamp, isactive int, lastpingdate timestamp)
            dao.executeUpdateQuery(dialect.insertAgentServer(),
                    new Object[] { serv.agentguid, serv.serverguid, serv.servertype, serv.serverhost, serv.serverip,
                    serv.serverport, serv.serverurl, serv.createddate, serv.isactive, serv.lastpingdate, serv.serverparams });
        } catch (Exception ex) {
            log.warn("Cannot register server at JDBC, reason: " + ex.getMessage(), ex);
            parentAgent.getAgentIssues().addIssue("RegistrationJdbc.addServer", ex);
        }
    }
    /** unregister server for communication */
    public void unregisterServer(DistAgentServerRow serv) {
        try {
            log.info("Unregistering server in JDBC, dialect: " + dialect.dialectName + ", server: " + serv.serverguid);
            dao.executeUpdateQuery(dialect.deleteAgentServer(),
                    new Object[] { serv.agentguid, serv.serverguid });
        } catch (Exception ex) {
            log.warn("Cannot unregister server at JDBC, reason: " + ex.getMessage(), ex);
        }
    }
    /** get all communication servers */
    public  List<DistAgentServerRow> getServers() {
        return dao.executeSelectQuery(dialect.selectAgentServersActive(), new Object[0], x -> DistAgentServerRow.fromMap(x));
    }
    /** ping given server by GUID */
    public boolean serverPing(DistAgentServerRow serv) {
        dao.executeUpdateQuery(dialect.pingAgentServer(),
                new Object[] { serv.agentguid, serv.serverguid });
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
        log.info("Registering service in JDBC registration, service: " + service.serviceguid + ", agent: " + service.agentguid + ", JDBC: " + jdbcUrl);
        //dao.executeAnyQuery(dialect.insertAgentService(), new Object[] {service.agentguid});
    }
    /** close current connector */
    @Override
    protected void onClose() {
        log.info("Closing JDBC registration object, unregistering agent");
        dao.executeSelectQuery(dialect.removeAgentRegister(), new Object[0]);
    }

}
