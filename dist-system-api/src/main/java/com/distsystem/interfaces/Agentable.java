package com.distsystem.interfaces;

import com.distsystem.api.*;
import com.distsystem.utils.ResolverManager;

import java.time.LocalDateTime;
import java.util.List;

/** basic interface for any agentable object that has parent Agent */
public interface Agentable {
    /** get parent Agent */
    Agent getAgent();
    /** get created date and time */
    LocalDateTime getCreateDate();
    /** get GUID of this agentable object */
    String getGuid();
    /** get name of this agentable object */
    String getAgentableName();
    /** */
    long getCurrentWorkingTimeMs();
    /** get configuration of parent Agent*/
    DistConfig getConfig();
    /** search for items inside agentable object - to be overriden by service, component or any other class */
    List<AgentSearchResultItem> search(AgentSearchQuery query);
    /** touch this object which is changing lastUpdatedDate */
    void touch();
    /** get resolver manager to resolve String values */
    ResolverManager getResolverManager();
    /** resolve any String value */
    String resolve(String value);
    /** get GUID of parent Agent */
    String getParentAgentGuid();
    /** add Issue to parent agent */
    void addIssueToAgent(String methodName, Exception ex);
    /** create event */
    void createEvent(AgentEvent event);
    /** create event */
    void createEvent(String method, String eventType, Object[] params);
    /** */
    void createEvent(String method, String eventType);
    /** */
    void createEvent(String method, String eventType, String param);
    /** create event */
    void createEvent(String method);
    /** is object is closed*/
    boolean isClosed();
    /** if object is initialized */
    boolean isInitialized();
    /** close current object */
    void close();
    /** receive message that belongs to parent agent */
    void receiveMessageToAgent(DistMessage receivedMsg);

}
