package com.distsystem.agent.impl;

import com.distsystem.agent.apis.WebSimpleApi;
import com.distsystem.api.*;
import com.distsystem.api.enums.DistComponentType;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.api.info.AgentApisInfo;
import com.distsystem.base.AgentWebApi;
import com.distsystem.base.ServiceBase;
import com.distsystem.interfaces.*;
import com.distsystem.utils.DistWebApiProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** manager for connections inside agent - servers and clients */
public class AgentApiImpl extends ServiceBase implements AgentApi {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(AgentApiImpl.class);

    /** all servers for connections to other agents */
    private final java.util.concurrent.ConcurrentHashMap<String, AgentWebApi> apiConnectors = new java.util.concurrent.ConcurrentHashMap<>();
    /** open counter */
    private final AtomicLong openCount = new AtomicLong();
    /** check counter */
    private final AtomicLong checkCount = new AtomicLong();

    /** create new connectors */
    public AgentApiImpl(Agent parentAgent) {
        super(parentAgent);
        parentAgent.getAgentServices().registerService(this);
    }

    /** get type of service: cache, measure, report, flow, space, ... */
    public DistServiceType getServiceType() {
        return DistServiceType.api;
    }
    /** process message, returns message with status */
    public DistMessage processMessage(DistMessage msg) {
        return msg.notSupported();
    }

    /** update configuration of this Service to add registrations, services, servers, ... */
    public void updateConfig(DistConfig newCfg) {
    }

    /** additional web API endpoints */
    protected DistWebApiProcessor additionalWebApiProcessor() {
        return new DistWebApiProcessor(getServiceType())
                .addHandlerGet("info", (m, req) -> req.responseOkJsonSerialize(getInfo()))
                .addHandlerGet("keys", (m, req) -> req.responseOkJsonSerialize(apiConnectors.keySet()))
                .addHandlerPost("open-apis", (m, req) -> req.responseOkJsonSerialize(openApisWithInfo()))
                .addHandlerGet("api-infos", (m, req) -> req.responseOkJsonSerialize(apiConnectors.values().stream().map(a -> a.getInfo()).toList()))
                .addHandlerGet("api-types", (m, req) -> req.responseOkJsonSerialize(getApiTypes()));
    }
    /** get first non empty Web API port */
    public int getPort() {
        return apiConnectors.values().stream().flatMapToInt(x -> IntStream.of(x.getPort())).findFirst().orElse(0);
    }
    /** get information structure about APIs */
    public AgentApisInfo getInfo() {
        return new AgentApisInfo(closed, openCount.get(), checkCount.get(),
                apiConnectors.values().stream().map(x -> x.getInfo()).collect(Collectors.toList()));
    }
    /** open all known APIs for this agent */
    public void openApis() {
        log.info("Opening Agent Web APIs for agent: " + parentAgent.getAgentGuid());
        if (parentAgent.getConfig().hasProperty(DistConfig.AGENT_API_PORT)) {
            openCount.incrementAndGet();
            log.info("Creates new WebApi interface for Agent: " + parentAgent.getAgentGuid());
            WebSimpleApi api = new WebSimpleApi(this);
            apiConnectors.put("", api);
        }
    }
    /** */
    public AgentApisInfo openApisWithInfo() {
        openApis();
        return getInfo();
    }
    /** read configuration and re-initialize this component */
    public boolean reinitialize() {
        // TODO: implement reinitialization
        return true;
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
        checkCount.incrementAndGet();

        // TODO: implement check of all APIs
    }

    /** close all connectors, clients, servers  */
    protected void onClose() {
        log.info("Closing APIs for agent: " + this.parentAgent.getAgentGuid() + ", count: " + apiConnectors.size());
        apiConnectors.values().stream().forEach(api -> api.close());
    }

}
