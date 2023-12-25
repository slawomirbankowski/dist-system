package com.distsystem.api;

import com.distsystem.api.dtos.*;
import com.distsystem.utils.DistUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

/** model for DAO storage representing one entity */
public class DaoModel<T extends BaseRow> {
    protected static final Logger log = LoggerFactory.getLogger(DaoModel.class);

    /** class of this entity that should be serialized into single row*/
    private final Class<T> modelClass;
    /** name of table or storage object to keep these rows */
    private final String tableName;
    /** name of key column that should be unique to get objects */
    private final String keyName;

    /** JDBC SQL query to get all rows from DB table */
    private final String selectAllQuery;
    private final String selectAllActiveQuery;
    private final String selectAllDescQuery;
    private final String selectByNameQuery;
    private final String insertQuery;
    private final String insertQueryNoConflict;
    private final String updateByKeyQuery;

    private boolean keyIsUnique;
    private final String deleteOldQuery;
    private final String deleteByKeyQuery;
    private final List<String> columns;
    private final Map<String, String> columnTypes;
    private final String columnList;

    public DaoModel(Class<T> modelClass) {
        this.modelClass = modelClass;
        this.columns = calculateColumns(modelClass);
        this.columnTypes = Map.of();
        this.columnList = columns.stream().collect(Collectors.joining(","));
        DaoTable dt = modelClass.getAnnotation(DaoTable.class);
        if (dt == null) {
            this.tableName = calculateTableName();
            this.keyName = calculateKeyName();
            this.keyIsUnique = calculateKeyIsUnique();
        } else {
            log.info("dt: " + dt);
            this.tableName = dt.tableName().toLowerCase();
            this.keyName = dt.keyName().toLowerCase();
            this.keyIsUnique = dt.keyIsUnique();
        }
        log.info("DaoModel: " + modelClass.getName() + ", table: " + tableName + ", key: " + keyName);
        this.selectAllQuery = "select * from " + tableName;
        this.selectAllDescQuery = selectAllQuery + " order by createdDate desc";
        this.selectAllActiveQuery = "select * from " + tableName + " where isActive=1";
        this.selectByNameQuery = "select * from " + tableName + " where " + keyName + "=?";
        String questionList = columns.stream().map(x -> "?").collect(Collectors.joining(","));
        this.insertQuery = "insert into " + tableName + "(" + columnList + ") values (" + questionList + ")";
        this.insertQueryNoConflict = insertQuery + " on conflict do nothing";
        this.updateByKeyQuery = "update " + tableName + " set columns...=? where " + keyName + "=?";
        this.deleteOldQuery = "delete from " + tableName + " where createdDate<?";
        this.deleteByKeyQuery = "delete from " + tableName + " where " + keyName + "=?";
    }
    /** calculate list of columns based on declared fields for DTO class */
    private List<String> calculateColumns(Class<T> cl) {
        return Arrays.stream(cl.getDeclaredFields()).map(f -> f.getName()).toList();
    }

    /** get all columns for model */
    public List<String> getColumns() {
        return columns;
    }
    public String generateInsertMethod() {
        StringBuilder b = new StringBuilder();
        b.append("/** insert row for " + modelClass.getSimpleName() + " */\n");
        b.append("public Object[] toInsertRow() {\n");
        b.append("  return new Object[] {");
        b.append(getColumns().stream().collect(Collectors.joining(",")));
        b.append("};\n}\n");
        return b.toString();
    }
    public String generateToMapMethod() {
        StringBuilder b = new StringBuilder();
        b.append("/** create map for " + modelClass.getSimpleName() + " */\n");
        b.append("public Map<String, String> toMap() {\n");
        b.append("  return Map.of(");

        b.append(getColumns().stream().map(c -> "" + c+ "" + c + "").collect(Collectors.joining(",")));
        b.append(");\n}\n");
        return b.toString();
    }
    // DistUtils.createMap()

    public String getColumnList() {
        return columnList;
    }
    public String getKeyName() {
        return keyName;
    }
    public String getTableName() {
        return tableName;
    }
    public Class<T> getModelClass() {
        return modelClass;
    }

    /** get SQL query to select all rows */
    public String getSelectAllQuery() {
        return selectAllQuery;
    }

    /** get SQL query to select all rows */
    public String getSelectAllActiveQuery() {
        return selectAllActiveQuery;
    }
    public String getSelectAllDescQuery() {
        return selectAllDescQuery;
    }
    public String getSelectLimitQuery(int n) {
        return selectAllQuery + " limit " + n;
    }
    public String getSelectLimitDescQuery(int n) {
        return selectAllDescQuery + " limit " + n;
    }
    public String getInsertQuery() {
        return insertQuery;
    }
    public String getInsertQueryNoConflict() {
        return insertQueryNoConflict;
    }
    public String getDeleteOldQuery() {
        return deleteOldQuery;
    }
    /** get SQL query for name query */
    public String getSelectForNameQuery() {
        return selectByNameQuery;
    }
    /** */
    public String getCreateTableQuery() {
        String columnsSql = Arrays.stream(modelClass.getDeclaredFields()).map(m -> {
            return m.getName().toLowerCase() + " " + translateType(m.getType().getSimpleName()) + " not null";
        }).collect(Collectors.joining(","));
        return "create table " + tableName + "(" + columnsSql + ")";
    }
    /** generate create index DDL command */
    public String getCreateTableIndexQuery() {
        if (keyName.isEmpty()) {
            return "";
        } else {
            String unq = (keyIsUnique)?"unique":"";
            return "create " + unq + " index idx_" + tableName + "_" + keyName + " on " + tableName + "(" + keyName + ")";
        }
    }
    /** convert row into T object */
    public T convert(Map<String, Object> map) {
        try {
            var m = modelClass.getDeclaredMethod("fromMap", new Class[] { Map.class });
            return (T)m.invoke(null, new Object[] { map });
        } catch (Exception ex) {
            log.info("Cannot invoke fromMap method, reason: " + ex.getMessage(), ex);
            return null;
        }
    }
    /** convert row into T object */
    public T fromResultSet(ResultSet rs) {
        try {
            var m = modelClass.getDeclaredMethod("fromResultSet", new Class[] { ResultSet.class });
            return (T)m.invoke(null, new Object[] { rs });
        } catch (Exception ex) {
            log.info("Cannot invoke fromResultSet method, reason: " + ex.getMessage(), ex);
            return null;
        }
    }
    /** calculate key name based on static method getKeyAttributeName or get first attribute from class */
    private String calculateKeyName() {
        try {
            var m = modelClass.getDeclaredMethod("getKeyAttributeName", new Class[0]);
            return (String)m.invoke(null, new Object[0]);
        } catch (Exception ex) {
            if (!columns.isEmpty()) {
                return getColumns().get(0);
            } else {
                return "";
            }
        }
    }
    /** calculate table name with model class from static method or just by name of class */
    private String calculateTableName() {
        try {
            var m = modelClass.getDeclaredMethod("getTableName", new Class[0]);
            return (String)m.invoke(null, new Object[0]);
        } catch (Exception ex) {
            String clName =modelClass.getSimpleName().toLowerCase();
            if (clName.endsWith("row")) {
                return clName.substring(0, clName.length()-3);
            } else {
                return clName;
            }
        }
    }
    /** calculate key name based on static method getKeyIsUnique or get first attribute from class */
    private boolean calculateKeyIsUnique() {
        try {
            var m = modelClass.getDeclaredMethod("getKeyIsUnique", new Class[0]);
            return (Boolean)m.invoke(null, new Object[0]);
        } catch (Exception ex) {
            return false;
        }
    }
    /**
     /** translate type from Java into SQL */
    private static String translateType(String fieldType) {
        return translatedTypes.getOrDefault(fieldType, "text");
    }

    /** */
    private static final Map<String, DaoModel> currentCreatedModels = new HashMap<>();
    public static <T extends BaseRow> DaoModel<T> getModelForClass(Class<T> cl) {
        DaoModel<T> model = currentCreatedModels.get(cl.getName());
        if (model == null) {
            model = new DaoModel<T>(cl);
            currentCreatedModels.put(cl.getName(), model);
        }
        return model;
    }
    public static final DaoModel<DistAgentAuthAccountRow> authAccount = new DaoModel<>(DistAgentAuthAccountRow.class);
    public static final DaoModel<DistAgentAuthDomainRow> authDomain = new DaoModel<>(DistAgentAuthDomainRow.class);
    public static final DaoModel<DistAgentAuthIdentityRow> authIdentity = new DaoModel<>(DistAgentAuthIdentityRow.class);
    public static final DaoModel<DistAgentAuthKeyRow> authKey = new DaoModel<>(DistAgentAuthKeyRow.class);
    public static final DaoModel<DistAgentAuthRoleRow> authRole = new DaoModel<>(DistAgentAuthRoleRow.class);
    public static final DaoModel<DistAgentAuthTokenParserRow> authTokenParser = new DaoModel<>(DistAgentAuthTokenParserRow.class);

    public static final DaoModel<DistAgentCacheItemRow> cacheItem = new DaoModel<>(DistAgentCacheItemRow.class);
    public static final DaoModel<DistAgentCacheInstanceRow> cacheInstance = new DaoModel<>(DistAgentCacheInstanceRow.class);
    public static final DaoModel<DistAgentConfigInitRow> configInit = new DaoModel<>(DistAgentConfigInitRow.class);
    public static final DaoModel<DistAgentConfigRow> config = new DaoModel<>(DistAgentConfigRow.class);
    public static final DaoModel<DistAgentDaoRow> dao = new DaoModel<>(DistAgentDaoRow.class);
    public static final DaoModel<DistAgentEventRow> event = new DaoModel<>(DistAgentEventRow.class);
    public static final DaoModel<DistAgentIssueRow> issue = new DaoModel<>(DistAgentIssueRow.class);
    public static final DaoModel<DistAgentMeasureRow> measure = new DaoModel<>(DistAgentMeasureRow.class);
    public static final DaoModel<DistAgentMemoryRow> memory = new DaoModel<>(DistAgentMemoryRow.class);
    public static final DaoModel<DistAgentMonitorCheckRow> monitorCheck = new DaoModel<>(DistAgentMonitorCheckRow.class);
    public static final DaoModel<DistAgentMonitorRow> monitor = new DaoModel<>(DistAgentMonitorRow.class);
    public static final DaoModel<DistAgentNotificationRow> notification = new DaoModel<>(DistAgentNotificationRow.class);
    public static final DaoModel<DistAgentQueryRow> query = new DaoModel<>(DistAgentQueryRow.class);
    public static final DaoModel<DistAgentRegisterRow> register = new DaoModel<>(DistAgentRegisterRow.class);
    public static final DaoModel<DistAgentReportRow> report = new DaoModel<>(DistAgentReportRow.class);
    public static final DaoModel<DistAgentReportRunRow> reportRun = new DaoModel<>(DistAgentReportRunRow.class);
    public static final DaoModel<DistAgentResourceRow> resource = new DaoModel<>(DistAgentResourceRow.class);
    public static final DaoModel<DistAgentScheduleExecutionRow> scheduleExecution = new DaoModel<>(DistAgentScheduleExecutionRow.class);
    public static final DaoModel<DistAgentScheduleRow> schedule = new DaoModel<>(DistAgentScheduleRow.class);
    public static final DaoModel<DistAgentScriptRow> script = new DaoModel<>(DistAgentScriptRow.class);
    public static final DaoModel<DistAgentServerRow> server = new DaoModel<>(DistAgentServerRow.class);
    public static final DaoModel<DistAgentServiceRow> service = new DaoModel<>(DistAgentServiceRow.class);
    public static final DaoModel<DistAgentSettingRow> setting = new DaoModel<>(DistAgentSettingRow.class);
    public static final DaoModel<DistAgentSpaceRow> space = new DaoModel<>(DistAgentSpaceRow.class);
    public static final DaoModel<DistAgentStorageRow> storage = new DaoModel<>(DistAgentStorageRow.class);

    private static List<DaoModel> gatherAllModels() {
        return Arrays.stream(DaoModel.class.getDeclaredFields())
                .filter(f -> f.getType().getSimpleName().equals("DaoModel") && Modifier.isStatic(f.getModifiers()))
                .flatMap(f -> {
                    try {
                        log.debug("Field, name=" + f.getName() + ", m=" + f.getModifiers() + ", t=" + f.getType().getName() + ", TABLE=" + ((DaoModel)f.get(null)).getTableName());
                        return Optional.of(((DaoModel)f.get(null))).stream();
                    } catch (Exception ex) {
                        log.warn("Exception: " + ex.getMessage());
                        return new LinkedList<DaoModel>().stream();
                    }
                }).toList();
    }

    private static final Map<String, String> translatedTypes = DistUtils.createMap(
            "String", "text",
            "short", "int",
            "int", "int",
            "long", "bigint",
            "float", "float",
            "double", "float",
            "LocalDateTime", "timestamp"
    );

}
