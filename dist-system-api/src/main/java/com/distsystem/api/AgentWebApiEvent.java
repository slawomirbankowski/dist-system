package com.distsystem.api;

import java.time.LocalDateTime;
import java.util.function.BiFunction;

public class AgentWebApiEvent {

    private long requestSeq;
    private String method;
    private String serviceMethod;
    private boolean methodFound;
    private int responseCode;
    private long responseSize;
    private LocalDateTime createdDate;

    public AgentWebApiEvent(long requestSeq, String method, String serviceMethod, boolean methodFound, int responseCode, long responseSize) {
        this.requestSeq = requestSeq;
        this.method = method;
        this.serviceMethod = serviceMethod;
        this.methodFound = methodFound;
        this.responseCode = responseCode;
        this.responseSize = responseSize;
        this.createdDate = LocalDateTime.now();
    }
    public long getRequestSeq() {
        return requestSeq;
    }
    public String getMethod() {
        return method;
    }
    public String getServiceMethod() {
        return serviceMethod;
    }
    public boolean isMethodFound() {
        return methodFound;
    }
    public int getResponseCode() {
        return responseCode;
    }
    public long getResponseSize() {
        return responseSize;
    }
    public String getCreatedDate() {
        return createdDate.toString();
    }
}

