package com.distsystem.agent.impl;

import com.distsystem.api.AgentWebApiRequest;
import com.distsystem.api.AgentWebApiResponse;
import com.distsystem.api.DistConfig;
import com.distsystem.api.DistMessage;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.base.ServiceBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentFlow;
import com.distsystem.interfaces.AgentSpace;
import com.distsystem.utils.DistUtils;

public class AgentSpaceImpl extends ServiceBase implements AgentSpace {

    /** creates new Space */
    public AgentSpaceImpl(Agent parentAgent) {
        super(parentAgent);
        log.info("--------> Created new space service with GUID: " + guid + ", CONFIG: " + getConfig().getConfigGuid() + ", properties: " + getConfig().getProperties().size());
    }

    @Override
    public DistServiceType getServiceType() {
        return DistServiceType.space;
    }
    @Override
    public DistMessage processMessage(DistMessage msg) {
        return msg.methodNotFound();
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
