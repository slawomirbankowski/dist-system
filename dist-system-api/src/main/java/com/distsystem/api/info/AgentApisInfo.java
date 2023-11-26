package com.distsystem.api.info;

import java.util.List;

/** information about all possible Web APIs for Agent */
public class AgentApisInfo {
    private final boolean closed;
    private final List<AgentApiInfo> apis;
    private final long openCount;
    private final long checkCount;
    private final long handledRequestsCount;
    private final long handledRequestsTime;
    private final long handledRequestsErrors;


    public AgentApisInfo(boolean closed, long openCount, long checkCount, long handledRequestsCount, long handledRequestsTime, long handledRequestsErrors, List<AgentApiInfo> apis) {
        this.closed = closed;
        this.apis = apis;
        this.openCount = openCount;
        this.checkCount = checkCount;
        this.handledRequestsCount = handledRequestsCount;
        this.handledRequestsTime = handledRequestsTime;
        this.handledRequestsErrors = handledRequestsErrors;
    }
    /** */
    public boolean isClosed() {
        return closed;
    }
    public List<AgentApiInfo> getApis() {
        return apis;
    }
    public long getOpenCount() {
        return openCount;
    }
    public long getCheckCount() {
        return checkCount;
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
