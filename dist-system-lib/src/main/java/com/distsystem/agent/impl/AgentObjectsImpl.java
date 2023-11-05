package com.distsystem.agent.impl;

import com.distsystem.api.DistConfig;
import com.distsystem.api.DistMessage;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.base.ServiceBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentObjects;

/** implementation for shared objects */
public class AgentObjectsImpl extends ServiceBase implements AgentObjects {

    /** creates new Objects service for shared objects */
    public AgentObjectsImpl(Agent parentAgent) {
        super(parentAgent);
        parentAgent.getAgentServices().registerService(this);
        log.info("--------> Created new flow service with GUID: " + guid + ", CONFIG: " + getConfig().getConfigGuid() + ", properties: " + getConfig().getProperties().size());
    }

    @Override
    public DistServiceType getServiceType() {
        return DistServiceType.objects;
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
