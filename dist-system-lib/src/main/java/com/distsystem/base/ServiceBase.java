package com.distsystem.base;

import com.distsystem.api.*;
import com.distsystem.api.dtos.DistAgentEventRow;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.api.info.AgentServiceSimpleInfo;
import com.distsystem.api.info.DistConfigGroupInfo;
import com.distsystem.api.dtos.DistAgentServiceRow;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentComponent;
import com.distsystem.interfaces.DistService;
import com.distsystem.utils.DistUtils;
import com.distsystem.utils.DistWebApiProcessor;
import com.distsystem.utils.JsonUtils;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.Function;

/** base class for any service connected to Agent */
public abstract class ServiceBase extends AgentableBase implements DistService {

    /** maximum number of messages stored per service */
    private static final int MAX_MESSAGES = 100;

    /** list of components in current service */
    protected final List<AgentComponent> componentList = new LinkedList<>();
    /** latest 100 messages to this service */
    protected final LinkedList<DistMessageSimple> latestMessages = new LinkedList<>();
    /** config group used by this service */
    protected DistConfigGroup configGroup = null;
    /** open counter */
    protected final AtomicLong openCount = new AtomicLong();
    /** check counter */
    protected final AtomicLong checkCount = new AtomicLong();
    /** read counter */
    protected final AtomicLong readCount = new AtomicLong();
    /** created counter */
    protected final AtomicLong createdCount = new AtomicLong();
    /** created counter */
    protected final AtomicLong initializedCount = new AtomicLong();
    /** messages counter */
    protected final AtomicLong messagesCount = new AtomicLong();

    /** processor class for Web API - instant synchronized API to be used directly with Agent service */
    protected final DistWebApiProcessor webApiProcessor = new DistWebApiProcessor(getServiceType())
            .addHandlerGet("_self", (m, req) -> req.responseOkJsonSerialize(req))
            .addHandlerGet("ping", (m, req) -> req.responseOkText("pong"))
            .addHandlerGet("service-created", (m, req) -> req.responseOkText(getCreateDate().toString()))
            .addHandlerGet("service-uid", (m, req) -> req.responseOkText(getGuid()))
            .addHandlerGet("service-last-touch-date", (m, req) -> req.responseOkText(lastTouchDate.toString()))
            .addHandlerGet("service-last-touch-by", (m, req) -> req.responseOkText(lastTouchBy))
            .addHandlerGet("service-apis", (m, req) -> req.responseOkJsonSerialize(getAllHandlers()))
            .addHandlerGet("service-type", (m, req) -> req.responseOkText(getServiceType().name()))
            .addHandlerGet("service-info", (m, req) -> req.responseOkJsonSerialize(getServiceInfo()))
            .addHandlerGet("service-search", (m, req) -> req.responseOkJsonSerialize(searchInService(req)))
            .addHandlerGet("service-events", (m, req) -> req.responseOkJsonSerialize(getServiceEventRows()))
            .addHandlerGet("service-requests", (m, req) -> req.responseOkJsonSerialize(getLatestRequests()))
            .addHandlerGet("service-messages", (m, req) -> req.responseOkJsonSerialize(getLatestRequests()))
            .addHandlerGet("service-method-handlers", (m, req) -> req.responseOkJsonSerialize(getMessageHandlerMethods()))
            .addHandlerPost("service-reinitialize", (m, req) -> req.responseOkJsonSerialize(getServiceInfo()))
            .addHandlerGet("service-components", (m, req) -> req.responseOkText(JsonUtils.serialize(getComponentKeys())))
            .addHandlerGet("service-closed", (m, req) -> req.responseOkText( ""+isClosed()))
            .addHandlerGet("service-initialized", (m, req) -> req.responseOkText( ""+isInitialized()))
            .addHandlerGet("service-config-group", (m, req) -> req.responseOkJsonSerialize((configGroup==null)?DistConfigGroupInfo.emptyInfo():configGroup.getInfo()))
            .merge(additionalWebApiProcessor());

    /** creates new service with agent */
    public ServiceBase(Agent parentAgent) {
        super(parentAgent);
    }

    /** count objects in this agentable object including this object */
    protected long countObjectsAgentable() {
        return componentList.stream().mapToLong(c -> 1L).sum() + countObjectsService();
    }
    /** count objects in this agentable object including this object */
    protected abstract long countObjectsService();

    /** process simple request with text returned */
    protected final BiFunction<String, AgentWebApiRequest, AgentWebApiResponse> createTextHandler(Function<String, Object> methodToGetObj) {
        return (m, req) -> req.responseOkText(""+methodToGetObj.apply(req.getParamOne()));
    }
    /** process request with returned JSON content */
    protected final BiFunction<String, AgentWebApiRequest, AgentWebApiResponse> createJsonHandler(Function<String, Object> methodToGetObj) {
        return (m, req) -> req.responseOkJsonSerialize(methodToGetObj.apply(req.getParamOne()));
    }
    /** get keys of all components added to this service */
    public final List<String> getComponentKeys() {
        return componentList.stream().map(c -> c.getComponentType().name()).toList();
    }
    /** get agentable event rows */
    public List<DistAgentEventRow> getServiceEventRows() {
        return agentableEvents.stream().map(e -> e.getEventRow()).toList();
    }

    /** get name of this agentable object */
    public String getAgentableName() {
        return getServiceType().name();
    }
    /** read configuration and re-initialize this service */
    public boolean reinitialize() {
        touch();
        createEvent("reinitialize");
        log.debug("Reinitialization of service in agent: " + parentAgent.getAgentGuid() + ", service: " + getServiceType().name() + ", UID: " + getGuid() + ", class: " + this.getClass().getName() + ", initializedCount: " + initializedCount.get());
        initializedCount.incrementAndGet();
        boolean r = onReinitialize();
        initialized = true;
        return r;
    }
    /** read configuration and re-initialize this service - to override */
    protected abstract boolean onReinitialize();
    /** change values in configuration bucket - to override this method */
    public DistStatusMap initializeConfigBucket(DistConfigBucket bucket) {
        return DistStatusMap.create(this).notImplemented();
    }
    /** run after initialization */
    public void afterInitialization() {
    }
    /** create unique UID */
    protected String createGuid() {
        return DistUtils.generateServiceGuid(getServiceType().name().toUpperCase(), parentAgent.getAgentShortGuid());
    }
    /** search in this service */
    protected AgentSearchResults searchInService(AgentWebApiRequest request) {
        //request.getParamOne();
      //  AgentSearchQuery query = new AgentSearchQuery();
       // var items = search(query);
        AgentSearchResults res = new AgentSearchResults();
        return res;
    }
    /** get info object for configuration group used by this service */
    public final DistConfigGroupInfo getConfigGroupInfo() {
        return (configGroup==null)?DistConfigGroupInfo.emptyInfo():configGroup.getInfo();
    }
    /** */
    public List<String> getMessageHandlerMethods() {
        return webApiProcessor.getMessageHandlerMethods();
    }
    /** get info object for configuration group used by this service */
    public final DistConfigGroup getConfigGroup() {
        return configGroup;
    }
    /** register configuration group with buckets to initialize internal service objects */
    protected void registerConfigGroup(String groupName) {
        createEvent("registerConfigGroup", groupName);
        log.debug("Registering new configuration group for agent: " + parentAgent.getAgentGuid() + ", serviceType: " + getServiceType().name() + ", group: " + groupName);
        if (configGroup != null) {
            log.debug("Registering new configuration group, there is already existing group, need to close, agent: " + parentAgent.getAgentGuid() + ", serviceType: " + getServiceType().name() + ", group: " + groupName);
            configGroup.close();
        }
        configGroup = parentAgent.registerConfigGroup(groupName, this);
    }
    /** get current web api processor to handle web requests */
    public final DistWebApiProcessor getWebApiProcessor() {
        return webApiProcessor;
    }
    /** get additional web api processor to append more service-specific Web APIs
     * to be overridden by services */
    protected DistWebApiProcessor additionalWebApiProcessor() {
        return new DistWebApiProcessor(getServiceType());
    }
    /** get all API handlers - just keys like GET:list-apis, POST:services, PUT:account */
    public final List<String> getAllHandlers() {
        return webApiProcessor.getAllHandlers();
    }
    /** get the latest requests to this service */
    public final List<AgentWebApiEvent> getLatestRequests() {
        return webApiProcessor.getLatestRequests();
    }
    /** get the latest messages to this service */
    public final List<DistMessageSimple> getLatestMessages() {
        return latestMessages;
    }
    /** handle Web API simple request */
    public final AgentWebApiResponse handleSimpleRequestText(String method, String serviceMethod) {
        return handleSimpleRequest(method, serviceMethod, AgentWebApiRequest.headerText);
    }
    /** handle Web API simple request */
    public final AgentWebApiResponse handleSimpleRequestJson(String method, String serviceMethod) {
        return handleSimpleRequest(method, serviceMethod, AgentWebApiRequest.headerJson);
    }
    /** handle Web API simple request
     * method: GET, POST, PUT, DELETE
     * serviceMethod: service method to be run: ping, last-touch-date */
    public final AgentWebApiResponse handleSimpleRequest(String method, String serviceMethod, Map<String, List<String>> headers) {
        AgentWebApiRequest req = AgentWebApiRequest.create(getServiceType().name(), method, serviceMethod, headers);
        return handleRequest(req);
    }
    /** handle Web API request */
    @Override
    public final AgentWebApiResponse handleRequest(AgentWebApiRequest request) {
        return webApiProcessor.handleRequest(request);
    }
    /** process message, returns message with status */
    public final DistMessage processMessage(DistMessage msg) {
        messagesCount.incrementAndGet();
        latestMessages.push(msg.toSimple());
        while (latestMessages.size()>MAX_MESSAGES) {
            latestMessages.pop();
        }
        return webApiProcessor.processMessage(msg);
    }
    /** reinitialize and return info about this service */
    protected final DistServiceInfo reinitializeWithInfo() {
        reinitialize();
        return getServiceInfo();
    }
    /** get basic information about service */
    public final DistServiceInfo getServiceInfo() {
        Map<String, Long> counters = Map.of("open", openCount.get(),
                "check", checkCount.get(),
                "read", readCount.get(),
                "created", createdCount.get(),
                "initialized", initializedCount.get(),
                "messages", messagesCount.get()
        );
        return new DistServiceInfo(getServiceType(), getClass().getName(), getGuid(), getServiceDescription(),
                createDate, lastTouchDate, lastTouchBy, closed, initialized,
                getComponentKeys(), counters, getServiceInfoCustomMap(), webApiProcessor.getInfo(), getInfo());
    }

    /** get simple information about service */
    public final AgentServiceSimpleInfo getServiceSimpleInfo() {
        return new AgentServiceSimpleInfo(getServiceType(), getClass().getName(), getGuid(), createDate, lastTouchDate, closed, initialized);
    }
    /** get basic information about service */
    public Object getInfo() {
        return "NO_INFO";
    }
    /** get row for registration services */
    public DistAgentServiceRow getServiceRow() {
        // String agentguid, String serverguid, String servicetype, LocalDateTime createddate, int isactive, LocalDateTime lastpingdate
        return new DistAgentServiceRow(parentAgent.getAgentGuid(), getGuid(), getServiceType().name(),
                JsonUtils.serialize(getServiceInfo()), createDate, (closed)?0:1, LocalDateTime.now());
    }
    /** add component to this service */
    public final void addComponent(AgentComponent component) {
        createEvent("addComponent");
        componentList.add(component);
    }
    /** get custom map of info about service - to override */
    public Map<String, String> getServiceInfoCustomMap() {
        return Map.of();
    }

}
