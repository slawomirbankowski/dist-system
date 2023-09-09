package com.distsystem.agent.impl;

import com.distsystem.agent.services.AgentReceiverService;
import com.distsystem.api.*;
import com.distsystem.api.enums.DistComponentType;
import com.distsystem.base.dtos.DistAgentServiceRow;
import com.distsystem.interfaces.*;
import com.distsystem.managers.CacheManager;
import com.distsystem.report.AgentReportsImpl;
import com.distsystem.report.StoragesImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Implementation of manager for services. Kept services, initiate them in case of the need.
 *
 *  */
public class AgentServicesImpl extends Agentable implements AgentServices, AgentComponent {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(AgentServicesImpl.class);
    /** all registered services for this agent */
    private final HashMap<String, DistService> services = new HashMap<>();
    /** policy to add cache Objects to storages and changing mode, ttl, priority, tags */
    protected CachePolicy policy;
    /** service to provide message receiver and sender */
    private Receiver receiver;
    /** cache service */
    private Cache cache;
    /** service for reports */
    private AgentReports agentReports;
    /** service for storages */
    private Storages storages;
    /** service for spaces */
    private AgentSpaces agentSpaces;
    /** service for security */
    private AgentSecurity security;
    /** */
    private AgentAuth auth;
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
        parentAgent.addComponent(this);
    }

    /** get type of this component */
    public DistComponentType getComponentType() {
        return DistComponentType.services;
    }
    @Override
    public String getGuid() {
        return getParentAgentGuid();
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
                cache = new CacheManager(getParentAgent(), policy);
                registerService(cache);
            }
            return cache;
        }
    }
    /** get or create receiver service */
    public Receiver getReceiver() {
        if (receiver != null) {
            return receiver;
        }
        synchronized (this) {
            if (receiver == null) {
                receiver = new AgentReceiverService(getParentAgent());
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
                agentReports = new AgentReportsImpl(getParentAgent());
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
                flow = new AgentFlowImpl(getParentAgent());
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
                semaphores = new AgentSemaphoresImpl(getParentAgent());
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
                ml = new AgentMachineLearningImpl(getParentAgent());
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
                storages = new StoragesImpl(getParentAgent());
                registerService(storages);
            }
            return storages;
        }
    }
    /** get service for managing spaces */
    public AgentSpaces getSpaces() {
        if (agentSpaces != null) {
            return agentSpaces;
        }
        synchronized (this) {
            if (agentSpaces == null) {
                agentSpaces = new AgentSpacesImpl(getParentAgent());
                registerService(agentSpaces);
            }
            return agentSpaces;
        }
    }
    /** get service for security */
    public AgentSecurity getSecurity() {
        if (security != null) {
            return security;
        }
        synchronized (this) {
            if (security == null) {
                security = new AgentSecurityImpl(getParentAgent());
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
                objects = new AgentObjectsImpl(getParentAgent());
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
        return services.values().stream().map(DistService::getServiceUid).collect(Collectors.toList());
    }
    /** get types of registered services */
    public Set<String> getServiceTypes() {
        return services.keySet();
    }
    /** get basic information about service for given type of UID */
    public DistServiceInfo getServiceInfo(String serviceUid) {
        DistService srv = services.get(serviceUid);
        if (srv != null) {
            srv.getServiceInfo();
        }
        return null;
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
    public AgentWebApiResponse handleRequest(AgentWebApiRequest request) {
        DistService service = services.get(request.getServiceName());
        if (service != null) {
            return service.handleRequest(request);
        }
        //  no service found, returning 404
        return new AgentWebApiResponse(404, AgentWebApiRequest.headerText, "No service for name: " + request.getServiceName());
    }
    /** close */
    public void close() {
        log.info("Closing all registered services with agent, services: " + services.size());
    }

}
