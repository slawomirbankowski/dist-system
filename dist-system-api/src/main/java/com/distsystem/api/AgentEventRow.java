package com.distsystem.api;

import java.time.LocalDateTime;

/** */
public class AgentEventRow {

    /** date and time of event */
    private final LocalDateTime createdDateTime;
    private final String parentClass;
    private final String method;
    private final String eventType;

    public AgentEventRow(LocalDateTime createdDateTime, String parentClass, String method, String eventType) {
        this.createdDateTime = createdDateTime;
        this.parentClass = parentClass;
        this.method = method;
        this.eventType = eventType;
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

}
