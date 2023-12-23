package com.distsystem.interfaces;

import com.distsystem.api.*;
import com.distsystem.api.enums.DistMessageType;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.api.info.AgentInfo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/** interfaces for agent in distributed environment
 * this is to communicate among all distributed services
 * Agent is having:
 * - Registration services to register agents and get list of agents/servers/services in Distributed System
 * - Services - local services like cache, reports, security, flow, schedule - list of services could be found in DistServiceType
 * - WebAPI as technical access point through HTTP
 * - Servers to connect other agents, Servers can be implemented with different technology like: Socket text, Socket binary, HTTP, Kafka, ...
 * - Clients to other agents - to send direct and broadcast messages
 * - Thread management service - to manage threads inside Dist services
 * - Issue management - to add issues from all local services
 *
 * */
public interface Agent extends DistService, IssueHandler {
    /** get unique ID of this agent */
    String getAgentGuid();
    /** get total initialization agent time in milliseconds */
    long getTotalInitializationTime();
    /** get short version ID of this agent GUID */
    String getAgentShortGuid();
    /** get start time of this agent -  System.currentTimeMillis() */
    long getAgentStartTime();
    /** get distributed group name */
    String getDistGroup();
    /** get distributed system name */
    String getDistName();
    /** get current agent name */
    String getAgentName();
    /** get name of current environment */
    String getEnvironmentName();
    /** get configuration for this agent */
    DistConfig getConfig();
    /** get high-level information about this agent */
    AgentInfo getAgentInfo();
    /** returns unmodificable set of Agent tags */
    Set<String> getAgentTags();
    /** get serializer/deserializer helper to serialize/deserialize objects when sending through connectors or saving to external storages */
    DistSerializer getSerializer();
    /** returns true if agent has been already closed */
    boolean isClosed();
    /** initialize agent - server, application, jdbc, kafka */
    void initializeAgent();
    /** get date and time of creating this agent */
    LocalDateTime getCreateDate();
    /** count all objects in agent - all services objects */
    long countAgentObjects();
    /** list all endpoints for all services */
    List<String> listAllEndpoints();
    /** check if Agent configuration has given property by name */
    boolean hasConfigProperty(String propName);
    /** get secret generated or set for this agent */
    String getAgentSecret();
    /** get component to read configuration from external sources */
    AgentConfigReader getConfigReader();
    /** get agent threads manager */
    AgentThreads getThreads();
    /** get agent timers manager */
    AgentTimers getTimers();
    /** get agent service manager */
    AgentServices getServices();
    /** get agent connector manager to manage direct connections to other agents, including sending and receiving messages */
    AgentConnectors getConnectors();
    /** get agent registration manager to register this agent in global repositories (different types: JDBC, Kafka, App, Elasticsearch, ... */
    AgentRegistrations getRegistrations();
    /** get agent events manager to add events and set callbacks */
    AgentEvents getEvents();
    /** get agent issuer manager add issues */
    AgentIssues getIssues();
    /** get agent DAOs manager for external connections to JDBC, Elasticsearch, Kafka, Redis */
    AgentDao getAgentDao();
    /** get WebAPI for this Agent */
    AgentApi getApi();
    /** get authentication service for managing accounts and login into identity services */
    AgentAuth getAuth();
    /** get receiver service to get data from different sources */
    Receiver getReceiver();
    /** get cache connected with this Agent to have copy of */
    Cache getCache();
    /** get service for reports to create, update, remove or execute reports */
    AgentReports getReports();
    /** get service for storages */
    Storages getStorages();
    /** get service for managing spaces - shared spaces with objects that could be modified by any agent */
    AgentSpace getSpace();
    /** get service for security for managing priviliges, roles, objects */
    AgentSecurity getSecurity();
    /** get flow service for data flows */
    AgentFlow getFlow();
    /** get semaphores service for semaphores and mutexes */
    AgentSemaphores getSemaphores();
    /** get ML service for Machine Learning models */
    AgentMachineLearning getMl();
    /** get service for managing shared objects */
    AgentObjects getObjects();
    /** get monitor service */
    AgentMonitor getMonitor();
    /** get measure service */
    AgentMeasure getMeasure();
    /** get notification service to send notifications */
    AgentNotification getNotification();
    /** get schedule for service */
    AgentSchedule getSchedule();
    /** get service for version */
    AgentVersion getVersion();
    /** create new message builder starting this agent */
    DistMessageBuilder createMessageBuilder();
    /** message send to agent(s) */
    DistMessageFull sendMessage(DistMessageFull msg);
    /** send message to agents */
    DistMessageFull sendMessage(DistMessage msg, DistCallbacks callbacks);
    /** create message send to all clients */
    DistMessageFull sendMessageBroadcast(DistMessageType messageType, DistServiceType fromService, DistServiceType toService, String method, Object message, String tags, LocalDateTime validTill, DistCallbacks callbacks);
    DistMessageFull sendMessageBroadcast(DistMessageType messageType, DistServiceType fromService, DistServiceType toService, String method, Object message, String tags, DistCallbacks callbacks);
    DistMessageFull sendMessageBroadcast(DistMessageType messageType, DistServiceType fromService, DistServiceType toService, String method, Object message, DistCallbacks callbacks);
    DistMessageFull sendMessageBroadcast(DistServiceType fromService, DistServiceType toService, String method, Object message, DistCallbacks callbacks);
    DistMessageFull sendMessage(DistService fromService, String toAgent, DistServiceType toService, String method, Object message, DistCallbacks callbacks);
    DistMessageFull sendMessageAny(DistService fromService, DistServiceType toService, String method, Object message, DistCallbacks callbacks);

    /** register new config group */
    DistConfigGroup registerConfigGroup(String groupName, DistService parentService);
    /** create new event and add it to events */
    void addEvent(AgentEvent event);

    /** close all items in this agent */
    void close();
    /** wait in this thread till agent would be killed by external command, WebAPI request or closed by issue */
    void waitTillKill();
    /** set this Agent instance as default one for current JVM */
    Agent setAsDefaultAgent();
}
