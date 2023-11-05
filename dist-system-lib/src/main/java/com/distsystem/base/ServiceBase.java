package com.distsystem.base;

import com.distsystem.api.AgentWebApiRequest;
import com.distsystem.api.AgentWebApiResponse;
import com.distsystem.api.DistServiceInfo;
import com.distsystem.base.dtos.DistAgentServiceRow;
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
import java.util.function.BiFunction;
import java.util.function.Function;

/** base class for any service connected to Agent */
public abstract class ServiceBase extends Agentable implements DistService {

    /** list of components in current service */
    protected List<AgentComponent> componentList = new LinkedList<>();
    /** processor class for Web API - instant synchronized API to be used directly with Agent service */
    private final DistWebApiProcessor webApiProcessor = new DistWebApiProcessor(getServiceType())
            .addHandlerGet("ping", (m, req) -> req.responseOkText("ping"))
            .addHandlerGet("created", (m, req) -> req.responseOkText( getCreateDate().toString()))
            .addHandlerGet("uid", (m, req) -> req.responseOkText(getGuid()))
            .addHandlerGet("last-touch-date", (m, req) -> req.responseOkText(getGuid()))
            .addHandlerGet("service-type", (m, req) -> req.responseOkText(getServiceType().name()))
            .addHandlerGet("service-info", (m, req) -> req.responseOkJson(JsonUtils.serialize(getServiceInfo())))
            .addHandlerPost("reinitialize", (m, req) -> req.responseOkJson(JsonUtils.serialize(getServiceInfo())))
            .addHandlerGet("components", (m, req) -> req.responseOkText(JsonUtils.serialize(getComponentKeys())))
            .addHandlerGet("closed", (m, req) -> req.responseOkText( ""+isClosed()))
            .merge(additionalWebApiProcessor());

    /** creates new service with agent */
    public ServiceBase(Agent parentAgent) {
        super(parentAgent);
    }

    /** process simple request with text returned */
    protected BiFunction<String, AgentWebApiRequest, AgentWebApiResponse> createTextHandler(Function<String, Object> methodToGetObj) {
        return (m, req) -> req.responseOkText(JsonUtils.serialize(methodToGetObj.apply(req.getParamOne())));
    }
    /** process request with returned JSON content */
    protected BiFunction<String, AgentWebApiRequest, AgentWebApiResponse> createJsonHandler(Function<String, Object> methodToGetObj) {
        return (m, req) -> req.responseOkJson(JsonUtils.serialize(methodToGetObj.apply(req.getParamOne())));
    }
    /** get keys of all components added to this service */
    public List<String> getComponentKeys() {
        return componentList.stream().map(c -> c.getComponentType().name()).toList();
    }
    /** read configuration and re-initialize this service */
    public abstract boolean reinitialize();
    /** create unique UID */
    protected String createGuid() {
        return DistUtils.generateServiceGuid(getServiceType().name().toUpperCase(), parentAgent.getAgentShortGuid());
    }
    /** get current web api processor to handle web requests */
    protected final DistWebApiProcessor getWebApiProcessor() {
        return webApiProcessor;
    }
    /** get additional web api processor to append more service-specific APIs
     * to be overridden by services */
    protected DistWebApiProcessor additionalWebApiProcessor() {
        return new DistWebApiProcessor(getServiceType());
    }
    /** handle Web API request */
    @Override
    public  AgentWebApiResponse handleRequest(AgentWebApiRequest request) {
        return webApiProcessor.handleRequest(request);
    }

    /** get basic information about service */
    public DistServiceInfo getServiceInfo() {
        return new DistServiceInfo(getServiceType(), getClass().getName(), getGuid(), createDate, closed, getServiceInfoCustomMap());
    }
    /** get row for registration services */
    public DistAgentServiceRow getServiceRow() {
        // String agentguid, String serverguid, String servicetype, LocalDateTime createddate, int isactive, LocalDateTime lastpingdate
        return new DistAgentServiceRow(parentAgent.getAgentGuid(), "", getServiceType().name(), createDate, (closed)?0:1, LocalDateTime.now());
    }
    /** add component to this service */
    public void addComponent(AgentComponent component) {
        componentList.add(component);
    }
    /** get custom map of info about service */
    public Map<String, String> getServiceInfoCustomMap() {
        return Map.of();
    }

}
