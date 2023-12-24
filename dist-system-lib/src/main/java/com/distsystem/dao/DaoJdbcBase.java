package com.distsystem.dao;

import com.distsystem.api.BaseRow;
import com.distsystem.api.DaoModel;
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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.BiFunction;
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

    /** count objects in this agentable object including this object */
    public long countObjectsAgentable() {
        return 2L;
    }
     /** read configuration and re-initialize this component */
    public boolean componentReinitialize() {
        // nothing to be done here
        log.info("Reinitializing JDBC Dao for agent: " +parentAgent.getAgentGuid() + ", DAO guid: " + getGuid() + ", URL: " + getUrl());
        if (connPool!= null) {
            try {
                connPool.close();
            } catch (Exception ex) {
                log.warn("Cannot close existing connection pool for JDBC DAO, agent: " +parentAgent.getAgentGuid() + ", DAO guid: " + getGuid() + ", URL: " + getUrl());
            }
            connPool = null;
            onInitialize();
        }
        return true;
    }

    /** get RL of this DAO */
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
                new Object[0], row -> row.get("table_name").toString(), Integer.MAX_VALUE);
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
                    log.debug("UPDATE QUERY, sql: " + sql + ", objects count: " + params.size());
                    PreparedStatement st = createStatement(conn, sql);
                    for (Object[] p : params) {
                        log.debug("----> UPDATE QUERY, object params: " + p.length);
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
    /** */
    public int checkAndCreate(String structureName, String... createQueries) {
        if (!checkDaoStructure(structureName)) {
            Arrays.stream(createQueries).forEach(createQuery -> {
                executeAnyQuery(createQuery, new Object[0]);
            });
            return createQueries.length;
        } else {
            return 0;
        }
    }
    public <T extends  BaseRow> int checkAndCreateModel(DaoModel<T> model) {
        if (!checkDaoStructure(model.getTableName())) {
            executeAnyQuery(model.getCreateTableQuery());
            executeAnyQuery(model.getCreateTableIndexQuery());
        }
        return 1;
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
    public <T> LinkedList<T> executeSelectQueryResultSet(String sql, Object[] params, BiFunction<Integer, ResultSet, T> convertMethod, int maxRows) {
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
                    int rowNum = 0;
                    while (rs.next() && rowNum<maxRows) {
                        T row = convertMethod.apply(colsCount, rs);
                        if (row != null) {
                            tmpRows.add(row);
                            rowNum++;
                        }
                    }
                    return tmpRows;
                } catch (SQLException ex) {
                    log.warn("Cannot execute query for SQL: " + sql + ", reason: " + ex.getMessage());
                    return new LinkedList<T>();
                }
            }, new LinkedList<T>());
        }
    }
    /** execute SELECT query from resultset */
    public <T> int executeSelectIterativeResultSet(String sql, Object[] params,
                                                             BiFunction<Integer, ResultSet, T> convertMethod,
                                                   Function<T, Boolean> onRow,
                                                             int maxRows) {
        if (sql == null || sql.isEmpty()) {
            return 0;
        } else {
            return withConnection(conn -> {
                int rowNum = 0;
                try {
                    PreparedStatement stat = createStatement(conn, sql);
                    fillStatement(stat, params);
                    boolean continueReading = true;
                    ResultSet rs = stat.executeQuery();
                    int colsCount = stat.getMetaData().getColumnCount();
                    while (rs.next() && rowNum<maxRows && continueReading) {
                        T row = convertMethod.apply(colsCount, rs);
                        if (row != null) {
                            continueReading = onRow.apply(row);
                            rowNum++;
                        }
                    }
                    return rowNum;
                } catch (SQLException ex) {
                    log.warn("Cannot execute query for SQL: " + sql + ", reason: " + ex.getMessage());
                    return rowNum;
                }
            }, 0);
        }
    }
    /** execute SELECT query from Map */
    public <T> LinkedList<T> executeSelectQuery(String sql, Object[] params, Function<Map<String, Object>, T> convertMethod, int maxRows) {
        return executeSelectQueryResultSet(sql, params, (colsCount, rs) -> {
            try {
                Map<String, Object> row = new HashMap<>();
                for (int col=1; col<=colsCount; col++) {
                    row.put(rs.getMetaData().getColumnName(col), rs.getObject(col));
                }
                return convertMethod.apply(row);
            } catch (SQLException ex) {
                return null;
            }
        }, maxRows);
    }

    /** execute SELECT query from Map */
    public <T> LinkedList<T> executeSelectQuery(String sql, Object[] params, Function<Map<String, Object>, T> convertMethod) {
        return executeSelectQuery(sql, params, convertMethod, Integer.MAX_VALUE);
    }
    /** */
    public LinkedList<Map<String, Object>> executeSelectQuery(String sql, Object[] params, int maxRows) {
        return executeSelectQuery(sql, params, x -> x, maxRows);
    }

    /** */
    public LinkedList<Map<String, Object>> executeSelectQuery(String sql, Object[] params) {
        return executeSelectQuery(sql, params, Integer.MAX_VALUE);
    }
    /** execute SELECT query from resultset */
    public LinkedList<Map<String, Object>> executeSelectQuery(String sql, int maxRows) {
        return executeSelectQuery(sql, new Object[0], maxRows);
    }
    /** execute SELECT query from resultset */
    public LinkedList<Map<String, Object>> executeSelectQuery(String sql) {
        return executeSelectQuery(sql, Integer.MAX_VALUE);
    }

    /** execute SELECT query from Map */
    public <T> List<T> executeSelectQueryFromOptionMap(String sql, Object[] params, Function<Map<String, Object>, Optional<T>> convertMethod) {
        return executeSelectQuery(sql, params, convertMethod, Integer.MAX_VALUE).stream().flatMap(Optional::stream).collect(Collectors.toList());
    }
    /** */
    public AdvancedMap executeSelectQuerySingle(String sql, int maxRows) {
        LinkedList<Map<String, Object>> rows = executeSelectQuery(sql, new Object[0], maxRows);
        if (rows.isEmpty()) {
            return new AdvancedMap(Map.of());
        } else {
            AdvancedMap map = new AdvancedMap(rows.get(0));
            return map;
        }
    }

    /** */
    public AdvancedMap executeSelectQuerySingle(String sql) {
        return executeSelectQuerySingle(sql, Integer.MAX_VALUE);
    }
    /** execute query for SQL and returns only first row as a Map */
    public Optional<Map<String, Object>> executeSelectQueryFirstRow(String sql, Object[] params) {
        return executeSelectQuery(sql, params, x -> x, 1).stream().findFirst();
    }
    /** execute select for model and convert map to model class */
    public <T> LinkedList<T> executeSelectQuery(String sql, Function<Map<String, Object>, T> convertMethod, int maxRows) {
        return executeSelectQuery(sql, EMPTY_OBJECT_TAB, convertMethod, maxRows);
    }
    /** execute select for model and convert map to model class */
    public <T> LinkedList<T> executeSelectQuery(String sql, Function<Map<String, Object>, T> convertMethod) {
        return executeSelectQuery(sql, convertMethod, Integer.MAX_VALUE);
    }
    /** select query and convert to given type */
    public <T> Optional<T> executeSelectQueryFirstRow(String sql, Function<Map<String, Object>, T> convertMethod) {
        return executeSelectQuery(sql, EMPTY_OBJECT_TAB, convertMethod, 1).stream().findFirst();
    }

    /** select query and convert to given type */
    public <T> boolean executeSelectQueryFirstExists(String sql, Function<Map<String, Object>, T> convertMethod) {
        return executeSelectQuery(sql, EMPTY_OBJECT_TAB, convertMethod, 1).stream().findFirst().isPresent();
    }

    /** execute SELECT query and convert from map to given type */
    public <T> Optional<T> executeSelectQueryOptional(String sql, Object[] params, Function<Map<String, Object>, Optional<T>> convertMethod, int maxRows) {
        return executeSelectQuery(sql, params, convertMethod, maxRows).stream().flatMap(x -> x.stream()).findFirst();
    }

    /** execute SELECT query and convert from map to given type */
    public <T> Optional<T> executeSelectQueryOptional(String sql, Object[] params, Function<Map<String, Object>, Optional<T>> convertMethod) {
        return executeSelectQueryOptional(sql, params, convertMethod, Integer.MAX_VALUE);
    }

    /** get all rows for given model */
    public <X extends BaseRow> LinkedList<X> executeSelectAllModel(DaoModel<X> model, int maxRows) {
        return executeSelectQuery(model.getSelectAllQuery(), new Object[0], map -> model.convert(map), maxRows);
    }

    /** get all rows for given model */
    public <X extends BaseRow> LinkedList<X> executeSelectAllActiveModel(DaoModel<X> model, int maxRows) {
        return executeSelectQuery(model.getSelectAllActiveQuery(), new Object[0], map -> model.convert(map), maxRows);
    }

    /** get all rows for given model */
    public <X extends BaseRow> LinkedList<X> executeSelectAllModel(DaoModel<X> model) {
        return executeSelectAllModel(model, 10000);
    }
    /** get the latest rows for given model */
    public <X extends BaseRow> List<X> executeSelectLatestRowsModel(DaoModel<X> model, int n) {
        return executeSelectQueryResultSet(model.getSelectLimitQuery(n), new Object[0], (colsCount, rs) -> model.fromResultSet(rs), n);
    }
    /** get first row for given model and object name */
    public <X extends BaseRow> Optional<X> executeSelectForNameModelFirst(DaoModel<X> model, String objectName) {
        return executeSelectQuery(model.getSelectForNameQuery(), new Object[] { objectName }, map -> model.convert(map), 1).stream().findFirst();
    }
    /** get first row for given model and object name */
    public <X extends BaseRow> boolean executeSelectForNameModelExists(DaoModel<X> model, String objectName) {
        return executeSelectQuery(model.getSelectForNameQuery(), new Object[] { objectName }, map -> model.convert(map), 1).stream().findFirst().isPresent();
    }
    /** get rows for given model for given object name */
    public <X extends BaseRow> List<X> executeSelectForNameModelAll(DaoModel<X> model, String objectName, int maxRows) {
        return executeSelectQuery(model.getSelectForNameQuery(), new Object[] { objectName }, map -> model.convert(map), maxRows);
    }
    public <X extends BaseRow> List<X> executeSelectForNameModelAll(DaoModel<X> model, String objectName) {
        return executeSelectForNameModelAll(model, objectName, Integer.MAX_VALUE);
    }
    /** */
    public List<Object> executeSelectQuerySingleColumn(String sql, Object[] params, String colName, int maxRows) {
        return executeSelectQuery(sql, params, row -> row.getOrDefault(colName, ""), maxRows);
    }
    /** */
    public List<String> executeSelectQuerySingleColumnString(String sql, Object[] params, String colName, int maxRows) {
        return executeSelectQuery(sql, params, row -> row.getOrDefault(colName, "").toString(), maxRows);
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
    /** insert full object to given table name */
    public <X extends BaseRow> int executeInsertRowForModel(DaoModel<X> model, X obj) {
        return executeUpdateQuery(model.getInsertQuery(), obj.toInsertRow());
    }
    public <X extends BaseRow> int executeInsertRowForModelNoConflict(DaoModel<X> model, X obj) {
        return executeUpdateQuery(model.getInsertQueryNoConflict(), obj.toInsertRow());
    }
    /** insert full object to given table name */
    public <X extends BaseRow> int executeInsertRowsForModel(DaoModel<X> model, List<X> objs) {
        if (objs.isEmpty()) {
            return 0;
        } else {
            String sql = model.getInsertQuery();
            if (sql == null || sql.isEmpty()) {
                return -1;
            } else {
                return withConnection(conn -> {
                    try {
                        int updCnt = 0;
                        log.debug("INSERT QUERY, table: " + model.getTableName() + ", objects count: " + objs.size() + ", sql: " + sql);
                        PreparedStatement st = createStatement(conn, sql);
                        for (X p : objs) {
                            fillStatement(st, p.toInsertRow());
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
    /** test DAO and returns items */
    public Map<String, Object> testDao() {
        return Map.of("isConnected", isConnected(), "url", getUrl(), "className", this.getClass().getName());
    }
    public boolean closeDao() {
        try {
            log.info("Closing DAO JDBC for agent: " + parentAgent.getAgentGuid());
            connPool.close();
            connPool = null;
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
                st.setTimestamp(i, new Timestamp(((java.util.Date) obj).getTime()));
            } else if (obj instanceof LocalDateTime) {
                st.setTimestamp(i, Timestamp.valueOf((LocalDateTime) obj));
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
