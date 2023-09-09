package com.distsystem.agent.impl;

import com.distsystem.api.AgentWebApiRequest;
import com.distsystem.api.AgentWebApiResponse;
import com.distsystem.api.DistMessage;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.base.ServiceBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentSemaphores;
import com.distsystem.utils.DistUtils;

/** */
public class AgentSemaphoresImpl extends ServiceBase implements AgentSemaphores {

    /** creates new ML service */
    public AgentSemaphoresImpl(Agent parentAgent) {
        super(parentAgent);
        log.info("--------> Created new semaphores service with GUID: " + guid + ", CONFIG: " + getConfig().getConfigGuid() + ", properties: " + getConfig().getProperties().size());
    }

    @Override
    public DistServiceType getServiceType() {
        return DistServiceType.semaphores;
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
