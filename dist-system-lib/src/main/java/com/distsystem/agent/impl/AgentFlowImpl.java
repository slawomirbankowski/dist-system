package com.distsystem.agent.impl;

import com.distsystem.api.AgentWebApiRequest;
import com.distsystem.api.AgentWebApiResponse;
import com.distsystem.api.DistConfig;
import com.distsystem.api.DistMessage;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.base.ServiceBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentFlow;
import com.distsystem.utils.DistUtils;

import java.util.Map;

/** implementation of flow in distributed environment */
public class AgentFlowImpl extends ServiceBase implements AgentFlow {

    /** creates new Flow service */
    public AgentFlowImpl(Agent parentAgent) {
        super(parentAgent);
        parentAgent.getAgentServices().registerService(this);
        log.info("--------> Created new flow service with GUID: " + guid + ", CONFIG: " + getConfig().getConfigGuid() + ", properties: " + getConfig().getProperties().size());
    }

    @Override
    public DistServiceType getServiceType() {
        return DistServiceType.flow;
    }
    @Override
    public DistMessage processMessage(DistMessage msg) {
        return msg.methodNotFound();
    }

    /** get custom map of info about service */
    @Override
    public Map<String, String> getServiceInfoCustomMap() {
        return Map.of();
    }
    /** update configuration of this Service */
    public void updateConfig(DistConfig newCfg) {
        // TODO: update configuration of this service
    }
    @Override
    protected void onClose() {
    }

    /** read configuration and re-initialize this component */
    public boolean reinitialize() {
        // TODO: implement reinitialization
        return true;
    }
}
