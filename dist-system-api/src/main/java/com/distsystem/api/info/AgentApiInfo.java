package com.distsystem.api.info;

public class AgentApiInfo {

    private String apiType;
    private int webApiPort;
    private long handledRequestsCount;
    private long handledRequestsTime;
    private long handledRequestsErrors;

    public AgentApiInfo(String apiType, int webApiPort, long handledRequestsCount, long handledRequestsTime, long handledRequestsErrors) {
        this.apiType = apiType;
        this.webApiPort = webApiPort;
        this.handledRequestsCount = handledRequestsCount;
        this.handledRequestsTime = handledRequestsTime;
        this.handledRequestsErrors = handledRequestsErrors;
    }

    public String getApiType() {
        return apiType;
    }
    public int getWebApiPort() {
        return webApiPort;
    }
    public long getHandledRequestsCount() {
        return handledRequestsCount;
    }
    public long getHandledRequestsTime() {
        return handledRequestsTime;
    }
    public long getHandledRequestsErrors() {
        return handledRequestsErrors;
    }
}
