package com.distsystem.base;

import com.distsystem.api.*;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.Agentable;
import com.distsystem.utils.DistUtils;
import com.distsystem.utils.ResolverManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;

/** base class to keep agent - this is for classes that are keeping parent Agent object */
public abstract class AgentableBase implements Agentable {

    /** maximum number of locally stored historical requests */
    public final int MAX_REQUESTS = 100;
    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(AgentableBase.class);
    /** date ant time of creation for this object */
    protected final LocalDateTime createDate = LocalDateTime.now();
    /** UUID for agentable object - globally unique identifier */
    protected final String guid;
    /** parent agent for this class - could be null if this service in an Agent!!! */
    protected Agent parentAgent;
    /** if object has been already closed */
    protected boolean closed = false;
    /** if object has been already initialized */
    protected boolean initialized = false;
    /** last touch date of this agentable instance of class */
    protected LocalDateTime lastTouchDate = LocalDateTime.now();
    /** service events - only last 100 is stored */
    protected final LinkedList<AgentEvent> agentableEvents = new LinkedList<>();

    /** creates new Agentable class with parent Agent */
    public AgentableBase(Agent parentAgent) {
        this.parentAgent = parentAgent;
        this.guid = createGuid();
        log.debug("Creating agentable object for agent: " + ((parentAgent!=null)?parentAgent.getAgentGuid():"NO_AGENT") + ", class: " + this.getClass().getName() + ", guid: " + guid);
    }
    /** get agent */
    @Override
    public Agent getAgent() {
        return parentAgent;
    }
    /** get date and time of creation */
    @Override
    public LocalDateTime getCreateDate() {
        return createDate;
    }
    /** get unique ID of this object */
    @Override
    public String getGuid() {
        return guid;
    }
    /** get name of this agentable object */
    public String getAgentableName() {
        return this.getClass().getSimpleName();
    }
    /** get current working time in milliseconds */
    @Override
    public long getCurrentWorkingTimeMs() {
        return ChronoUnit.MILLIS.between(createDate, LocalDateTime.now());
    }
    /** get configuration for parent agent */
    @Override
    public DistConfig getConfig() {
        return parentAgent.getConfig();
    }
    /** create unique agentable UID */
    protected String createGuid() {
        return DistUtils.generateCustomGuid("OBJ_" + parentAgent.getAgentShortGuid());
    }
    /** touch this object */
    @Override
    public void touch() {
        createEvent("touch");
        lastTouchDate = LocalDateTime.now();
    }
    /** count objects in this agentable object including this object */
    public long countObjects() {
        return 6L + agentableEvents.size()*5L + countObjectsAgentable();
    }
    /** search for items inside agentable object - to be overriden by service, component or any other class */
    public List<AgentSearchResultItem> search(AgentSearchQuery query) {
        return onSearch(query);
    }
    /** to override */
    protected List<AgentSearchResultItem> onSearch(AgentSearchQuery query) {
        
        return List.of();
    }

    /** count objects in this agentable object including this object */
    protected abstract long countObjectsAgentable();
    /** get resolver manager to resolve names, values, properties */
    @Override
    public ResolverManager getResolverManager() {
        return parentAgent.getConfig().getResolverManager();
    }
    /** method to resolve any parameter, value, setting from raw value with ${key} into final value translated and resolved using all defined Resolvers for this Agent */
    @Override
    public String resolve(String value) {
        return parentAgent.getConfig().getResolverManager().resolve(value);
    }
    /** get GUID of parent agent */
    @Override
    public String getParentAgentGuid() {
        return parentAgent.getAgentGuid();
    }
    /** add issue to parent agent */
    @Override
    public void addIssueToAgent(String methodName, Exception ex) {
        parentAgent.addIssue(methodName, ex);
    }
    /** create new event */
    @Override
    public void createEvent(AgentEvent event) {
        agentableEvents.add(event);
        while (agentableEvents.size()>MAX_REQUESTS) {
            agentableEvents.pop();
        }
        parentAgent.addEvent(event);
    }
    /** create new event */
    @Override
    public void createEvent(String method, String eventType, Object[] params) {
        createEvent(new AgentEvent(this, method, eventType, params));
    }
    /** create new event */
    @Override
    public void createEvent(String method, String eventType) {
        createEvent(new AgentEvent(this, method, eventType));
    }
    /** create new event */
    @Override
    public void createEvent(String method, String eventType, String param) {
        createEvent(new AgentEvent(this, method, eventType, param));
    }
    @Override
    public void createEvent(String method) {
        createEvent(new AgentEvent(this, method, method));
    }

    /** if object have been closed */
    @Override
    public final boolean isClosed() {
        return closed;
    }
    /** if object have been initialized */
    @Override
    public boolean isInitialized() {
        return initialized;
    }

    /** to override - close this agentable object */
    protected abstract void onClose();

    /** close and deinitialize object (service, component, ...) - remove all items, disconnect from all storages, stop all timers */
    @Override
    public final synchronized void close() {
        createEvent("close");
        if (closed) {
            log.warn("Agentable object is already closed for UID: " + guid);
        } else {
            touch();
            closed = true;
            log.debug("Closing Agentable object for GUID: " + guid);
            onClose();
        }
    }
    /** receive message from connector or server and process by parent Agent,
     * need to find service and process that message on service */
    @Override
    public void receiveMessageToAgent(DistMessage receivedMsg) {
        touch();
        parentAgent.getServices().receiveMessage(receivedMsg);
    }

}
