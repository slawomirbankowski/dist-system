package com.distsystem.agent.impl;

import com.distsystem.api.AgentWebApiRequest;
import com.distsystem.api.AgentWebApiResponse;
import com.distsystem.api.DistMessage;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.base.ServiceBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentSecurity;
import com.distsystem.utils.DistUtils;

/** implementation of security within Agent distributed system */
public class AgentSecurityImpl extends ServiceBase implements AgentSecurity {

    /** creates new Auth */
    public AgentSecurityImpl(Agent parentAgent) {
        super(parentAgent);
        log.info("--------> Created new security with GUID: " + guid + ", CONFIG: " + getConfig().getConfigGuid() + ", properties: " + getConfig().getProperties().size());
    }

    @Override
    public DistServiceType getServiceType() {
        return DistServiceType.security;
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
        return DistUtils.generateCustomGuid("SECURITY");
    }

    @Override
    protected void onClose() {
    }


}
