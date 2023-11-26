package com.distsystem.jdbc;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/** dialects for different databases like Postgres, MySql, MS SQL Server, Oracle, IBM DB2 */
public class JdbcDialect {

    /** name of this dialect */
    public final String dialectName;
    /** all queries for this dialect */
    protected Properties dialectQueries = new Properties();

    /** creates new dialect for given name */
    private JdbcDialect(String dialectName) {
        this.dialectName = dialectName;
    }
    /** load dialect from resource */
    public boolean loadFromResource(String resourceFile) {
        try {
            dialectQueries.load(this.getClass().getClassLoader().getResourceAsStream(resourceFile));
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
    /** get SQL query for given template name */
    public Optional<String> getQuery(DialectQueries templateName) {
        Object sql = dialectQueries.get(templateName.name());
        if (sql != null) {
            return Optional.of(""+sql);
        } else {
            return Optional.empty();
        }
    }
    /** get SQL query for given template */
    public String getQueryOrEmpty(DialectQueries templateName) {
        Object sql = dialectQueries.get(templateName.name());
        return ""+sql;
    }
    public String dialectName() { return getQueryOrEmpty(DialectQueries.dialectName); }
    public String selectAllTables() { return getQueryOrEmpty(DialectQueries.selectAllTables); }
    public String selectTable() { return getQueryOrEmpty(DialectQueries.selectTable); }
    public String selectCacheTables() { return getQueryOrEmpty(DialectQueries.selectCacheTables); }
    public String createDistCacheItemTable() { return getQueryOrEmpty(DialectQueries.createDistCacheItemTable); }
    public String createCacheItemIndex() { return getQueryOrEmpty(DialectQueries.createCacheItemIndex); }
    public String selectFindCacheItems() { return getQueryOrEmpty(DialectQueries.selectFindCacheItems); }
    public String selectCacheItemsByKey() { return getQueryOrEmpty(DialectQueries.selectCacheItemsByKey); }
    public String insertUpdateCacheItem() { return getQueryOrEmpty(DialectQueries.insertUpdateCacheItem); }
    public String deleteOldCacheItemsTemplate() { return getQueryOrEmpty(DialectQueries.deleteOldCacheItemsTemplate); }
    public String selectAgentRegisterTable() { return getQueryOrEmpty(DialectQueries.selectAgentRegisterTable); }
    public String createAgentRegister() { return getQueryOrEmpty(DialectQueries.createAgentRegister); }
    public String createAgentRegisterIndex() { return getQueryOrEmpty(DialectQueries.createAgentRegisterIndex); }
    public String selectAgentRegisters() { return getQueryOrEmpty(DialectQueries.selectAgentRegisters); }
    public String selectActiveAgentRegisters() { return getQueryOrEmpty(DialectQueries.selectActiveAgentRegisters); }
    public String updateAgentRegister() { return getQueryOrEmpty(DialectQueries.updateAgentRegister); }
    public String pingAgentRegister() { return getQueryOrEmpty(DialectQueries.pingAgentRegister); }
    public String insertAgentRegister() { return getQueryOrEmpty(DialectQueries.insertAgentRegister); }
    public String removeAgentRegister() { return getQueryOrEmpty(DialectQueries.removeAgentRegister); }
    public String checkAgentRegisters() { return getQueryOrEmpty(DialectQueries.checkAgentRegisters); }

    public String updateInactiveAgentRegisters() { return getQueryOrEmpty(DialectQueries.updateInactiveAgentRegisters); }
    public String deleteInactiveAgentRegisters() { return getQueryOrEmpty(DialectQueries.deleteInactiveAgentRegisters); }


    public String createAgentConfig() { return getQueryOrEmpty(DialectQueries.createAgentConfig); }
    public String createAgentConfigIndex() { return getQueryOrEmpty(DialectQueries.createAgentConfigIndex); }
    public String selectAgentConfig() { return getQueryOrEmpty(DialectQueries.selectAgentConfig); }
    public String deleteAgentConfig() { return getQueryOrEmpty(DialectQueries.deleteAgentConfig); }
    public String insertAgentConfig() { return getQueryOrEmpty(DialectQueries.insertAgentConfig); }
    public String createAgentServer() { return getQueryOrEmpty(DialectQueries.createAgentServer); }
    public String createAgentServerIndex() { return getQueryOrEmpty(DialectQueries.createAgentServerIndex); }
    public String selectAgentServers() { return getQueryOrEmpty(DialectQueries.selectAgentServers); }
    public String selectAgentServersActive() { return getQueryOrEmpty(DialectQueries.selectAgentServersActive); }
    public String selectAgentServersForAgent() { return getQueryOrEmpty(DialectQueries.selectAgentServersForAgent); }
    public String insertAgentServer() { return getQueryOrEmpty(DialectQueries.insertAgentServer); }
    public String deleteAgentServer() { return getQueryOrEmpty(DialectQueries.deleteAgentServer); }
    public String pingAgentServer() { return getQueryOrEmpty(DialectQueries.pingAgentServer); }
    public String checkAgentServer() { return getQueryOrEmpty(DialectQueries.checkAgentServer); }
    public String deleteAgentServers() { return getQueryOrEmpty(DialectQueries.deleteAgentServers); }

    public String createAgentIssue() { return getQueryOrEmpty(DialectQueries.createAgentIssue); }
    public String insertAgentIssue() { return getQueryOrEmpty(DialectQueries.insertAgentIssue); }
    public String selectAgentIssue() { return getQueryOrEmpty(DialectQueries.selectAgentIssue); }

    public String createAgentService() { return getQueryOrEmpty(DialectQueries.createAgentService); }
    public String createAgentServiceIndex() { return getQueryOrEmpty(DialectQueries.createAgentServiceIndex); }
    public String insertAgentService() { return getQueryOrEmpty(DialectQueries.insertAgentService); }
    public String selectAgentService() { return getQueryOrEmpty(DialectQueries.selectAgentService); }
    public String deleteAgentService() { return getQueryOrEmpty(DialectQueries.deleteAgentService); }

    public String createConfigInit() { return getQueryOrEmpty(DialectQueries.createConfigInit); }
    public String selectConfigInit() { return getQueryOrEmpty(DialectQueries.selectConfigInit); }

    public String createDistAgentAuthAccount() { return getQueryOrEmpty(DialectQueries.createDistAgentAuthAccount); }
    public String createDistAgentAuthAccountIndex() { return getQueryOrEmpty(DialectQueries.createDistAgentAuthAccountIndex); }
    public String insertDistAgentAuthAccount() { return getQueryOrEmpty(DialectQueries.insertDistAgentAuthAccount); }
    public String selectDistAgentAuthAccount() { return getQueryOrEmpty(DialectQueries.selectDistAgentAuthAccount); }
    public String selectDistAgentAuthAccountForName() { return getQueryOrEmpty(DialectQueries.selectDistAgentAuthAccountForName); }
    public String deleteDistAgentAuthAccount() { return getQueryOrEmpty(DialectQueries.deleteDistAgentAuthAccount); }
    public String disableDistAgentAuthAccount() { return getQueryOrEmpty(DialectQueries.disableDistAgentAuthAccount); }

    public String createDistAgentAuthDomain() { return getQueryOrEmpty(DialectQueries.createDistAgentAuthDomain); }
    public String createDistAgentAuthDomainIndex() { return getQueryOrEmpty(DialectQueries.createDistAgentAuthDomainIndex); }
    public String insertDistAgentAuthDomain() { return getQueryOrEmpty(DialectQueries.insertDistAgentAuthDomain); }
    public String selectDistAgentAuthDomain() { return getQueryOrEmpty(DialectQueries.selectDistAgentAuthDomain); }

    public String createDistAgentAuthIdentity() { return getQueryOrEmpty(DialectQueries.createDistAgentAuthIdentity); }
    public String insertDistAgentAuthIdentity() { return getQueryOrEmpty(DialectQueries.insertDistAgentAuthIdentity); }
    public String selectDistAgentAuthIdentity() { return getQueryOrEmpty(DialectQueries.selectDistAgentAuthIdentity); }

    public String createDistAgentAuthRole() { return getQueryOrEmpty(DialectQueries.createDistAgentAuthRole); }
    public String insertDistAgentAuthRole() { return getQueryOrEmpty(DialectQueries.insertDistAgentAuthRole); }
    public String selectDistAgentAuthRole() { return getQueryOrEmpty(DialectQueries.selectDistAgentAuthRole); }

    public String createDistAgentAuthKey() { return getQueryOrEmpty(DialectQueries.createDistAgentAuthKey); }
    public String insertDistAgentAuthKey() { return getQueryOrEmpty(DialectQueries.insertDistAgentAuthKey); }
    public String selectDistAgentAuthKey() { return getQueryOrEmpty(DialectQueries.selectDistAgentAuthKey); }
    public String selectDistAgentAuthKeyForName() { return getQueryOrEmpty(DialectQueries.selectDistAgentAuthKeyForName); }

    public String createDistAgentAuthTokenParser() { return getQueryOrEmpty(DialectQueries.createDistAgentAuthTokenParser); }
    public String insertDistAgentAuthTokenParser() { return getQueryOrEmpty(DialectQueries.insertDistAgentAuthTokenParser); }
    public String selectDistAgentAuthTokenParser() { return getQueryOrEmpty(DialectQueries.selectDistAgentAuthTokenParser); }

    /** all supported dialects for databases */
    private static final Map<String, JdbcDialect> dialects = createDialectsMap();
    /** */
    private static JdbcDialect defaultDialect = new JdbcDialect("default");

    /** creates map with dialects for databases */
    private static Map<String, JdbcDialect> createDialectsMap() {
        var dialectDefault = createDialectFromResource("default", "default.dialect");
        var dialectPostgres = createDialectFromResource("postgres", "postgres.dialect");
        var dialectOracle = createDialectFromResource("oracle", "oracle.dialect");
        HashMap<String, JdbcDialect> map = new HashMap<>();
        if (dialectDefault.isPresent()) {
            map.put("default", dialectDefault.get());
            map.put("", dialectDefault.get());
            defaultDialect = dialectDefault.get();
        }
        if (dialectPostgres.isPresent()) {
            // dialectPostgres.get().setDefault(dialectDefault);
            map.put("postgres", dialectPostgres.get());
            map.put("postgresql", dialectPostgres.get());
            map.put("org.postgresql.driver", dialectPostgres.get());
        }
        if (dialectOracle.isPresent()) {
            // dialectPostgres.get().setDefault(dialectDefault);
            map.put("oracle", dialectOracle.get());
            map.put("com.oracle.drivermanager", dialectOracle.get());
        }
        return map;
    }

    /** create dialect and load SQL queries from given resource file */
    public static Optional<JdbcDialect> createDialectFromResource(String name, String resourceFile) {
        JdbcDialect d = new JdbcDialect(name);
        boolean loaded = d.loadFromResource(resourceFile);
        if (loaded) {
            return Optional.of(d);
        } else {
            return Optional.empty();
        }
    }
    /** get dialect by dialect name or driver class
     * if no dialect is available, then default dialect is selected */
    public static JdbcDialect getDialect(String jdbcDriver, String jdbcDialect) {
        JdbcDialect dialect = dialects.get(jdbcDialect.toLowerCase());
        if (dialect == null) {
            dialect = dialects.get(jdbcDriver.toLowerCase());
        }
        if (dialect == null) {
            dialect = defaultDialect;
        }
        return dialect;
    }


}
