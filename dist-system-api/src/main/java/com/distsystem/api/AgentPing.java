package com.distsystem.api;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/** ping object */
public class AgentPing {
    /** date ant time of creation for this ping */
    protected final LocalDateTime createDate = LocalDateTime.now();
    private AgentRegister register;
    /** unique ID of this agent */
    private String agentGuid;
    /** number of agents known by this agent, not only with client, but reachable and not closed */
    private int agentsConnected;
    /** number of threads in this Agent */
    private int threadsCount;
    /** */
    private int servicesCount;
    private int serversCount;
    private int clientsCount;

    public AgentPing() {
    }

    public AgentPing(AgentRegister register, int agentsConnected, int threadsCount, int servicesCount, int serversCount, int clientsCount) {
        this.register = register;
        this.agentsConnected = agentsConnected;
        this.threadsCount = threadsCount;
        this.servicesCount = servicesCount;
        this.serversCount = serversCount;
        this.clientsCount = clientsCount;
    }

    public AgentRegister getRegister() {
        return register;
    }

    public String getAgentGuid() {
        return register.getAgentGuid();
    }
    public int getAgentsConnected() {
        return agentsConnected;
    }
    public int getThreadsCount() {
        return threadsCount;
    }
    public int getServicesCount() {
        return servicesCount;
    }
    public int getServersCount() {
        return serversCount;
    }
    public int getClientsCount() {
        return clientsCount;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }
    /** */
    public Map<String, String> toMap() {
        HashMap<String, String> map = new HashMap<>(register.toAgentRegisterRow().toMap());
        map.put("lastpingdate", LocalDateTime.now().toString());
        map.put("agents", "" + agentsConnected);
        map.put("threads", "" + threadsCount);
        map.put("services", "" + servicesCount);
        map.put("servers", "" + serversCount);
        map.put("clients", "" + clientsCount);
        return map;
    }

    @Override
    public java.lang.String toString() {
        return "PING,createdServers=" + register.getAgentGuid() + ",agentsConnected=" + agentsConnected + ",threadsCount=" + threadsCount;
    }
}
