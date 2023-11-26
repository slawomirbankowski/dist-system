package com.distsystem.api;

import com.distsystem.api.enums.DistServiceType;

import java.time.LocalDateTime;
import java.util.Map;

/** class to keep information about Dist Service.
 * There could be many services in Dist System connected through Agents:
 * report, storage, space, remove, ...
 * */
public class DistServiceInfo {
    private final DistServiceType serviceType;
    private final String serviceClass;
    private final String serviceGuid;
    private final LocalDateTime createdDateTime;
    private final LocalDateTime lastTouchDate;
    private final boolean closed;
    private final boolean initialized;
    private final Map<String, Long> counters;
    private final Map<String, String> customAttributes;
    /** custom info */
    private final Object info;

    /** */
    public DistServiceInfo(DistServiceType serviceType, String serviceClass, String serviceGuid, LocalDateTime createdDateTime, LocalDateTime lastTouchDate, boolean closed, boolean initialized, Map<String, Long> counters, Map<String, String> customAttributes, Object info) {
        this.serviceType = serviceType;
        this.serviceClass = serviceClass;
        this.serviceGuid = serviceGuid;
        this.createdDateTime = createdDateTime;
        this.lastTouchDate = lastTouchDate;
        this.closed = closed;
        this.initialized = initialized;
        this.counters = counters;
        this.customAttributes = customAttributes;
        this.info = info;
    }

    public DistServiceType getServiceType() {
        return serviceType;
    }
    public String getServiceTypeName() {
        return serviceType.name();
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
    public Map<String, Long> getCounters() {
        return counters;
    }
    public Object getInfo() {
        return info;
    }
    public Map<String, String> getCustomAttributes() {
        return customAttributes;
    }

    @Override
    public java.lang.String toString() {
        return "SRV,type=" + serviceType + ",class=" + serviceClass + ",guid=" + serviceGuid;
    }
}
