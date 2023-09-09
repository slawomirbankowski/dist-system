package com.distsystem.api.info;

import java.util.List;

/** information about all possible Web APIs for Agent */
public class AgentApisInfo {
    boolean closed;
    private List<AgentApiInfo> apis;
    public AgentApisInfo(boolean closed, List<AgentApiInfo> apis) {
        this.closed = closed;
        this.apis = apis;
    }
    /** */
    public boolean isClosed() {
        return closed;
    }
    public List<AgentApiInfo> getApis() {
        return apis;
    }
}
