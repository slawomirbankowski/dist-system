package com.distsystem.agent.impl;

import com.distsystem.api.AgentWebApiRequest;
import com.distsystem.api.AgentWebApiResponse;
import com.distsystem.api.DistMessage;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.base.ServiceBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentObjects;
import com.distsystem.utils.DistUtils;

/** implementation for shared objects */
public class AgentObjectsImpl extends ServiceBase implements AgentObjects {

    /** creates new Objects service for shared objects */
    public AgentObjectsImpl(Agent parentAgent) {
        super(parentAgent);
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
    @Override
    public AgentWebApiResponse handleRequest(AgentWebApiRequest request) {
        return request.responseNotImplemented();
    }
    @Override
    protected String createServiceUid() {
        return DistUtils.generateCustomGuid("FLOW");
    }

    @Override
    protected void onClose() {
    }

}
