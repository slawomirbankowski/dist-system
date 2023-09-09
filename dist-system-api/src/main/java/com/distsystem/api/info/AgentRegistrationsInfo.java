package com.distsystem.api.info;

import java.util.List;

/** information about Registration class to register Agent */
public class AgentRegistrationsInfo {

    /** */
    private List<AgentRegistrationInfo> registrations;
    private long checkCount;
    /** */
    private List<AgentRegisteredInfo> agents;
    /** */
    public AgentRegistrationsInfo(List<AgentRegistrationInfo> registrations, long checkCount, List<AgentRegisteredInfo> agents) {
        this.registrations = registrations;
        this.checkCount = checkCount;
        this.agents = agents;
    }
    public List<AgentRegistrationInfo> getRegistrations() {
        return registrations;
    }

    public long getCheckCount() {
        return checkCount;
    }

    public List<AgentRegisteredInfo> getAgents() {
        return agents;
    }
}
