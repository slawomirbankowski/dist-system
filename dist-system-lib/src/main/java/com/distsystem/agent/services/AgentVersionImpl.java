package com.distsystem.agent.services;

import com.distsystem.api.DistConfig;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.base.ServiceBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentMachineLearning;
import com.distsystem.interfaces.AgentVersion;
import com.distsystem.utils.DistWebApiProcessor;

import java.util.LinkedList;

/** service for versions */
public class AgentVersionImpl extends ServiceBase implements AgentVersion {

    /** creates new version service */
    public AgentVersionImpl(Agent parentAgent) {
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
        return DistServiceType.version;
    }
    /** get description of this service */
    public String getServiceDescription() {
        return "Version service ";
    }

    /** additional web API endpoints */
    protected DistWebApiProcessor additionalWebApiProcessor() {
        return new DistWebApiProcessor(getServiceType())
                .addHandlerPost("versions", (m, req) -> req.responseOkJsonSerialize(new LinkedList<String>()));
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
        // nothing to be done here
        return true;
    }

}
