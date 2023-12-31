package com.distsystem.api.dtos;

import com.distsystem.api.BaseRow;
import com.distsystem.api.DaoTable;

import java.time.LocalDateTime;
import java.util.Map;

/**  */
public class DistAgentMonitorRow extends BaseRow {

    private String monitorName;
    private String monitorType;
    private String monitorUrl;
    private String monitorParams;
    private int isActive;
    private LocalDateTime createdDate;
    private LocalDateTime lastUpdatedDate;

    public DistAgentMonitorRow(String monitorName, String monitorType, String monitorUrl, String monitorParams, LocalDateTime createdDate, int isActive, LocalDateTime lastUpdatedDate) {
        this.monitorName = monitorName;
        this.monitorType = monitorType;
        this.monitorUrl = monitorUrl;
        this.monitorParams = monitorParams;
        this.isActive = isActive;
        this.createdDate = createdDate;
        this.lastUpdatedDate = lastUpdatedDate;
    }
    public DistAgentMonitorRow(String monitorName, String monitorType, String monitorUrl, String monitorParams) {
        this.monitorName = monitorName;
        this.monitorType = monitorType;
        this.monitorUrl = monitorUrl;
        this.monitorParams = monitorParams;
        this.isActive = 1;
        this.createdDate = LocalDateTime.now();
        this.lastUpdatedDate = createdDate;
    }

    public String getMonitorName() {
        return monitorName;
    }

    public String getMonitorType() {
        return monitorType;
    }

    public String getMonitorUrl() {
        return monitorUrl;
    }

    public String getMonitorParams() {
        return monitorParams;
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
        return new Object[] { monitorName, monitorType, monitorUrl, monitorParams, isActive, createdDate, lastUpdatedDate };
    }
    public Map<String, String> toMap() {
        return Map.of("monitorName", monitorName,
                "monitorType", monitorType,
                "monitorUrl", monitorUrl,
                "monitorParams", monitorParams,
                "isActive", ""+isActive,
                "createdDate", createdDate.toString(),
                "lastUpdatedDate", lastUpdatedDate.toString());
    }

}
