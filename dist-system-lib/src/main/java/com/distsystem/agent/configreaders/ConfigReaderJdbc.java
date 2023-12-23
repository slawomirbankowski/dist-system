package com.distsystem.agent.configreaders;

import com.distsystem.api.DaoParams;
import com.distsystem.api.DistConfig;
import com.distsystem.api.ServiceObjectParams;
import com.distsystem.base.ConfigReaderBase;
import com.distsystem.api.dtos.DistAgentConfigInitRow;
import com.distsystem.dao.DaoJdbcBase;
import com.distsystem.jdbc.JdbcDialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

/** */
public class ConfigReaderJdbc extends ConfigReaderBase {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(ConfigReaderJdbc.class);
    /** JDBC DAO to read configuration from */
    private DaoJdbcBase dao;
    private JdbcDialect dialect;
    private boolean closed = false;

    /** */
    public ConfigReaderJdbc(ServiceObjectParams params) {
        super(params);
        initialize();
    }

    /** count objects in this agentable object including this object */
    public long countObjectsAgentable() {
        return 2L;
    }
    /** get info about config reader as map */
    public Map<String, Object> getInfoMap() {
        return Map.of();
    }

    public void initialize() {
        try {
            touch();
            String jdbcUrl = getConfigProperty(DistConfig.URL, "");
            if (!jdbcUrl.isEmpty()) {
                String jdbcDriver = getConfigProperty(DistConfig.DRIVER, "");
                String jdbcUser = getConfigProperty(DistConfig.USER, "");
                String jdbcPass = getConfigProperty(DistConfig.PASS, "");
                String tableName = getConfigProperty(DistConfig.TABLE, "");
                String jdbcDialect = getConfigProperty(DistConfig.DIALECT, "");
                log.info("Reading external configuration for agent from JDBC, agent: " + parentAgent.getAgentGuid() + ", URL: " + jdbcUrl + ", driver: " + jdbcDriver);
                var initConnections = parentAgent.getConfig().getPropertyAsInt(DistConfig.AGENT_REGISTRATION_OBJECT_JDBC_INIT, 2);
                var maxActiveConnections = parentAgent.getConfig().getPropertyAsInt(DistConfig.AGENT_REGISTRATION_OBJECT_JDBC_MAXCONNECTIONS, 10);
                DaoParams params = DaoParams.jdbcParams(jdbcUrl, jdbcDriver, jdbcUser, jdbcPass, initConnections, maxActiveConnections);
                dao = parentAgent.getAgentDao().getOrCreateDaoOrError(DaoJdbcBase.class, params);
                dialect = JdbcDialect.getDialect(jdbcDriver, jdbcDialect);
                if (!dao.checkDaoStructure("distagentconfiginit")) {
                    log.info("Config reader JDBC detect no config table in database, agent: " + parentAgent.getAgentGuid());
                    dao.executeAnyQuery(dialect.createConfigInit());
                }
                // TODO: implement reading configuration from JDBC source
                //dao.usedByComponent(parentAgent.getConfigReader());
            } else {
                log.warn("Cannot read external configuration from JDBC, URL is empty!!!, agent: " + parentAgent.getAgentGuid());
            }
        } catch (Exception ex) {
            closed = true;
        }

    }

    /** read configuration from JDBC */
    public void readConfiguration() {
        if (dao != null) {
            touch();
            LinkedList<DistAgentConfigInitRow> configInitRows = dao.executeSelectQuery(dialect.selectConfigInit(), new Object[] { parentAgent.getDistName() }, dto -> DistAgentConfigInitRow.fromMap(dto));
            log.info("Read configuration init rows from JDBC for agent: " + parentAgent.getAgentGuid() + ", rows: " + configInitRows.size());
            Properties pr = new Properties();
            configInitRows.forEach(cr -> {
                log.info("Got new configuration value from ConfigReader JDBC, name: " + cr.getConfigName());
                pr.setProperty(cr.getConfigName(), cr.getConfigValue());
            });
            DistConfig newCfg = new DistConfig(pr);
            parentAgent.getConfig().mergeWithConfig(newCfg);
        }
    }

    /** close this reader */
    protected void onClose() {
        touch();
    }
}
