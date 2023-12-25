package com.distsystem.interfaces;

import com.distsystem.api.*;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.api.info.AgentServiceSimpleInfo;
import com.distsystem.api.info.DistConfigGroupInfo;
import com.distsystem.api.dtos.DistAgentServiceRow;
import com.distsystem.utils.DistWebApiProcessor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** basic interface for service in distributed environment
 * service is a module or class that is cooperating with agent, could be registered
 * */
public interface DistService {

    /** get date and time of creating service */
    LocalDateTime getCreateDate();
    /** get type of service: cache, measure, report, flow, space, ... */
    DistServiceType getServiceType();
    /** get description of this service */
    String getServiceDescription();
    /** get info object for configuration group used by this service */
    DistConfigGroupInfo getConfigGroupInfo();
    /** get info object for configuration group used by this service */
    DistConfigGroup getConfigGroup();
    /** count objects in this agentable object including this object */
    long countObjects();
    /** get keys of all components added to this service */
    List<String> getComponentKeys();
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
    /** handle Web API simple request
     * method: GET, POST, PUT, DELETE
     * serviceMethod: service method to be run: ping, last-touch-date */
    AgentWebApiResponse handleSimpleRequest(String method, String serviceMethod, Map<String, List<String>> headers);
    /** get current web api processor to handle web requests */
    DistWebApiProcessor getWebApiProcessor();
    /** get the latest requests to this service */
    List<AgentWebApiEvent> getLatestRequests();
    /** get the latest messages to this service */
    List<DistMessageSimple> getLatestMessages();
    /** get all API handlers */
    List<String> getAllHandlers();
    /** get unique ID of this object */
    String getGuid();
    /** get basic information about service */
    DistServiceInfo getServiceInfo();
    /** get simple information about service */
    AgentServiceSimpleInfo getServiceSimpleInfo();
    /** get row for registration services */
    DistAgentServiceRow getServiceRow();
    /** get configuration for cache */
    DistConfig getConfig();
    /** update configuration of this Service to add registrations, services, servers, ... */
    void updateConfig(DistConfig newCfg);
    /** change values in configuration bucket */
    void initializeConfigBucket(DistConfigBucket bucket);
    /** run after initialization */
    void afterInitialization();
    /** close and deinitialize service */
    void close();

}
