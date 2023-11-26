package com.distsystem.api;

import com.distsystem.api.dtos.*;
import com.distsystem.utils.DistUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DaoModel<T extends BaseRow> {
    protected static final Logger log = LoggerFactory.getLogger(DaoModel.class);

    private final Class<T> modelClass;
    private final String tableName;
    private final String keyName;

    private final String selectAllQuery;
    private final String selectAllDescQuery;
    private final String selectByNameQuery;
    private final String insertQuery;
    private final String updateByKeyQuery;

    private boolean keyIsUnique;
    private final String deleteOldQuery;
    private final List<String> columns;
    private final String columnList;

    public DaoModel(Class<T> modelClass) {
        this.modelClass = modelClass;
        log.info("modelClass: " + modelClass.getName());
        DaoTable dt = modelClass.getAnnotation(DaoTable.class);
        if (dt == null) {
            this.tableName = modelClass.getSimpleName().toLowerCase();

            this.keyName = calculateKeyName();
            this.keyIsUnique = false;
        } else {
            log.info("dt: " + dt);
            this.tableName = dt.tableName().toLowerCase();

            this.keyName = dt.keyName().toLowerCase();
            this.keyIsUnique = dt.keyIsUnique();
        }
        log.info("DaoModel: " + modelClass.getName() + ", table: " + tableName + ", key: " + keyName);
        this.selectAllQuery = "select * from " + tableName;
        this.selectAllDescQuery = selectAllQuery + " order by createdDate desc";
        this.selectByNameQuery = "select * from " + tableName + " where " + keyName + "=?";
        this.columns = calculateColumns(modelClass);
        this.columnList = columns.stream().collect(Collectors.joining(","));
        String questionList = columns.stream().map(x -> "?").collect(Collectors.joining(","));
        this.insertQuery = "insert into " + tableName + "(" + columnList + ") values (" + questionList + ")";
        this.updateByKeyQuery = "";
        this.deleteOldQuery = "delete from  " + tableName + " where createdDate<?";
    }
    /** calculate list of columns based on declared fields for DTO class */
    private List<String> calculateColumns(Class<T> cl) {
        return Arrays.stream(cl.getDeclaredFields()).map(f -> f.getName()).toList();
    }
    /** get all columns for model */
    public List<String> getColumns() {
        return columns;
    }
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
    /** calculate key name based on static method getKeyName */
    private String calculateKeyName() {
        try {
            var m = modelClass.getDeclaredMethod("getKeyName", new Class[0]);
            return (String)m.invoke(null, new Object[0]);
        } catch (Exception ex) {
            return "";
        }
    }

    /** translate type from Java into SQL */
    private static String translateType(String fieldType) {
        return translatedTypes.getOrDefault(fieldType, "text");
    }

    public static final DaoModel<DistAgentAuthAccountRow> authAccount = new DaoModel<DistAgentAuthAccountRow>(DistAgentAuthAccountRow.class);
    public static final DaoModel<DistAgentDaoRow> dao = new DaoModel<DistAgentDaoRow>(DistAgentDaoRow.class);

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
