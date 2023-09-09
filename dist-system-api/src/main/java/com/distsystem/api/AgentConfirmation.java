package com.distsystem.api;

import com.distsystem.base.dtos.DistAgentRegisterRow;

import java.util.List;

/** confirmation of registering agent */
public class AgentConfirmation {
    /** unique ID of agent in the system */
    private String agentGuid;
    /** if this agent is new */
    private boolean isNew;
    /** if this agent has been deleted */
    private boolean isDeleted;
    /** total number of agents after this confirmation */
    private int totalAgentsCount;
    /** currently registered agents */
    private List<DistAgentRegisterRow> agents;

    public AgentConfirmation(String agentGuid, boolean isNew, boolean isDeleted, int totalAgentsCount, List<DistAgentRegisterRow> agents) {
        this.agentGuid = agentGuid;
        this.isNew = isNew;
        this.isDeleted = isDeleted;
        this.totalAgentsCount = totalAgentsCount;
        this.agents = agents;
    }


    public String getAgentGuid() {
        return agentGuid;
    }

    public boolean isNew() {
        return isNew;
    }
    public boolean isDeleted() {
        return isDeleted;
    }
    public int getTotalAgentsCount() {
        return totalAgentsCount;
    }
    public List<DistAgentRegisterRow> getAgents() {
        return agents;
    }

    @Override
    public java.lang.String toString() {
        return "AGENT_CONFIRMATION,agentGuid=" + agentGuid + ",totalAgentsCount=" + totalAgentsCount + "isNew=" + isNew;
    }
}
