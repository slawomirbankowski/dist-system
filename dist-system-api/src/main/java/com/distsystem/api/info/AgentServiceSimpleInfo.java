package com.distsystem.api.info;


import com.distsystem.api.enums.DistServiceType;

import java.time.LocalDateTime;
import java.util.List;

/** information class about Server manager in Agent. */
public class AgentServiceSimpleInfo {
    private final DistServiceType serviceType;
    private final String serviceClass;
    private final String serviceGuid;
    private final LocalDateTime createdDateTime;
    private final LocalDateTime lastTouchDate;
    private final boolean closed;
    private final boolean initialized;

    public AgentServiceSimpleInfo(DistServiceType serviceType, String serviceClass, String serviceGuid, LocalDateTime createdDateTime, LocalDateTime lastTouchDate, boolean closed, boolean initialized) {
        this.serviceType = serviceType;
        this.serviceClass = serviceClass;
        this.serviceGuid = serviceGuid;
        this.createdDateTime = createdDateTime;
        this.lastTouchDate = lastTouchDate;
        this.closed = closed;
        this.initialized = initialized;
    }

    public DistServiceType getServiceType() {
        return serviceType;
    }

    public String getServiceClass() {
        return serviceClass;
    }

    public String getServiceGuid() {
        return serviceGuid;
    }

    public String getCreatedDateTime() {
        return createdDateTime.toString();
    }

    public String getLastTouchDate() {
        return lastTouchDate.toString();
    }

    public boolean isClosed() {
        return closed;
    }

    public boolean isInitialized() {
        return initialized;
    }
}
