package com.distsystem.api.info;

import com.distsystem.api.dtos.DistAgentServerRow;

import java.util.List;

/** information class about connectors in Agent: servers and clients */
public class AgentConnectorsInfo {
    private final List<AgentServerInfo> createdServers;
    private final List<DistAgentServerRow> agents;
    private final List<ClientInfo> clients;
    public AgentConnectorsInfo(List<AgentServerInfo> createdServers, List<DistAgentServerRow> agents, List<ClientInfo> clients) {
        this.createdServers = createdServers;
        this.agents = agents;
        this.clients = clients;
    }
    public List<AgentServerInfo> getCreatedServers() {
        return createdServers;
    }
    public List<DistAgentServerRow> getAgents() {
        return agents;
    }
    public List<ClientInfo> getClients() {
        return clients;
    }

    @Override
    public java.lang.String toString() {
        return "SRV,createdServers=" + createdServers + ",serverDefinitions=" + agents.size() + ",clientInfos=" + clients.size();
    }
}

