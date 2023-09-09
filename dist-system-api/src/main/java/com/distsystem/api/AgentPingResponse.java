package com.distsystem.api;

/** ping object */
public class AgentPingResponse {
    /** unique ID of this agent */
    public String agentGuid;

    // TODO: add more info to ping response

    public AgentPingResponse() {
    }

    public AgentPingResponse(String agentGuid) {
        this.agentGuid = agentGuid;
    }


}
