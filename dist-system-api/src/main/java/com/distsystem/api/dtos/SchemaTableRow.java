package com.distsystem.api.dtos;

import com.distsystem.api.BaseRow;

import java.time.LocalDateTime;
import java.util.Map;

public class SchemaTableRow extends BaseRow {

    private String table_schema;
    private String table_name;

    public SchemaTableRow(String table_schema, String table_name) {
        this.table_schema = table_schema;
        this.table_name = table_name;
    }
    public String getTable_schema() {
        return table_schema;
    }
    public String getTable_name() {
        return table_name;
    }

    public LocalDateTime getCreatedDate() {
        return LocalDateTime.now();
    }

    public int getIsActive() {
        return 1;
    }

    public LocalDateTime getLastUpdatedDate() {
        return LocalDateTime.now();
    }

    public Object[] toInsertRow() {
        return new Object[] { table_schema, table_name };
    }

    public Map<String, String> toMap() {
        return Map.of("type", "SchemaTableRow",
                "table_schema", table_schema,
                "table_name", table_name);
    }
    public static SchemaTableRow fromMap(Map<String, Object> map) {
        return new SchemaTableRow(map.getOrDefault("table_schema", "").toString(),
                map.getOrDefault("table_name", "").toString());
    }
}
