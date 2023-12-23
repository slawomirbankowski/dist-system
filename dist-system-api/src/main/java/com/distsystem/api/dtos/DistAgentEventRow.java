package com.distsystem.api.dtos;

import com.distsystem.api.BaseRow;

import java.time.LocalDateTime;
import java.util.Map;

/** */
public class DistAgentEventRow extends BaseRow {

    private final String eventGuid;
    /** date and time of event */
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

    public LocalDateTime getCreatedDateTime() {
        return createdDateTime;
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
        return new Object[] { eventGuid, createdDateTime, parentClass, parentGuid, method, eventType, isActive, createdDate, isActive, lastUpdatedDate };
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
