package com.distsystem.api;

import java.time.LocalDateTime;

/** class representing single method in service can can be called */
public class AgentableEvent {

    private String eventName;
    private String eventParam;
    private LocalDateTime createdDate;

    public AgentableEvent(String eventName, String eventParam) {
        this.eventName = eventName;
        this.eventParam = eventParam;
        createdDate = LocalDateTime.now();
    }
    public String getEventName() {
        return eventName;
    }
    public String getEventParam() {
        return eventParam;
    }
    public String getCreatedDate() {
        return createdDate.toString();
    }
}
