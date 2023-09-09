package com.distsystem.interfaces;

import com.distsystem.api.*;
import com.distsystem.base.dtos.DistAgentServiceRow;

import java.util.List;
import java.util.Set;

/** interface for service manager
 * there are many services that would be working with Dist system, example:
 * cache, reports, flow, measures, space, scheduler, security, config
 * Full list of services are in DistServiceType.
 * Each service might be having many components.
 * */
public interface AgentServices {

    /** return all services assigned to this agent */
    List<DistService> getServices();
    /** set new policy for services */
    void setPolicy(CachePolicy policy);
    /** get or create cache connected with this Agent */
    Cache getCache();
    /** get or create service for reports to create, update, remove or execute reports */
    AgentReports getReports();
    /** get or create receiver service */
    Receiver getReceiver();
    /** get flow service */
    AgentFlow getFlow();
    /** get semaphores service */
    AgentSemaphores getSemaphores();
    /** get ML service */
    AgentMachineLearning getMl();
    /** get service for managing shared objects */
    AgentObjects getObjects();
    /** get number of services */
    int getServicesCount();
    /** get keys of registered services */
    List<String> getServiceKeys();
    /** get types of registered services */
    Set<String> getServiceTypes();
    /** get basic information about service for given type of UID */
    DistServiceInfo getServiceInfo(String serviceUid);
    /** get basic information about all services */
    List<DistServiceInfo> getServiceInfos();
    /** get all rows of services to registrations */
    List<DistAgentServiceRow> getServiceRows();
    /** register service to this agent */
    void registerService(DistService service) ;
    /** receive message from connector or server, need to find service and process that message on service */
    void receiveMessage(DistMessage msg);
    /** handle API request in this Web API for Agent */
    AgentWebApiResponse handleRequest(AgentWebApiRequest request);
    /** close  */
    void close();

}
