package com.distsystem.api.dtos;

import com.distsystem.api.BaseRow;
import com.distsystem.api.DaoTable;
import com.distsystem.utils.AdvancedMap;

import java.time.LocalDateTime;
import java.util.Map;

/**  */
@DaoTable(tableName="DistAgentSchedule", keyName="scheduleName", keyIsUnique=true)
public class DistAgentScheduleRow extends BaseRow {

    private final String scheduleName;
    private String scheduleType;
    private String scheduleExpression;
    private String scheduleParams;
    private LocalDateTime scheduleStartDate;
    private LocalDateTime scheduleEndDate;
    private LocalDateTime lastRunDate;
    private long runCount;
    private LocalDateTime createdDate;
    private int isActive;
    private LocalDateTime lastUpdatedDate;

    public DistAgentScheduleRow(String scheduleName, String scheduleType, String scheduleExpression, String scheduleParams, LocalDateTime scheduleStartDate, LocalDateTime scheduleEndDate, int isActive, LocalDateTime createdDate, LocalDateTime lastUpdatedDate) {
        this.scheduleName = scheduleName;
        this.scheduleType = scheduleType;
        this.scheduleExpression = scheduleExpression;
        this.scheduleParams = scheduleParams;
        this.scheduleStartDate = scheduleStartDate;
        this.scheduleEndDate = scheduleEndDate;
        this.lastRunDate = LocalDateTime.of(1900, 1, 1, 0, 0);
        this.runCount = 0;
        this.createdDate = createdDate;
        this.isActive = isActive;
        this.lastUpdatedDate = lastUpdatedDate;
    }
    public DistAgentScheduleRow(String scheduleName, String scheduleType, String scheduleExpression, String scheduleParams, LocalDateTime scheduleStartDate, LocalDateTime scheduleEndDate) {
        this.scheduleName = scheduleName;
        this.scheduleType = scheduleType;
        this.scheduleExpression = scheduleExpression;
        this.scheduleParams = scheduleParams;
        this.scheduleStartDate = scheduleStartDate;
        this.scheduleEndDate = scheduleEndDate;
        this.lastRunDate = LocalDateTime.of(1900, 1, 1, 0, 0);
        this.runCount = 0;
        this.createdDate = LocalDateTime.now();
        this.isActive = 1;
        this.lastUpdatedDate = createdDate;
    }
    public Object[] toInsertRow() {
        return new Object[] { scheduleName, scheduleType, scheduleExpression, scheduleParams,
                scheduleStartDate, scheduleEndDate, lastRunDate, runCount, createdDate, isActive, lastUpdatedDate };
    }
    public Map<String, String> toMap() {
        return Map.of("scheduleName", scheduleName,
                "scheduleType", scheduleType,
                "scheduleExpression", scheduleExpression,
                "scheduleParams", scheduleParams,
                "scheduleStartDate", scheduleStartDate.toString(),
                "scheduleEndDate", scheduleEndDate.toString(),
                "createdDate", createdDate.toString(),
                "isActive", ""+isActive,
                "lastUpdatedDate", lastUpdatedDate.toString());
    }

    public static DistAgentScheduleRow fromMap(Map<String, Object> map) {
        AdvancedMap m = new AdvancedMap(map, true);
        return new DistAgentScheduleRow(
                m.getStringOrEmpty("scheduleName"),
                m.getStringOrEmpty("scheduleType"),
                m.getStringOrEmpty("scheduleExpression"),
                m.getStringOrEmpty("scheduleParams"),
                m.getLocalDateOrNow("scheduleStartDate"),
                m.getLocalDateOrNow("scheduleEndDate"),
                m.getInt("isActive", 1),
                m.getLocalDateOrNow("createdDate"),
                m.getLocalDateOrNow("lastUpdatedDate")
        );
    }
    public String getScheduleName() {
        return scheduleName;
    }

    public String getScheduleType() {
        return scheduleType;
    }

    public String getScheduleExpression() {
        return scheduleExpression;
    }

    public String getScheduleParams() {
        return scheduleParams;
    }

    public LocalDateTime getScheduleStartDate() {
        return scheduleStartDate;
    }

    public LocalDateTime getScheduleEndDate() {
        return scheduleEndDate;
    }

    public LocalDateTime getLastRunDate() {
        return lastRunDate;
    }

    public long getRunCount() {
        return runCount;
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


    /** get name of key attribute */
    public static String getKeyAttributeName() {
        return "scheduleName";
    }

}
