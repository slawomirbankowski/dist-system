package com.distsystem.agent.impl;

import com.distsystem.api.AgentWebApiRequest;
import com.distsystem.api.AgentWebApiResponse;
import com.distsystem.api.DistMessage;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.base.ServiceBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentFlow;
import com.distsystem.utils.DistUtils;

/** implementation of flow in distributed environment */
public class AgentFlowImpl extends ServiceBase implements AgentFlow {

    /** creates new Auth */
    public AgentFlowImpl(Agent parentAgent) {
        super(parentAgent);
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
