package com.distsystem.api.dtos;

import com.distsystem.api.BaseRow;
import com.distsystem.api.DaoTable;
import com.distsystem.utils.AdvancedMap;
import com.distsystem.utils.DistUtils;
import com.distsystem.utils.JsonUtils;
import org.json4s.JsonUtil;

import java.time.LocalDateTime;
import java.util.Map;

/**  */
public class DistAgentResourceRow extends BaseRow {

    private String resourceName;
    private String resourceType;
    private String resourceDescription;
    private String resourceParameters;
    private int isActive;
    private LocalDateTime createdDate;
    private LocalDateTime lastUpdatedDate;

    public DistAgentResourceRow(String resourceName, String resourceType, String resourceDescription, String resourceParameters, int isActive, LocalDateTime createdDate, LocalDateTime lastUpdatedDate) {
        this.resourceName = resourceName;
        this.resourceType = resourceType;
        this.resourceDescription = resourceDescription;
        this.resourceParameters = resourceParameters;
        this.isActive = isActive;
        this.createdDate = createdDate;
        this.lastUpdatedDate = lastUpdatedDate;
    }
    public DistAgentResourceRow(String resourceName, String resourceType, String resourceDescription, String resourceParameters) {
        this.resourceName = resourceName;
        this.resourceType = resourceType;
        this.resourceDescription = resourceDescription;
        this.resourceParameters = resourceParameters;
        this.isActive = 1;
        this.createdDate = LocalDateTime.now();
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
        return JsonUtils.deserializeToMap(resourceParameters);
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
        return new Object[] { resourceName, resourceType, resourceDescription, resourceParameters, isActive, createdDate, lastUpdatedDate };
    }

    public static DistAgentResourceRow fromMap(Map<String, Object> map) {
        AdvancedMap m = new AdvancedMap(map, true);
        return new DistAgentResourceRow(
                m.getString("resourceName", ""),
                m.getString("resourceType", ""),
                m.getString("resourceDescription", ""),
                m.getString("resourceParameters", "{}"),
                m.getInt("isActive", 1),
                m.getLocalDateOrNow("createdDate"),
                m.getLocalDateOrNow("lastUpdatedDate")
        );
    }
    public Map<String, String> toMap() {
        return Map.of("type", "DistAgentResourceRow",
                "resourceName", resourceName,
                "resourceType", resourceType,
                "resourceDescription", resourceDescription,
                "resourceParameters", resourceParameters,
                "createdDate", createdDate.toString(),
                "isActive", ""+isActive,
                "lastUpdatedDate", lastUpdatedDate.toString());
    }

}
