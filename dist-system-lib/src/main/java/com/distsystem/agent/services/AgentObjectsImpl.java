package com.distsystem.agent.services;

import com.distsystem.api.DistConfig;
import com.distsystem.api.DistConfigBucket;
import com.distsystem.api.DistMessage;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.base.ServiceBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentObjects;

/** service with shared objects in distributed environment */
public class AgentObjectsImpl extends ServiceBase implements AgentObjects {

    /** creates new Objects service for shared objects */
    public AgentObjectsImpl(Agent parentAgent) {
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
        return DistServiceType.objects;
    }

    /** update configuration of this Service */
    public void updateConfig(DistConfig newCfg) {
        // TODO: update configuration of this service
    }

    /** change values in configuration bucket */
    public void initializeConfigBucket(DistConfigBucket bucket) {
        // INITIALIZE DAOs
    }
    /** run after initialization */
    public void afterInitialization() {

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
