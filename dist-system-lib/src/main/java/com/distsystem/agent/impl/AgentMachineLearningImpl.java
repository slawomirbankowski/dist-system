package com.distsystem.agent.impl;

import com.distsystem.api.AgentWebApiRequest;
import com.distsystem.api.AgentWebApiResponse;
import com.distsystem.api.DistMessage;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.base.ServiceBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentMachineLearning;
import com.distsystem.utils.DistUtils;

/** */
public class AgentMachineLearningImpl extends ServiceBase implements AgentMachineLearning {

    /** creates new ML service */
    public AgentMachineLearningImpl(Agent parentAgent) {
        super(parentAgent);
        log.info("--------> Created new ML service with GUID: " + guid + ", CONFIG: " + getConfig().getConfigGuid() + ", properties: " + getConfig().getProperties().size());
    }

    @Override
    public DistServiceType getServiceType() {
        return DistServiceType.ml;
    }
    @Override
    public DistMessage processMessage(DistMessage msg) {
        // TODO: implement processing message in ML service
        return msg.methodNotFound();
    }
    @Override
    public AgentWebApiResponse handleRequest(AgentWebApiRequest request) {
        // TODO: implement handling request in ML
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
