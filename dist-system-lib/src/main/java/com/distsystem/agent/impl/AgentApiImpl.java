package com.distsystem.agent.impl;

import com.distsystem.agent.apis.WebSimpleApi;
import com.distsystem.api.*;
import com.distsystem.api.enums.DistComponentType;
import com.distsystem.api.info.AgentApisInfo;
import com.distsystem.base.AgentWebApi;
import com.distsystem.interfaces.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** manager for connections inside agent - servers and clients */
public class AgentApiImpl extends Agentable implements AgentApi, AgentComponent {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(AgentApiImpl.class);

    /** all servers for connections to other agents */
    private final java.util.concurrent.ConcurrentHashMap<String, AgentWebApi> apiConnectors = new java.util.concurrent.ConcurrentHashMap<>();
    /** if APIs have been closed */
    private boolean closed = false;

    /** create new connectors */
    public AgentApiImpl(Agent parentAgent) {
        super(parentAgent);
        parentAgent.addComponent(this);
    }

    /** get type of this component */
    public DistComponentType getComponentType() {
        return DistComponentType.api;
    }
    @Override
    public String getGuid() {
        return getParentAgentGuid();
    }
    /** get first non empty Web API port */
    public int getPort() {
        return apiConnectors.values().stream().flatMapToInt(x -> IntStream.of(x.getPort())).findFirst().orElse(0);
    }
    /** get parent agent connected to this API implementation */
    public Agent getAgent() {
        return parentAgent;
    }
    /** get information structure about APIs */
    public AgentApisInfo getInfo() {
        return new AgentApisInfo(closed,
                apiConnectors.values().stream().map(x -> x.getInfo()).collect(Collectors.toList()));
    }
    /** open all known APIs for this agent */
    public void openApis() {
        log.info("Opening Agent Web APIs for agent: " + parentAgent.getAgentGuid());
        if (parentAgent.getConfig().hasProperty(DistConfig.AGENT_API_PORT)) {
            log.info("Creates new WebApi interface for Agent: " + parentAgent.getAgentGuid());
            WebSimpleApi api = new WebSimpleApi(this);
            apiConnectors.put("", api);
        }
    }

    /** get count of APIs */
    public int getApisCount() {
        return apiConnectors.size();
    }
    /** get all types of registered Web APIs */
    public List<String> getApiTypes() {
        return apiConnectors.values().stream().map(v -> v.getApiType()).collect(Collectors.toList());
    }
    /** check all registered APIs */
    public void checkApis() {
        log.info("Check APIs for Agent: " + parentAgent.getAgentGuid());

        // TODO: implement check of all APIs
    }
    /** if APIs have been closed */
    public boolean isClosed() {
        return closed;
    }
    /** close all connectors, clients, servers  */
    public void close() {
        log.info("Closing APIs for agent: " + this.parentAgent.getAgentGuid() + ", count: " + apiConnectors.size());
        closed = true;
        apiConnectors.values().stream().forEach(api -> api.close());
    }

}
