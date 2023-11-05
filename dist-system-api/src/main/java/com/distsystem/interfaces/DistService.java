package com.distsystem.interfaces;

import com.distsystem.api.*;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.base.dtos.DistAgentServiceRow;

import java.time.LocalDateTime;

/** basic interface for service in distributed environment
 * service is a module or class that is cooperating with agent, could be registered
 * */
public interface DistService {

    /** get date and time of creating service */
    LocalDateTime getCreateDate();
    /** get type of service: cache, measure, report, flow, space, ... */
    DistServiceType getServiceType();
    /** read configuration and re-initialize this service */
    boolean reinitialize();
    /** add component to this service */
    void addComponent(AgentComponent component);
    /** get parent Agent */
    Agent getAgent();
    /** process message, returns message with status */
    DistMessage processMessage(DistMessage msg);
    /** handle API request in this Web API for this service */
    AgentWebApiResponse handleRequest(AgentWebApiRequest request);
    /** get unique ID of this object */
    String getGuid();
    /** get basic information about service */
    DistServiceInfo getServiceInfo();
    /** get row for registration services */
    DistAgentServiceRow getServiceRow();
    /** get configuration for cache */
    DistConfig getConfig();
    /** update configuration of this Service to add registrations, services, servers, ... */
    void updateConfig(DistConfig newCfg);
    /** close and deinitialize service */
    void close();

}
