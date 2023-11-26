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
    private LocalDateTime createdDate;
    private int isActive;
    private LocalDateTime lastPingDate;
    private LocalDateTime lastUpdatedDate;

    /** */
    public DistAgentServiceRow() {
    }

    public DistAgentServiceRow(String agentGuid, String serviceGuid, String serviceType, LocalDateTime createdDate, int isActive, LocalDateTime lastPingDate) {
        this.agentGuid = agentGuid;
        this.serviceGuid = serviceGuid;
        this.serviceType = serviceType;
        this.createdDate = createdDate;
        this.isActive = isActive;
        this.lastPingDate = lastPingDate;
    }

    public String getAgentGuid() {
        return agentGuid;
    }

    public String getServiceGuid() {
        return serviceGuid;
    }
    public Object[] getObjectRow() {
        return new Object[] {agentGuid, serviceGuid, serviceType, createdDate, 1L, lastPingDate, lastUpdatedDate};
    }

    public String getServiceType() {
        return serviceType;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public int getIsActive() {
        return isActive;
    }

    public LocalDateTime getLastPingDate() {
        return lastPingDate;
    }


    public LocalDateTime getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public Object[] toInsertRow() {
        return new Object[] { agentGuid, serviceGuid, serviceType, createdDate, isActive, lastPingDate };
    }
    public Map<String, String> toMap() {
        return Map.of("type", "service",
                "agentguid", agentGuid,
                "serviceguid", serviceGuid,
                "servicetype", serviceType,
                "isactive", "" + isActive,
                "createddate", createdDate.toString(),
                "lastpingdate", lastPingDate.toString());
    }

}
