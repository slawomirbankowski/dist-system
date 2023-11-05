package com.distsystem.api.info;

import java.util.List;

/** information about all possible Web APIs for Agent */
public class AgentApisInfo {
    private boolean closed;
    private List<AgentApiInfo> apis;
    private long openCount;
    private long checkCount;

    public AgentApisInfo(boolean closed, long openCount, long checkCount, List<AgentApiInfo> apis) {
        this.closed = closed;
        this.apis = apis;
        this.openCount = openCount;
        this.checkCount = checkCount;
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
}
