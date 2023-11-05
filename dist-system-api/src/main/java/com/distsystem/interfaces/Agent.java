package com.distsystem.interfaces;

import com.distsystem.api.*;
import com.distsystem.api.enums.DistMessageType;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.api.info.AgentInfo;

import java.time.LocalDateTime;
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
    /** get short version ID of this agent GUID */
    String getAgentShortGuid();
    /** get distributed group name */
    String getDistGroup();
    /** get distributed system name */
    String getDistName();
    /** get current agent name */
    String getAgentName();
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
    /** check if Agent configuration has given property by name */
    boolean hasConfigProperty(String propName);
    /** get secret generated or set for this agent */
    String getAgentSecret();
    /** get component to read configuration from external sources */
    AgentConfigReader getConfigReader();
    /** get agent threads manager */
    AgentThreads getAgentThreads();
    /** get agent timers manager */
    AgentTimers getAgentTimers();
    /** get agent service manager */
    AgentServices getAgentServices();
    /** get agent connector manager to manage direct connections to other agents, including sending and receiving messages */
    AgentConnectors getAgentConnectors();
    /** get agent registration manager to register this agent in global repositories (different types: JDBC, Kafka, App, Elasticsearch, ... */
    AgentRegistrations getAgentRegistrations();
    /** get agent events manager to add events and set callbacks */
    AgentEvents getAgentEvents();
    /** get agent issuer manager add issues */
    AgentIssues getAgentIssues();
    /** get agent DAOs manager for external connections to JDBC, Elasticsearch, Kafka, Redis */
    AgentDao getAgentDao();
    /** get WebAPI for this Agent */
    AgentApi getAgentApi();

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
    DistConfigGroup registerConfigGroup(String groupName);

    /** close all items in this agent */
    void close();
    /** wait in this thread till agent would be killed by external command, WebAPI request or closed by issue */
    void waitTillKill();
    /** set this Agent instance as default one for current JVM */
    Agent setAsDefaultAgent();
}
