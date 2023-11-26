package com.distsystem.api.info;

public class AgentApiInfo {

    private final String apiType;
    private final int webApiPort;
    private final long handledRequestsCount;
    private final long handledRequestsTime;
    private final long handledRequestsErrors;

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
