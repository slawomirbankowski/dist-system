package com.distsystem.api.dtos;

import com.distsystem.api.BaseRow;
import com.distsystem.api.DaoTable;
import com.distsystem.utils.JsonUtils;

import java.time.LocalDateTime;
import java.util.Map;

/**  */
@DaoTable(tableName="DistAgentResourceRow", keyName="resourceName", keyIsUnique=true)
public class DistAgentResourceRow extends BaseRow {

    private String resourceName;
    private String resourceType;
    private String resourceDescription;
    private Map<String, String> resourceParameters;
    private LocalDateTime createdDate;
    private int isActive;
    private LocalDateTime lastUpdatedDate;

    public DistAgentResourceRow(String resourceName, String resourceType, String resourceDescription, Map<String, String> resourceParameters, LocalDateTime createdDate, int isActive, LocalDateTime lastUpdatedDate) {
        this.resourceName = resourceName;
        this.resourceType = resourceType;
        this.resourceDescription = resourceDescription;
        this.resourceParameters = resourceParameters;
        this.createdDate = createdDate;
        this.isActive = isActive;
        this.lastUpdatedDate = lastUpdatedDate;
    }
    public DistAgentResourceRow(String resourceName, String resourceType, String resourceDescription, Map<String, String> resourceParameters) {
        this.resourceName = resourceName;
        this.resourceType = resourceType;
        this.resourceDescription = resourceDescription;
        this.resourceParameters = resourceParameters;
        this.createdDate = LocalDateTime.now();
        this.isActive = 1;
        this.lastUpdatedDate = createdDate;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceDescription() {
        return resourceDescription;
    }

    public Map<String, String> getResourceParameters() {
        return resourceParameters;
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
        return new Object[] { resourceName, resourceType, resourceDescription, resourceParameters, createdDate, isActive, lastUpdatedDate };
    }
    public Map<String, String> toMap() {
        return Map.of("type", "DistAgentResourceRow",
                "resourceName", resourceName,
                "resourceType", resourceType,
                "resourceDescription", resourceDescription,
                "resourceParameters", JsonUtils.serialize(resourceParameters),
                "createdDate", createdDate.toString(),
                "isActive", ""+isActive,
                "lastUpdatedDate", lastUpdatedDate.toString());
    }

}
