package com.distsystem.api.dtos;

import com.distsystem.api.BaseRow;

import java.time.LocalDateTime;
import java.util.Map;

/** */
public class DistAgentEventRow extends BaseRow {

    private final String eventGuid;
    /** */
    private final LocalDateTime createdDateTime;
    private final String parentClass;
    private final String parentGuid;
    private final String method;
    private final String eventType;
    private final String hostName;
    private final int isActive;
    /** date and time of event */
    private final LocalDateTime createdDate;
    private final LocalDateTime lastUpdatedDate;

    public DistAgentEventRow(String eventGuid, LocalDateTime createdDateTime, String parentClass, String parentGuid, String method, String eventType, String hostName) {
        this.eventGuid = eventGuid;
        this.createdDateTime = createdDateTime;
        this.parentClass = parentClass;
        this.parentGuid = parentGuid;
        this.method = method;
        this.eventType = eventType;
        this.hostName = hostName;
        this.isActive = 1;
        this.createdDate = createdDateTime;
        this.lastUpdatedDate = createdDate;
    }

    public String getEventGuid() {
        return eventGuid;
    }

    public String getParentGuid() {
        return parentGuid;
    }

    public String getHostName() {
        return hostName;
    }

    public int getIsActive() {
        return isActive;
    }

    public String getCreatedDate() {
        return createdDate.toString();
    }

    public String getLastUpdatedDate() {
        return lastUpdatedDate.toString();
    }

    public String getCreatedDateTime() {
        return createdDateTime.toString();
    }
    public String getParentClass() {
        return parentClass;
    }
    public String getMethod() {
        return method;
    }
    public String getEventType() {
        return eventType;
    }

    public Object[] toInsertRow() {
        return new Object[] { eventGuid, createdDateTime, parentClass, parentGuid, method, eventType, hostName, isActive, createdDate, lastUpdatedDate };
    }

    public Map<String, String> toMap() {
        return Map.of("type", "DistAgentDaoRow",
                "eventGuid", eventGuid,
                "createdDateTime", createdDateTime.toString(),
                "parentClass", parentClass,
                "parentGuid", parentGuid,
                "method", method,
                "createdDate", createdDate.toString(),
                "lastUpdatedDate", lastUpdatedDate.toString());
    }

    /** get name of key attribute */
    public static String getKeyAttributeName() {
        return "eventGuid";
    }
}
