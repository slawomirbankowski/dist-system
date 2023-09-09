package com.distsystem.api;

import com.distsystem.api.enums.DistServiceType;

import java.time.LocalDateTime;
import java.util.Map;

/** class to keep information about Dist Service.
 * There could be many services in Dist System connected through Agents:
 * report, storage, space, remove, ...
 * */
public class DistServiceInfo {
    private DistServiceType serviceType;
    private String serviceClass;
    private String serviceGuid;
    private LocalDateTime createdDateTime;
    private boolean closed;
    private Map<String, String> customAttributes;

    /** */
    public DistServiceInfo(DistServiceType serviceType, String serviceClass, String serviceGuid, LocalDateTime createdDateTime, boolean closed, Map<String, String> customAttributes) {
        this.serviceType = serviceType;
        this.serviceClass = serviceClass;
        this.serviceGuid = serviceGuid;
        this.createdDateTime = createdDateTime;
        this.closed = closed;
        this.customAttributes = customAttributes;
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

    public boolean isClosed() {
        return closed;
    }

    public Map<String, String> getCustomAttributes() {
        return customAttributes;
    }

    @Override
    public java.lang.String toString() {
        return "SRV,type=" + serviceType + ",class=" + serviceClass + ",guid=" + serviceGuid;
    }
}
