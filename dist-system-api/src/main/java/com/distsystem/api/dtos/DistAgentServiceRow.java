package com.distsystem.api.dtos;

import com.distsystem.api.BaseRow;
import com.distsystem.api.DaoTable;

import java.time.LocalDateTime;
import java.util.Map;

/**  */
@DaoTable(tableName="DistAgentService", keyName="serviceGuid", keyIsUnique=true)
public class DistAgentServiceRow extends BaseRow {

    private String agentGuid;
    private String serviceGuid;
    private String serviceType;
    String serviceInfoJson;
    private LocalDateTime createdDate;
    private int isActive;
    private LocalDateTime lastPingDate;
    private LocalDateTime lastUpdatedDate;

    public DistAgentServiceRow(String agentGuid, String serviceGuid, String serviceType, String serviceInfoJson, LocalDateTime createdDate, int isActive, LocalDateTime lastPingDate) {
        this.agentGuid = agentGuid;
        this.serviceGuid = serviceGuid;
        this.serviceType = serviceType;
        this.serviceInfoJson = serviceInfoJson;
        this.createdDate = createdDate;
        this.isActive = isActive;
        this.lastPingDate = lastPingDate;
        this.lastUpdatedDate = createdDate;
    }

    public String getAgentGuid() {
        return agentGuid;
    }

    public String getServiceGuid() {
        return serviceGuid;
    }
    public Object[] toObjectRow() {
        return new Object[] {agentGuid, serviceGuid, serviceType, createdDate, 1L, lastPingDate, lastUpdatedDate};
    }

    public String getServiceType() {
        return serviceType;
    }

    public String getCreatedDate() {
        return createdDate.toString();
    }

    public int getIsActive() {
        return isActive;
    }

    public String getLastPingDate() {
        return lastPingDate.toString();
    }

    public String getServiceInfoJson() {
        return serviceInfoJson;
    }

    public void setServiceInfoJson(String serviceInfoJson) {
        this.serviceInfoJson = serviceInfoJson;
    }

    public String getLastUpdatedDate() {
        return lastUpdatedDate.toString();
    }

    public Object[] toInsertRow() {
        return new Object[] { agentGuid, serviceGuid, serviceType, serviceInfoJson, createdDate, isActive, lastPingDate, lastUpdatedDate };
    }
    public Map<String, String> toMap() {
        return Map.of("type", "service",
                "agentguid", agentGuid,
                "serviceguid", serviceGuid,
                "servicetype", serviceType,
                "serviceInfoJson", serviceInfoJson,
                "isactive", "" + isActive,
                "createddate", createdDate.toString(),
                "lastpingdate", lastPingDate.toString());
    }

    /** get name of key attribute */
    public static String getKeyAttributeName() {
        return "serviceGuid";
    }
}
