package com.distsystem.agent.impl;

import com.distsystem.agent.services.AgentReceiverService;
import com.distsystem.api.*;
import com.distsystem.api.enums.DistComponentType;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.auth.AgentAuthImpl;
import com.distsystem.base.ServiceBase;
import com.distsystem.base.dtos.DistAgentServiceRow;
import com.distsystem.interfaces.*;
import com.distsystem.managers.CacheManager;
import com.distsystem.report.AgentReportsImpl;
import com.distsystem.report.StoragesImpl;
import com.distsystem.utils.DistWebApiProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** Implementation of manager for services. Kept services, initiate them in case of the need.
 *
 *  */
public class AgentServicesImpl extends ServiceBase implements AgentServices {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(AgentServicesImpl.class);
    /** all registered services for this agent */
    private final HashMap<String, DistService> services = new HashMap<>();
    /** policy to add cache Objects to storages and changing mode, ttl, priority, tags */
    protected CachePolicy policy;
    private AgentAuth auth;
    /** service to provide message receiver and sender */
    private Receiver receiver;
    /** cache service */
    private Cache cache;
    /** service for reports */
    private AgentReports agentReports;
    /** service for storages */
    private Storages storages;
    /** service for spaces */
    private AgentSpace agentSpace;
    /** service for security */
    private AgentSecurity security;
    /** flow service */
    private AgentFlow flow;
    /** get semaphores service */
    private AgentSemaphores semaphores;
    /** get ML service */
    private AgentMachineLearning ml;
    /** get service for managing shared objects */
    private AgentObjects objects;
    /** creates service manager for agent with parent agent assigned */
    public AgentServicesImpl(Agent parentAgent) {
        super(parentAgent);
        registerService(this);
    }

    /** update configuration of this Service */
    public void updateConfig(DistConfig newCfg) {
        // TODO: update configuration of this service
    }

    /** reinitialize all registered services */
    public List<Boolean> reinitializeAllServices() {
        return services.values().stream().map(DistService::reinitialize).toList();
    }
    /** read configuration and re-initialize this component */
    public boolean reinitialize() {
        // nothing to be done here
        return true;
    }
    /** get type of this component */
    public DistComponentType getComponentType() {
        return DistComponentType.services;
    }

    /** additional web API endpoints */
    protected DistWebApiProcessor additionalWebApiProcessor() {
        return new DistWebApiProcessor(getServiceType())
                .addHandlerGet("service-keys", (m, req) -> req.responseOkJsonSerialize(getServiceKeys()))
                .addHandlerGet("services", (m, req) -> req.responseOkJsonSerialize(services.values().stream().map(DistService::getServiceInfo).toList()))
                .addHandlerGet("services-row", (m, req) -> req.responseOkJsonSerialize(services.values().stream().map(DistService::getServiceRow).toList()))
                .addHandlerGet("services-guid", (m, req) -> req.responseOkJsonSerialize(services.values().stream().map(DistService::getGuid).toList()))
                .addHandlerPost("service-keys", (m, req) -> req.responseOkJsonSerialize(initializeAllPossible()));
    }


    /** set new policy for services */
    public void setPolicy(CachePolicy policy) {
        this.policy = policy;
    }
    /** return all services assigned to this agent */
    public List<DistService> getServices() {
        return services.values().stream().collect(Collectors.toList());
    }



    /** get or create cache connected with this Agent */
    public Cache getCache() {
        if (cache != null) {
            return cache;
        }
        synchronized (this) {
            if (cache == null) {
                cache = new CacheManager(getAgent(), policy);
                registerService(cache);
            }
            return cache;
        }
    }
    /** */
    public AgentAuth getAuth() {
        if (auth != null) {
            return auth;
        }
        synchronized (this) {
            if (auth == null) {
                auth = new AgentAuthImpl(getAgent());
                registerService(auth);
            }
            return auth;
        }
    }

    /** get or create receiver service */
    public Receiver getReceiver() {
        if (receiver != null) {
            return receiver;
        }
        synchronized (this) {
            if (receiver == null) {
                receiver = new AgentReceiverService(getAgent());
                registerService(receiver);
            }
            return receiver;
        }
    }
    /** get or create service for reports to create, update, remove or execute reports */
    public AgentReports getReports() {
        if (agentReports != null) {
            return agentReports;
        }
        synchronized (this) {
            if (agentReports == null) {
                agentReports = new AgentReportsImpl(getAgent());
                registerService(agentReports);
            }
            return agentReports;
        }
    }
    /** get flow service */
    public AgentFlow getFlow() {
        if (flow != null) {
            return flow;
        }
        synchronized (this) {
            if (flow == null) {
                flow = new AgentFlowImpl(getAgent());
                registerService(flow);
            }
            return flow;
        }
    }
    /** get semaphores service */
    public AgentSemaphores getSemaphores() {
        if (semaphores != null) {
            return semaphores;
        }
        synchronized (this) {
            if (semaphores == null) {
                semaphores = new AgentSemaphoresImpl(getAgent());
                registerService(semaphores);
            }
            return semaphores;
        }
    }
    /** get ML service */
    public AgentMachineLearning getMl() {
        if (ml != null) {
            return ml;
        }
        synchronized (this) {
            if (ml == null) {
                ml = new AgentMachineLearningImpl(getAgent());
                registerService(ml);
            }
            return ml;
        }
    }
    /** get service for storages */
    public Storages getStorages() {
        if (storages != null) {
            return storages;
        }
        synchronized (this) {
            if (storages == null) {
                storages = new StoragesImpl(getAgent());
                registerService(storages);
            }
            return storages;
        }
    }
    /** get service for managing spaces */
    public AgentSpace getSpace() {
        if (agentSpace != null) {
            return agentSpace;
        }
        synchronized (this) {
            if (agentSpace == null) {
                agentSpace = new AgentSpaceImpl(getAgent());
                registerService(agentSpace);
            }
            return agentSpace;
        }
    }
    /** get service for security */
    public AgentSecurity getSecurity() {
        if (security != null) {
            return security;
        }
        synchronized (this) {
            if (security == null) {
                security = new AgentSecurityImpl(getAgent());
                registerService(security);
            }
            return security;
        }
    }
    /** get service for managing shared objects */
    public AgentObjects getObjects() {
        if (objects != null) {
            return objects;
        }
        synchronized (this) {
            if (objects == null) {
                objects = new AgentObjectsImpl(getAgent());
                registerService(objects);
            }
            return objects;
        }
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
    public Set<String> getServiceTypes() {
        return services.keySet();
    }
    /** initialize all known services */
    public Set<String> initializeAllPossible() {
        getAuth();
        getSecurity();
        getCache();
        getReceiver();
        getReports();
        getFlow();
        getSemaphores();
        getMl();
        getStorages();
        getSpace();
        getObjects();
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
    /** get basic information about all services */
    public List<DistServiceInfo> getServiceInfos() {
        return services.values().stream().map(DistService::getServiceInfo).collect(Collectors.toList());
    }
    /** get all rows of services to registrations */
    public List<DistAgentServiceRow> getServiceRows() {
        return services.values().stream().map(DistService::getServiceRow).collect(Collectors.toList());
    }
    /** register service to this agent */
    public void registerService(DistService service) {
        // TODO: register new service like cache, report, measure, ...
        synchronized (services) {
            services.put(service.getServiceType().name(), service);
        }
    }
    /** receive message from connector or server, need to find service and process that message on service */
    public void receiveMessage(DistMessage msg) {
        log.info("Receive message to be processes, message: " + msg.toString());
        if (msg.isTypeRequest()) {
            DistMessage response = processMessage(msg);
            // got response, send it to requestor agent
            parentAgent.getAgentConnectors().sendMessage(response);
        } else if (msg.isTypeResponse()) {
            if (msg.isTypeResponse()) {
                processMessage(msg);
            }
            parentAgent.getAgentConnectors().markResponse(msg);
        } else {
            // incorrect message type - log Issue
            parentAgent.getAgentIssues().addIssue("receiveMessage", new Exception("Unknown message type: " + msg.getMessageType().name()));
        }
    }
    /** process message - find service and execute on method  */
    public DistMessage processMessage(DistMessage msg) {
        DistService serviceToProcessMessage = services.get(msg.getToService().name());
        if (serviceToProcessMessage != null) {
            return serviceToProcessMessage.processMessage(msg);
        } else {
            return msg.serviceNotFound();
        }
    }

    /** handle API request in this Web API for Agent */
    public AgentWebApiResponse dispatchRequest(AgentWebApiRequest request) {
        DistService service = services.get(request.getServiceName());
        if (service != null) {
            return service.handleRequest(request);
        }
        //  no service found, returning 404
        return new AgentWebApiResponse(404, AgentWebApiRequest.headerText, "No service for name: " + request.getServiceName());
    }
    /** close */
    protected void onClose() {
        log.info("Closing all registered services with agent, services: " + services.size());
    }
    @Override
    public DistServiceType getServiceType() {
        return DistServiceType.services;
    }

}
