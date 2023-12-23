package com.distsystem.agent.services;

import com.distsystem.agent.apis.WebSimpleApi;
import com.distsystem.api.*;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.api.info.AgentApisInfo;
import com.distsystem.base.AgentWebApi;
import com.distsystem.base.ServiceBase;
import com.distsystem.interfaces.*;
import com.distsystem.utils.DistWebApiProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** service for Web REST API for Agent endpoints - it contains many endpoints per service for variuos actions
 * Each endpoint has format:
 * /service/method/paramOne/paramTwo?query
 * Sample endpoints:
 *  /agent/ping
 *  /timers/info
 *  /events/events
 *  /connectors/servers
 *  /cache/items-count
 *
 * */
public class AgentApiImpl extends ServiceBase implements AgentApi {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(AgentApiImpl.class);

    /** all servers for connections to other agents */
    private final java.util.concurrent.ConcurrentHashMap<String, AgentWebApi> apiConnectors = new java.util.concurrent.ConcurrentHashMap<>();

    /** create new connectors */
    public AgentApiImpl(Agent parentAgent) {
        super(parentAgent);
        parentAgent.getServices().registerService(this);
    }
    /** count objects in this agentable object including this object */
    public long countObjectsService() {
        return 2 + apiConnectors.values().stream().mapToLong(x -> 1L + x.countObjects()).sum();
    }

    /** get type of service: cache, measure, report, flow, space, ... */
    public DistServiceType getServiceType() {
        return DistServiceType.api;
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
                .addHandlerPost("check", (m, req) -> req.responseOkJsonSerialize(checkApis()))
                .addHandlerGet("all-apis", (m, req) -> req.responseOkJsonSerialize(listAllApis()))
                .addHandlerGet("api-infos", (m, req) -> req.responseOkJsonSerialize(apiConnectors.values().stream().map(a -> a.getInfo()).toList()))
                .addHandlerGet("api-types", (m, req) -> req.responseOkJsonSerialize(getApiTypes()));
    }
    /** get first non empty Web API port */
    public int getPort() {
        return apiConnectors.values().stream().flatMapToInt(x -> IntStream.of(x.getPort())).findFirst().orElse(0);
    }
    /** list all API handlers - methods to be run */
    public List<AgentServiceHandlers> listAllApis() {
        return getAgent().getServices().getServices().stream().map(s -> {
            return new AgentServiceHandlers(s.getServiceType().name(), s.getWebApiProcessor().getAllHandlers());
        }).toList();
    }
    /** get information structure about APIs */
    public AgentApisInfo getInfo() {
        return new AgentApisInfo(closed, openCount.get(), checkCount.get(),
                getHandledRequestsCount(), getHandledRequestsTime(), getHandledRequestsErrors(),
                apiConnectors.values().stream().map(x -> x.getInfo()).collect(Collectors.toList()));
    }
    /** get number of requests */
    public long getHandledRequestsCount() {
        return apiConnectors.values().stream().mapToLong(x -> x.getHandledRequestsCount()).sum();
    }
    /** get total time of requests */
    public long getHandledRequestsTime() {
        return apiConnectors.values().stream().mapToLong(x -> x.getHandledRequestsTime()).sum();
    }
    /** get count of errors in requests */
    public long getHandledRequestsErrors() {
        return apiConnectors.values().stream().mapToLong(x -> x.getHandledRequestsErrors()).sum();
    }

    /** open all known APIs for this agent */
    public void openApis() {
        log.info("Opening Agent Web APIs for agent: " + parentAgent.getAgentGuid());
        if (parentAgent.getConfig().hasProperty(DistConfig.AGENT_API_PORT)) {
            createEvent("openApis");
            openCount.incrementAndGet();
            log.info("Creates new WebApi interface for Agent: " + parentAgent.getAgentGuid());
            WebSimpleApi api = new WebSimpleApi(this);
            apiConnectors.put(DistConfig.PRIMARY, api);
            openCount.incrementAndGet();
            initialized = true;
        }
    }
    /** open APIs and get info */
    public AgentApisInfo openApisWithInfo() {
        openApis();
        return getInfo();
    }
    /** read configuration and re-initialize this component */
    protected boolean onReinitialize() {
        openApis();
        // TODO: implement reinitialization of APIs
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
    public List<Boolean> checkApis() {
        log.info("Check APIs for Agent: " + parentAgent.getAgentGuid());
        createEvent("checkApis");
        checkCount.incrementAndGet();
        return apiConnectors.values().stream().map(v -> v.check()).collect(Collectors.toList());
    }
    /** close all connectors, clients, servers  */
    protected void onClose() {
        log.info("Closing APIs for agent: " + this.parentAgent.getAgentGuid() + ", count: " + apiConnectors.size());
        createEvent("onClose");
        apiConnectors.values().stream().forEach(api -> api.close());
    }

}
