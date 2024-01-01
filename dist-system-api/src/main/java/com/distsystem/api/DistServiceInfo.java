package com.distsystem.api;

import com.distsystem.api.enums.DistServiceType;
import com.distsystem.api.info.AgentMessageProcessorInfo;
import com.distsystem.api.info.DistWebApiInfo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** class to keep information about Dist Service.
 * There could be many services in Dist System connected through Agents:
 * report, storage, space, remove, ...
 * */
public class DistServiceInfo implements Serializable {
    private final DistServiceType serviceType;
    private final String serviceClass;
    private final String serviceGuid;
    private final String serviceDescription;
    private final LocalDateTime createdDateTime;
    private final LocalDateTime lastTouchDate;
    private final String lastTouchBy;
    private final boolean closed;
    private final boolean initialized;
    private final List<String> componentKeys;
    private final Map<String, Long> counters;
    private final Map<String, String> customAttributes;
    private final DistWebApiInfo webApiInfo;
    /** custom info */
    private final Object info;

    /** */
    public DistServiceInfo(DistServiceType serviceType, String serviceClass, String serviceGuid, String serviceDescription,
                           LocalDateTime createdDateTime, LocalDateTime lastTouchDate, String lastTouchBy,
                           boolean closed, boolean initialized,
                           List<String> componentKeys, Map<String, Long> counters,
                           Map<String, String> customAttributes, DistWebApiInfo webApiInfo,
                           Object info) {
        this.serviceType = serviceType;
        this.serviceClass = serviceClass;
        this.serviceGuid = serviceGuid;
        this.serviceDescription = serviceDescription;
        this.createdDateTime = createdDateTime;
        this.lastTouchDate = lastTouchDate;
        this.lastTouchBy = lastTouchBy;
        this.closed = closed;
        this.initialized = initialized;
        this.componentKeys = componentKeys;
        this.counters = counters;
        this.customAttributes = customAttributes;
        this.webApiInfo = webApiInfo;
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

    public String getServiceDescription() {
        return serviceDescription;
    }

    public String getCreatedDateTime() {
        return createdDateTime.toString();
    }

    public String getLastTouchDate() {
        return lastTouchDate.toString();
    }
    public String getLastTouchBy() {
        return lastTouchBy;
    }

    public boolean isClosed() {
        return closed;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public List<String> getComponentKeys() {
        return componentKeys;
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
    public DistWebApiInfo getWebApiInfo() {
        return webApiInfo;
    }

    @Override
    public java.lang.String toString() {
        return "SRV,type=" + serviceType + ",class=" + serviceClass + ",guid=" + serviceGuid;
    }
}
