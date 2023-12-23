package com.distsystem.api.dtos;

import com.distsystem.api.BaseRow;
import com.distsystem.api.DaoTable;

import java.time.LocalDateTime;
import java.util.Map;

/**  */
public class DistAgentMonitorCheckRow extends BaseRow {

    private final String monitorName;
    private final String agentGuid;
    private final String checkStatus;
    private final double checkValue;
    private final String checkOutput;
    protected final int isActive;
    protected final LocalDateTime createdDate;
    protected final LocalDateTime lastUpdatedDate;

    public DistAgentMonitorCheckRow(String monitorName, String agentGuid, String checkStatus, double checkValue, String checkOutput, LocalDateTime createdDate) {
        this.monitorName = monitorName;
        this.agentGuid = agentGuid;
        this.checkStatus = checkStatus;
        this.checkValue = checkValue;
        this.checkOutput = checkOutput;
        this.isActive = 1;
        this.createdDate = createdDate;
        this.lastUpdatedDate = createdDate;
    }
    public DistAgentMonitorCheckRow(String monitorName, String agentGuid, String checkStatus, double checkValue, String checkOutput) {
        this.monitorName = monitorName;
        this.agentGuid = agentGuid;
        this.checkStatus = checkStatus;
        this.checkValue = checkValue;
        this.checkOutput = checkOutput;
        this.isActive = 1;
        this.createdDate = LocalDateTime.now();
        this.lastUpdatedDate = createdDate;
    }

    public String getMonitorName() {
        return monitorName;
    }

    public String getAgentGuid() {
        return agentGuid;
    }

    public String getCheckStatus() {
        return checkStatus;
    }

    public double getCheckValue() {
        return checkValue;
    }

    public String getCheckOutput() {
        return checkOutput;
    }

    public int getIsActive() {
        return isActive;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public LocalDateTime getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public Object[] toInsertRow() {
        return new Object[] { monitorName, agentGuid, checkStatus, checkValue, checkOutput, createdDate };
    }

    /** get name of key attribute */
    public static String getKeyAttributeName() {
        return "monitorName";
    }

    /** if key is unique */
    public static Boolean getKeyIsUnique() {
        return false;
    }

    /** if key is unique */
    public static String[] getUpdateColumns() {
        return new String[] { "checkStatus", "checkValue", "checkOutput" };
    }

    public Map<String, String> toMap() {
        return Map.of("", "",
                "monitorName", monitorName,
                "agentGuid", agentGuid,
                "checkStatus", checkStatus,
                "checkValue", ""+checkValue,
                "checkOutput", checkOutput,
                "createdDate", createdDate.toString());
    }

}
