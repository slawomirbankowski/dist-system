package com.distsystem.api.dtos;

import com.distsystem.api.BaseRow;
import com.distsystem.api.DaoTable;
import com.distsystem.utils.AdvancedMap;

import java.time.LocalDateTime;
import java.util.Map;

/**  */
public class DistAgentScheduleExecutionRow extends BaseRow {

    private String executionGuid;
    private String scheduleName;
    private String agentGuid;
    private LocalDateTime executionDate;
    private String executionStatus;
    private String executionOutput;
    private long executionTimeMs;
    private int isActive;
    private LocalDateTime createdDate;
    private LocalDateTime lastUpdatedDate;

    public DistAgentScheduleExecutionRow(String executionGuid, String scheduleName, String agentGuid, LocalDateTime executionDate, String executionStatus, String executionOutput, long executionTimeMs, int isActive, LocalDateTime createdDate, LocalDateTime lastUpdatedDate) {
        this.executionGuid = executionGuid;
        this.scheduleName = scheduleName;
        this.agentGuid = agentGuid;
        this.executionDate = executionDate;
        this.executionStatus = executionStatus;
        this.executionOutput = executionOutput;
        this.executionTimeMs = executionTimeMs;
        this.isActive = isActive;
        this.createdDate = createdDate;
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
        this.isActive = 1;
        this.createdDate = LocalDateTime.now();
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
        return new Object[] { executionGuid, scheduleName, agentGuid, executionDate, executionStatus, executionOutput, executionTimeMs, isActive, createdDate, lastUpdatedDate };
    }
    // String executionGuid, String scheduleName, String agentGuid, LocalDateTime executionDate, String executionStatus,
    // String executionOutput, long executionTimeMs,
    // int isActive, LocalDateTime createdDate, LocalDateTime lastUpdatedDate
    public static DistAgentScheduleExecutionRow fromMap(Map<String, Object> map) {
        AdvancedMap m = new AdvancedMap(map, true);
        return new DistAgentScheduleExecutionRow(
                m.getStringOrEmpty("executionGuid"),
                m.getStringOrEmpty("scheduleName"),
                m.getStringOrEmpty("agentGuid"),
                m.getLocalDateOrNow("executionDate"),
                m.getStringOrEmpty("executionStatus"),
                m.getStringOrEmpty("executionOutput"),
                m.getLongOrZero("executionTimeMs"),
                m.getInt("isActive", 1),
                m.getLocalDateOrNow("createdDate"),
                m.getLocalDateOrNow("lastUpdatedDate")
        );
    }
    /** */
    public Map<String, String> toMap() {
        return Map.of("type", "DistAgentScheduleExecutionRow",
                "executionGuid", executionGuid,
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
