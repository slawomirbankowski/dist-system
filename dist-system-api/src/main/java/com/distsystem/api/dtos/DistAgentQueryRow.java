package com.distsystem.api.dtos;

import com.distsystem.api.BaseRow;
import com.distsystem.api.DaoTable;

import java.time.LocalDateTime;
import java.util.Map;

/**  */
@DaoTable(tableName="DistAgentQuery", keyName="queryName", keyIsUnique=true)
public class DistAgentQueryRow extends BaseRow {

    private final String queryName;
    private String queryDefinition;
    private String queryParameters;
    private int isActive;
    private LocalDateTime createdDate;
    private LocalDateTime lastUpdatedDate;

    public DistAgentQueryRow(String queryName, String queryDefinition, String queryParameters, LocalDateTime createdDate, int isActive, LocalDateTime lastUpdatedDate) {
        this.queryName = queryName;
        this.queryDefinition = queryDefinition;
        this.queryParameters = queryParameters;
        this.isActive = isActive;
        this.createdDate = createdDate;
        this.lastUpdatedDate = lastUpdatedDate;
    }
    public DistAgentQueryRow(String queryName, String queryDefinition, String queryParameters) {
        this.queryName = queryName;
        this.queryDefinition = queryDefinition;
        this.queryParameters = queryParameters;
        this.isActive = 1;
        this.createdDate = LocalDateTime.now();
        this.lastUpdatedDate = createdDate;
    }

    public String getQueryName() {
        return queryName;
    }

    public String getQueryDefinition() {
        return queryDefinition;
    }

    public String getQueryParameters() {
        return queryParameters;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public int getIsActive() {
        return isActive;
    }

    public LocalDateTime getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public Object[] toInsertRow() {
        return new Object[] { queryName, queryDefinition, queryParameters, isActive, createdDate, lastUpdatedDate };
    }
    public Map<String, String> toMap() {
        return Map.of("queryName", queryName,
                "queryDefinition", queryDefinition,
                "queryParameters", queryParameters,
                "createdDate", createdDate.toString(),
                "isActive",""+isActive,
                "lastUpdatedDate", lastUpdatedDate.toString());
    }

}
