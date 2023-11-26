package com.distsystem.agent.services;

import com.distsystem.api.DistConfig;
import com.distsystem.api.DistMessage;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.base.ServiceBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentSecurity;

/** implementation of security within Agent distributed system */
public class AgentSecurityImpl extends ServiceBase implements AgentSecurity {

    /** creates new Auth */
    public AgentSecurityImpl(Agent parentAgent) {
        super(parentAgent);
        parentAgent.getServices().registerService(this);
    }

    /** count objects in this agentable object including this object */
    public long countObjectsService() {
        // TODO: calculate counts of objects
        return 2L;
    }
    @Override
    public DistServiceType getServiceType() {
        return DistServiceType.security;
    }


    /** update configuration of this Service */
    public void updateConfig(DistConfig newCfg) {
        // TODO: update configuration of this service
    }
    @Override
    protected void onClose() {
    }

    /** read configuration and re-initialize this component */
    protected boolean onReinitialize() {
        // TODO: implement reinitialization
        return true;
    }

}
