package com.distsystem.api;

import com.distsystem.api.info.AgentInfo;
import com.distsystem.api.info.AgentSimpleInfo;
import com.distsystem.api.info.ClientInfo;

import java.io.Serializable;

/** message sending as welcome from server to client to gain more information about Agent on the other side s*/
public class AgentWelcomeMessage implements Serializable {

    /** information about Agent */
    private AgentSimpleInfo agentSimpleInfo;
    /** information about Client */
    private ClientInfo clientInfo;

    public AgentWelcomeMessage(AgentSimpleInfo agentSimpleInfo, ClientInfo clientInfo) {
        this.agentSimpleInfo = agentSimpleInfo;
        this.clientInfo = clientInfo;
    }
    public AgentSimpleInfo getAgentSimpleInfo() {
        return agentSimpleInfo;
    }

    public ClientInfo getClientInfo() {
        return clientInfo;
    }
}
