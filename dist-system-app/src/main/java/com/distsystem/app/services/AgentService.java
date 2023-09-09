package com.distsystem.app.services;

import com.distsystem.api.*;
import com.distsystem.base.dtos.DistAgentRegisterRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class AgentService {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(AgentService.class);
    /** list of local agents */
    private final HashMap<String, AgentObject> agents = new HashMap<>();

    /** register new agent for given GUID */
    public AgentConfirmation registerAgent(AgentRegister register) {
        AgentConfirmation confirmation;
        synchronized (agents) {
            log.info("Registering new agent for guid: " + register.getAgentGuid() + " on host: " + register.getHostName());
            List<DistAgentRegisterRow> agentsSimplified = getAgents();
            AgentObject currentAgent = agents.get(register.getAgentGuid());
            confirmation = new AgentConfirmation(register.getAgentGuid(), currentAgent==null, false, agents.size(), agentsSimplified);
            if (currentAgent == null) {
                log.info("No agent for guid: " + register.getAgentGuid() + ", registering new one, host: " + register.getHostName() + ", port: " + register.getPort());
                // agent is new - need to register
                AgentObject agentObj = new AgentObject(register);
                agents.put(register.getAgentGuid(), agentObj);
            } else {
                // agent is already registered, need to update information about storages and other connected agents
                log.info("Agent for guid: " + register.getAgentGuid() + " already registered, updating existing new, created date: " + currentAgent.getCreateDate());
                //currentAgent.update(register);
            }
        }
        return confirmation;
    }

    /** ping agent with status information - this could be done every 1 minute */
    public AgentPingResponse pingAgent(AgentPing pingObject) {

        return new AgentPingResponse(pingObject.getAgentGuid());
    }
    /** get list of all registered agents */
    public List<DistAgentRegisterRow> getAgents() {
        return agents.values().stream().map(x -> x.toSimplified()).collect(Collectors.toList());
    }
    /** get agent by ID */
    public List<DistAgentRegisterRow> getAgentById(String id) {
        var agent = agents.get(id);
        if (agent == null) {

        } else {
            // TODO: serialize single agent
        }
        return agents.values().stream().map(x -> x.toSimplified()).collect(Collectors.toList());
    }
    /** unregister agent */
    public AgentConfirmation unregisterAgent(String guid) {
        // TODO: unregister agent for given GUID and secret
        AgentObject currentAgent = agents.remove(guid);
        if (currentAgent != null) {
            log.info("Agent to be unregistered: " + currentAgent.getAgentGuid());
            currentAgent.unregister();
        }
        AgentConfirmation confirmation = new AgentConfirmation(guid, false, true, agents.size(), getAgents());
        return confirmation;
    }

}
