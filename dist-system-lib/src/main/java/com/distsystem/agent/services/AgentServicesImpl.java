package com.distsystem.agent.services;

import com.distsystem.api.*;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.api.info.AgentServiceInfo;
import com.distsystem.base.ServiceBase;
import com.distsystem.api.dtos.DistAgentServiceRow;
import com.distsystem.interfaces.*;
import com.distsystem.utils.DistWebApiProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/** Implementation of manager for services. Kept services, initiate them in case of the need.
 *
 *  */
public class AgentServicesImpl extends ServiceBase implements AgentServices {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(AgentServicesImpl.class);
    /** all registered services for this agent */
    private final HashMap<String, DistService> services = new HashMap<>();
    /** endpoint processor for empty service */
    private DistWebApiProcessor emptyServiceWebApiProcessor = new DistWebApiProcessor("")
            .addHandlerGet("ping", (m, req) -> req.responseOkText("pong"))
            .addHandlerGet("info", (m, req) -> req.responseOkJsonSerialize(parentAgent.getAgentInfo()))
            .addHandlerGet("created", (m, req) -> req.responseOkText(getCreateDate().toString()))
            .addHandlerGet("name", (m, req) -> req.responseOkText(parentAgent.getAgentName()))
            .addHandlerGet("environment", (m, req) -> req.responseOkText(parentAgent.getEnvironmentName()))
            .addHandlerGet("uid", (m, req) -> req.responseOkText(parentAgent.getAgentGuid()))
            .addHandlerGet("endpoints", (m, req) -> req.responseOkJsonSerialize(parentAgent.getApi().getAllHandlers()))
            .addHandlerGet("", (m, req) -> req.responseOkText(parentAgent.welcomeMessage()));

    /** creates service manager for agent with parent agent assigned */
    public AgentServicesImpl(Agent parentAgent) {
        super(parentAgent);
        registerService(this);
    }

    /** count objects in this agentable object including this object */
    public long countObjectsService() {
        // TODO: calculate counts of objects
        return 2L + services.size()*2L;
    }
    /** update configuration of this Service */
    public void updateConfig(DistConfig newCfg) {
        // TODO: update configuration of this service
    }

    /** reinitialize all registered services */
    public List<Boolean> reinitializeAllServices() {
        log.info("Reinitialization of all services for agent: " + parentAgent.getAgentGuid() + ", services: " + services.size());
        long startTime = System.currentTimeMillis();
        List<DistService> servicesToReinitialize = services.values().stream().toList();
        List<Boolean> res = servicesToReinitialize.stream().map(DistService::reinitialize).toList();
        long totalTime = System.currentTimeMillis() - startTime;
        log.info("Reinitialization of all services for agent: " + parentAgent.getAgentGuid() + " FINISHED, time: " + totalTime);
        return res;
    }
    /** read configuration and re-initialize this component */
    protected boolean onReinitialize() {
        // nothing to be done here
        initializeAllPossible();
        return true;
    }

    /** additional web API endpoints */
    protected DistWebApiProcessor additionalWebApiProcessor() {
        return new DistWebApiProcessor(getServiceType())
                .addHandlerGet("service-keys", (m, req) -> req.responseOkJsonSerialize(getServiceKeys()))
                .addHandlerGet("service-types", (m, req) -> req.responseOkJsonSerialize(getServiceTypes()))
                .addHandlerGet("services", (m, req) -> req.responseOkJsonSerialize(services.values().stream().map(DistService::getServiceInfo).toList()))
                .addHandlerGet("services-row", (m, req) -> req.responseOkJsonSerialize(services.values().stream().map(DistService::getServiceRow).toList()))
                .addHandlerGet("services-guid", (m, req) -> req.responseOkJsonSerialize(services.values().stream().map(DistService::getGuid).toList()))
                .addHandlerGet("service", (m, req) -> req.responseOkJsonSerializeOrNotFound(getServiceInfoOrEmpty(req.getParamOne())))
                .addHandlerPost("reinitialize-all", (m, req) -> req.responseOkJsonSerialize(reinitializeAllServices()))
                .addHandlerPost("reinitialize-service", (m, req) -> req.responseOkJsonSerializeOrNotFound(reinitializeServiceWithInfo(req.getParamOne())))
                .addHandlerPost("reinitialize-possible", (m, req) -> req.responseOkJsonSerialize(initializeAllPossible()));
    }

    public AgentServiceInfo getInfo() {
        return new AgentServiceInfo(services.keySet().stream().toList(), services.values().stream().map(DistService::getServiceSimpleInfo).collect(Collectors.toList()));
    }
    /** return all services assigned to this agent */
    public List<DistService> getServices() {
        return services.values().stream().collect(Collectors.toList());
    }

    /** get number of services */
    public int getServicesCount() {
        return services.size();
    }
    /** get keys of registered services */
    public List<String> getServiceKeys() {
        return services.values().stream().map(DistService::getGuid).collect(Collectors.toList());
    }
    /** get types of registered services */
    public List<String> getServiceTypes() {
        return services.keySet().stream().sorted().toList();
    }
    /** get description of all registered services */
    public String getServiceDescriptions() {
        return services.values().stream().map(s -> s.getServiceType().name() + " - " + s.getServiceDescription()).collect(Collectors.joining("\n"));
    }
    /** initialize all known services */
    public List<String> initializeAllPossible() {
        createEvent("initializeAllPossible");
        return getServiceTypes();
    }
    /** get basic information about service for given type of UID */
    public DistServiceInfo getServiceInfo(String serviceUid) {
        DistService srv = services.get(serviceUid);
        if (srv != null) {
            return srv.getServiceInfo();
        } else {
            return null;
        }
    }
    public Optional<DistServiceInfo> getServiceInfoOrEmpty(String serviceUid) {
        DistService srv = services.get(serviceUid);
        if (srv != null) {
            return Optional.of(srv.getServiceInfo());
        } else {
            return Optional.empty();
        }
    }
    /** */
    public Optional<DistServiceInfo> reinitializeServiceWithInfo(String serviceUid) {
        createEvent("reinitializeServiceWithInfo");
        DistService srv = services.get(serviceUid);
        if (srv != null) {
            srv.reinitialize();
            return Optional.of(srv.getServiceInfo());
        } else {
            return Optional.empty();
        }
    }
    /** */
    public Optional<DistServiceInfo> runAfterInitializationWithInfo(String serviceUid) {
        touch("runAfterInitializationWithInfo");
        createEvent("runAfterInitializationWithInfo");
        DistService srv = services.get(serviceUid);
        if (srv != null) {
            srv.afterInitialization();
            return Optional.of(srv.getServiceInfo());
        } else {
            return Optional.empty();
        }
    }
    /** get basic information about all services */
    public List<DistServiceInfo> getServiceInfos() {
        return services.values().stream().map(DistService::getServiceInfo).sorted(Comparator.comparing(DistServiceInfo::getServiceTypeName)).collect(Collectors.toList());
    }
    /** get all rows of services to registrations */
    public List<DistAgentServiceRow> getServiceRows() {
        return services.values().stream().map(DistService::getServiceRow).collect(Collectors.toList());
    }
    /** register service to this agent */
    public void registerService(DistService service) {
        touch("registerService");
        // TODO: register new service like cache, report, measure, ...
        synchronized (services) {
            createdCount.incrementAndGet();
            services.put(service.getServiceType().name(), service);
        }
    }
    /** receive message from connector or server, need to find service and process that message on service */
    public void receiveMessage(DistMessage msg) {
        touch("receiveMessage");
        log.info("Receive message to be processes, message: " + msg.toString());
        if (msg.isTypeRequest()) {
            DistMessage response = dispatchMessage(msg);
            // got response, send it to requestor agent
            parentAgent.getConnectors().sendMessage(response);
        } else if (msg.isTypeResponse()) {
            if (msg.isTypeResponse()) {
                dispatchMessage(msg);
            }
            parentAgent.getConnectors().markResponse(msg);
        } else {
            // incorrect message type - log Issue
            parentAgent.getIssues().addIssue("receiveMessage", new Exception("Unknown message type: " + msg.getMessageType().name()));
        }
    }
    /** dispatch message - find service and execute that message on processor */
    public DistMessage dispatchMessage(DistMessage msg) {
        createEvent("dispatchMessage");
        touch("dispatchMessage");
        DistService serviceToProcessMessage = services.get(msg.getToService().name());
        if (serviceToProcessMessage != null) {
            return serviceToProcessMessage.processMessage(msg);
        } else {
            return msg.serviceNotFound();
        }
    }
    /** handle API request in this Web API for Agent */
    public AgentWebApiResponse dispatchRequest(AgentWebApiRequest request) {
        createEvent("dispatchRequest");
        touch("dispatchRequest");
        DistService service = services.get(request.getServiceName());
        if (service != null) {
            return service.handleRequest(request);
        } else {
            if (request.getServiceName().equals("")) {
                return emptyServiceWebApiProcessor.handleRequest(request);
            } else {
                //  no service found, returning 404
                return new AgentWebApiResponse(404, AgentWebApiRequest.headerText, "No service for name: " + request.getServiceName());
            }
        }
    }
    /** close */
    protected void onClose() {
        log.info("Closing all registered services with agent, services: " + services.size());
    }
    @Override
    public DistServiceType getServiceType() {
        return DistServiceType.services;
    }
    /** get description of this service */
    public String getServiceDescription() {
        return "Services kept and managed.";
    }

}
