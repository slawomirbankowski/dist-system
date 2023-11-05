package com.distsystem.dao;

import com.distsystem.api.DaoParams;
import com.distsystem.base.DaoBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentComponent;
import com.distsystem.utils.AdvancedMap;
import com.distsystem.utils.DistUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/** base class for any JDBC based DAO */
public class DaoJdbcBase extends DaoBase implements AgentComponent {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(DaoJdbcBase.class);
    private final String jdbcUrl;
    private final String jdbcDriver;
    private final String jdbcUser;
    private final String jdbcPass;
    private final int initConnections;
    private final int maxActiveConnections;

    /** DBCP - connection pool to the JDBC compliant database */
    private org.apache.commons.dbcp.BasicDataSource connPool;

    /** creates new DAO to JDBC database */
    public DaoJdbcBase(DaoParams params, Agent agent) {
        super(params, agent);
        this.jdbcUrl = params.getUrl();
        this.jdbcDriver = params.getDriver();
        this.jdbcUser = params.getUser();
        this.jdbcPass = params.getPass();
        this.initConnections = params.getInitConnections();
        this.maxActiveConnections = params.getMaxActiveConnections();
        onInitialize();
    }
    /** creates new DAO to JDBC database */
    public DaoJdbcBase(String jdbcUrl, String jdbcDriver, String jdbcUser, String jdbcPass, int initConn, int maxActiveConn, Agent agent) {
        super(DaoParams.jdbcParams(jdbcUrl, jdbcDriver, jdbcUser, jdbcPass, initConn, maxActiveConn), agent);
        this.jdbcUrl = jdbcUrl;
        this.jdbcDriver = jdbcDriver;
        this.jdbcUser = jdbcUser;
        this.jdbcPass = jdbcPass;
        this.initConnections = initConn;
        this.maxActiveConnections = maxActiveConn;
        onInitialize();
    }
    public DaoJdbcBase(String jdbcUrl, String jdbcDriver, String jdbcUser, String jdbcPass, Agent agent) {
        super(DaoParams.jdbcParams(jdbcUrl, jdbcDriver, jdbcUser, jdbcPass, 2, 10), agent);
        this.jdbcUrl = jdbcUrl;
        this.jdbcDriver = jdbcDriver;
        this.jdbcUser = jdbcUser;
        this.jdbcPass = jdbcPass;
        this.initConnections = 2;
        this.maxActiveConnections = 10;
        testDriver();
        onInitialize();
    }

    /** read configuration and re-initialize this component */
    public boolean componentReinitialize() {
        // nothing to be done here
        return true;
    }

    /** get URL of this DAO */
    public String getUrl() {
        return jdbcUrl;
    }
    /** get number of active connections */
    public int getActiveConnections() {
        return connPool.getNumActive();
    }
    /** get number of idle connections */
    public int getIdleConnections() {
        return connPool.getNumIdle();
    }

    public void onInitialize() {
        try {
            log.info("Connecting to Connection Pool, GUID: " + getGuid() + ", URL=" + jdbcUrl);
            connPool = new org.apache.commons.dbcp.BasicDataSource();
            connPool.setUrl(resolve(jdbcUrl));
            connPool.setUsername(resolve(jdbcUser));
            connPool.setPassword(resolve(jdbcPass));
            connPool.setDriverClassName(resolve(jdbcDriver));
            connPool.setInitialSize(initConnections);
            connPool.setMaxActive(maxActiveConnections);
            log.info("Connected!!!! to Connection Pool, URL=" + resolve(jdbcUrl));
        } catch (Exception ex) {
            log.info("Cannot connect to JDBC, GUID: " + getGuid() + ",URL:" + jdbcUrl + ", reason: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    /** get all tables */
    public Collection<String> getDaoStructures() {
        return executeSelectQuery("select table_name from information_schema.tables where table_name like 'dist%'",
                new Object[0], row -> row.get("table_name").toString());
    }

    public <T> T withConnection(Function<Connection, T> toDo, T errorObj) {
        try {
            Connection conn = connPool.getConnection();
            T ret = toDo.apply(conn);
            conn.close();
            return ret;
        } catch (Exception ex) {
            log.warn("Cannot perform operation with connection, reason: " + ex.getMessage(), ex);
            return errorObj;
        }
    }

    /** execute update for one row with empty params */
    public int executeUpdateQuery(String sql) {
        return executeUpdateQuery(sql, EMPTY_PARAMS);
    }
    public int executeUpdateQuery(String sql, Object[] params) {
        return executeUpdateQuery(sql, rowtoParams(params));
    }
    /** */
    public int executeUpdateQueryParams(String sql, Object... params) {
        return executeUpdateQuery(sql, params);
    }
    /** execute INSERT/UPDATE/DELETE query */
    public int executeUpdateQuery(String sql, List<Object[]> params) {
        if (sql == null || sql.isEmpty()) {
            return -1;
        } else {
            return withConnection(conn -> {
                try {
                    int updCnt = 0;
                    log.debug("UPDATE QUERY, sql: " + sql + ", parameters count: " + params.size());
                    PreparedStatement st = createStatement(conn, sql);
                    for (Object[] p : params) {
                        fillStatement(st, p);
                        updCnt += st.executeUpdate();
                    }
                    st.close();
                    return updCnt;
                } catch (SQLException ex) {
                    log.warn("Exception while executing SQL: " + sql + ", reason: " + ex.getMessage());
                    return -1;
                }
            }, -1);
        }
    }
    /** execute any query on database */
    public int executeAnyQuery(String sql) {
        return executeAnyQuery(sql, new Object[0]);
    }
    /** execute any query on database with given SQL and parameters */
    public int executeAnyQuery(String sql, Object[] params) {
        return executeAnyQuery(sql, rowtoParams(params));
    }
    /** execute any query on database with given SQL and parameters */
    public int executeAnyQuery(String sql, List<Object[]> params) {
        if (sql == null || sql.isEmpty()) {
            return -1;
        } else {
            return withConnection(conn -> {
                try {
                    int trueCnt = 0;
                    PreparedStatement st = createStatement(conn, sql);
                    for (Object[] p : params) {
                        log.debug("Fill statement for SQL:" + sql);
                        fillStatement(st, p);
                        boolean ok = st.execute();
                        if (ok) {
                            trueCnt++;
                        }
                    }
                    st.close();
                    log.debug("------ Executed query: " +sql);
                    return trueCnt;
                } catch (SQLException ex) {
                    log.warn("Cannot execute query, reason: " + ex.getMessage(), ex);
                    return -2;
                }
            }, -1);
        }
    }
    /** execute SELECT query from resultset */
    public LinkedList<Map<String, Object>> executeSelectQuery(String sql) {
        return executeSelectQuery(sql, new Object[0]);
    }
    public AdvancedMap executeSelectQuerySingle(String sql) {
        LinkedList<Map<String, Object>> rows = executeSelectQuery(sql, new Object[0]);
        if (rows.isEmpty()) {
            return new AdvancedMap(Map.of());
        } else {
            AdvancedMap map = new AdvancedMap(rows.get(0));
            return map;
        }
    }
    public LinkedList<Map<String, Object>> executeSelectQuery(String sql, Object[] params) {
        return executeSelectQuery(sql, params, x -> x);
    }
    public List<Object> executeSelectQuerySingleColumn(String sql, Object[] params, String colName) {
        return executeSelectQuery(sql, params).stream().map(x -> x.getOrDefault(colName, "")).collect(Collectors.toList());
    }
    /** */
    public <T> LinkedList<T> executeSelectQuery(String sql, Function<Map<String, Object>, T> convertMethod) {
        return executeSelectQuery(sql, EMPTY_OBJECT_TAB, convertMethod);
    }
    /** execute SELECT query from resultset */
    public <T> List<T> executeSelectQueryOptional(String sql, Object[] params, Function<Map<String, Object>, Optional<T>> convertMethod) {
        return executeSelectQuery(sql, params, convertMethod).stream().flatMap(x -> x.stream()).collect(Collectors.toList());
    }
    /** execute SELECT query from resultset */
    public <T> LinkedList<T> executeSelectQuery(String sql, Object[] params, Function<Map<String, Object>, T> convertMethod) {
        if (sql == null || sql.isEmpty()) {
            return new LinkedList<>();
        } else {
            return withConnection(conn -> {
                try {
                    LinkedList<T> tmpRows = new LinkedList<T>();
                    PreparedStatement stat = createStatement(conn, sql);
                    fillStatement(stat, params);
                    ResultSet rs = stat.executeQuery();
                    int colsCount = stat.getMetaData().getColumnCount();
                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        for (int col=1; col<=colsCount; col++) {
                            row.put(rs.getMetaData().getColumnName(col), rs.getObject(col));
                        }
                        tmpRows.add(convertMethod.apply(row));
                    }
                    return tmpRows;
                } catch (SQLException ex) {
                    log.warn("Cannot execute query for SQL: " + sql + ", reason: " + ex.getMessage());
                    return new LinkedList<T>();
                }
            }, new LinkedList<T>());
        }
    }
    /** */
    public int executeInsert(Object obj, String tableName) {
        return executeInsert(List.of(obj), tableName);
    }
    /** insert full object to given table name */
    public int executeInsert(List<Object> objs, String tableName) {
        if (objs.isEmpty()) {
            return 0;
        } else {
            Object sampleObj = objs.get(0);
            String sql = getSqlForClass(sampleObj, tableName);
            log.debug("SAVING objects to DB, count: " + objs.size() +  "; SQL: " + sql);
            Field[] fields = sampleObj.getClass().getDeclaredFields();
            List<Object[]> params = objs.stream().map(o -> DistUtils.getValuesForFields(o, fields)).collect(Collectors.toList());
            return executeUpdateQuery(sql, params);
        }
    }
    /** returns true if DB is connected */
    public boolean isConnected() {
        try {
            Connection conn = connPool.getConnection();
            boolean closed = conn.isClosed();
            log.debug("Closed: " + closed);
            conn.close();
            return !closed;
        } catch (SQLException ex) {
            log.info("Cannot check if connection is connected");
            return false;
        }
    }
    public boolean closeDao() {
        try {
            log.info("Closing DAO JDBC for agent: " + parentAgent.getAgentGuid());
            connPool.close();
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }
    /** close current connection pool*/
    protected void onClose() {
        closeDao();
    }
    /** try to test driver for JDBC */
    private void testDriver() {
        try {
            Class.forName(jdbcDriver);
            log.info("Got driver: " + jdbcDriver);
        } catch (Exception ex) {
            log.warn("Cannot load driver for class:" + jdbcDriver + ", reason: " + ex.getMessage());
        }
    }
    /** create new PreparedStatement */
    private PreparedStatement createStatement(Connection conn, String sql) throws SQLException {
        PreparedStatement st = conn.prepareStatement(sql); //, java.sql.Statement.RETURN_GENERATED_KEYS
        return st;
    }
    /** */
    private PreparedStatement createFillStatement(Connection conn, String sql, Object[] params) throws SQLException {
        PreparedStatement st = createStatement(conn, sql);
        fillStatement(st, params);
        return st;
    }
    private void fillStatement(PreparedStatement st, Object[] params) throws SQLException {
        for (int i=1; i<=params.length; i++) {
            Object obj = params[i-1];
            if (obj instanceof java.util.Date) {
                st.setTimestamp(i, new Timestamp(((java.util.Date)obj).getTime()));
            } else {
                st.setObject(i, obj);
            }
        }
    }

    private static Object[] EMPTY_OBJECT_TAB = new Object[0];
    private static List<Object[]> EMPTY_PARAMS = new LinkedList<>();

    private static List<Object[]> ONE_ROW_PARAMS= rowtoParams(EMPTY_OBJECT_TAB);

    /** */
    private static List<Object[]> rowtoParams(Object[] row) {
        var list = new LinkedList<Object[]>();
        list.add(row);
        return list;
    }

    /** map with SQL insert statements for classes and tables */
    private static HashMap<String, String> insertSqlsForClasses = new HashMap<>();

    /** get or create SQL for given object from fields and table name */
    private static String getSqlForClass(Object obj, String tableName) {
        String key = obj.getClass().getName() + ":" + tableName;
        var sql = insertSqlsForClasses.get(key);
        if (sql == null) {
            sql = createSqlForClass(obj, tableName);
            insertSqlsForClasses.put(key, sql);
        }
        return sql;
    }
    /** create new SQL for given object class and table name */
    private static String createSqlForClass(Object obj, String tableName) {
        var fields = obj.getClass().getDeclaredFields();
        var fieldNames = Arrays.stream(fields).map(f -> f.getName()).collect(Collectors.toList());
        var questionMarks = Arrays.stream(fields).map(f -> "?").collect(Collectors.toList());
        var fieldNamesList = String.join(",", fieldNames);
        var questionMarkList = String.join(",", questionMarks);
        String sql = "insert into " + tableName + "(" + fieldNamesList + ") values (" + questionMarkList + ")";
        return sql;
    }

}
