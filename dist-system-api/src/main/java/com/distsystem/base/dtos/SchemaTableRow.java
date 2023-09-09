package com.distsystem.base.dtos;

import java.util.Map;

public class SchemaTableRow {

    public String table_schema;
    public String table_name;

    public SchemaTableRow(String table_schema, String table_name) {
        this.table_schema = table_schema;
        this.table_name = table_name;
    }

    public static SchemaTableRow fromMap(Map<String, Object> map) {
        return new SchemaTableRow(map.getOrDefault("table_schema", "").toString(),
                map.getOrDefault("table_name", "").toString());
    }
}
