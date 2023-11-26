package com.distsystem.api.dtos;

import com.distsystem.api.BaseRow;
import com.distsystem.api.DaoTable;

import java.time.LocalDateTime;
import java.util.Map;

/**  */
@DaoTable(tableName="DistAgentScheduleExecution", keyName="executionGuid", keyIsUnique=true)
public class DistAgentScheduleExecutionRow extends BaseRow {

    private String executionGuid;
    private String scheduleName;
    private String agentGuid;
    private LocalDateTime executionDate;
    private String executionStatus;
    private String executionOutput;
    private long executionTimeMs;
    private LocalDateTime createdDate;
    private int isActive;
    private LocalDateTime lastUpdatedDate;

    public DistAgentScheduleExecutionRow(String executionGuid, String scheduleName, String agentGuid, LocalDateTime executionDate, String executionStatus, String executionOutput, long executionTimeMs, LocalDateTime createdDate, int isActive, LocalDateTime lastUpdatedDate) {
        this.executionGuid = executionGuid;
        this.scheduleName = scheduleName;
        this.agentGuid = agentGuid;
        this.executionDate = executionDate;
        this.executionStatus = executionStatus;
        this.executionOutput = executionOutput;
        this.executionTimeMs = executionTimeMs;
        this.createdDate = createdDate;
        this.isActive = isActive;
        this.lastUpdatedDate = lastUpdatedDate;
    }
    public DistAgentScheduleExecutionRow(String executionGuid, String scheduleName, String agentGuid, LocalDateTime executionDate, String executionStatus, String executionOutput, long executionTimeMs) {
        this.executionGuid = executionGuid;
        this.scheduleName = scheduleName;
        this.agentGuid = agentGuid;
        this.executionDate = executionDate;
        this.executionStatus = executionStatus;
        this.executionOutput = executionOutput;
        this.executionTimeMs = executionTimeMs;
        this.createdDate = LocalDateTime.now();
        this.isActive = 1;
        this.lastUpdatedDate = createdDate;
    }

    public String getExecutionGuid() {
        return executionGuid;
    }
    public String getScheduleName() {
        return scheduleName;
    }
    public String getAgentGuid() {
        return agentGuid;
    }
    public LocalDateTime getExecutionDate() {
        return executionDate;
    }
    public String getExecutionStatus() {
        return executionStatus;
    }
    public String getExecutionOutput() {
        return executionOutput;
    }
    public long getExecutionTimeMs() {
        return executionTimeMs;
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
        return new Object[] { executionGuid, scheduleName, agentGuid, executionDate, executionStatus, executionOutput, executionTimeMs, createdDate, isActive, lastUpdatedDate };
    }
    /** */
    public Map<String, String> toMap() {
        return Map.of("type", "DistAgentScheduleExecutionRow",
                "scheduleName", scheduleName,
                "agentGuid", agentGuid,
                "executionDate", executionDate.toString(),
                "executionStatus", executionStatus,
                "executionOutput", executionOutput,
                "executionTimeMs", ""+executionTimeMs,
                "createdDate", createdDate.toString(),
                "isActive", ""+isActive,
                "lastUpdatedDate", lastUpdatedDate.toString());
    }

}
