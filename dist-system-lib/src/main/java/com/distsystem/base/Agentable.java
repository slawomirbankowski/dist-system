package com.distsystem.base;

import com.distsystem.api.AgentEvent;
import com.distsystem.api.DistConfig;
import com.distsystem.api.DistMessage;
import com.distsystem.interfaces.Agent;
import com.distsystem.utils.DistUtils;
import com.distsystem.utils.ResolverManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/** base class to keep agent - this is for classes that are keeping parent Agent object */
public abstract class Agentable {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(Agentable.class);
    /** date ant time of creation for this object */
    protected final LocalDateTime createDate = LocalDateTime.now();
    /** UUID for agentable object - globally unique identifier */
    protected final String guid;
    /** parent agent for this class */
    protected Agent parentAgent;
    /** if object has been already closed */
    protected boolean closed = false;
    /** last touch date of this agentable instance of class */
    protected LocalDateTime lastTouchDate = LocalDateTime.now();

    /** creates new Agentable class with parent Agent */
    public Agentable(Agent parentAgent) {
        this.parentAgent = parentAgent;
        this.guid = createGuid();
    }
    /** get agent */
    public Agent getAgent() {
        return parentAgent;
    }
    /** get date and time of creation */
    public LocalDateTime getCreateDate() {
        return createDate;
    }
    /** get unique ID of this object */
    public String getGuid() {
        return guid;
    }

    /** get current working time in milliseconds */
    public long getCurrentWorkingTimeMs() {
        return ChronoUnit.MILLIS.between(createDate, LocalDateTime.now());
    }
    /** get configuration for parent agent */
    public DistConfig getConfig() {
        return parentAgent.getConfig();
    }
    /** create unique agentable UID */
    protected String createGuid() {
        return DistUtils.generateCustomGuid("OBJ_" + parentAgent.getAgentShortGuid());
    }
    /** touch this object */
    protected void touch() {
        lastTouchDate = LocalDateTime.now();
    }
    /** get resolver manager to resolve names, values, properties */
    public ResolverManager getResolverManager() {
        return parentAgent.getConfig().getResolverManager();
    }
    /** method to resolve any parameter, value, setting from raw value with ${key} into final value translated and resolved using all defined Resolvers for this Agent */
    public String resolve(String value) {
        return parentAgent.getConfig().getResolverManager().resolve(value);
    }
    /** get GUID of parent agent */
    public String getParentAgentGuid() {
        return parentAgent.getAgentGuid();
    }
    /** add issue to parent agent */
    protected void addIssueToAgent(String methodName, Exception ex) {
        parentAgent.addIssue(methodName, ex);
    }
    /** add event to parent agent */
    protected void addEventToAgent(AgentEvent event) {
        parentAgent.getAgentEvents().addEvent(event);
    }
    /** add event to parent agent */
    protected void addEventToAgent(String method, String eventType, Object... params) {
        parentAgent.getAgentEvents().addEvent(new AgentEvent(this, method, eventType, params));
    }

    /** if object have been closed */
    public boolean isClosed() {
        return closed;
    }
    /** close all items in this object */
    protected abstract void onClose();
    /** close and deinitialize object (service, component, ...) - remove all items, disconnect from all storages, stop all timers */
    public final synchronized void close() {
        if (closed) {
            log.warn("Agentable object is already closed for UID: " + guid);
        } else {
            touch();
            closed = true;
            log.info("Closing Agentable object for GUID: " + guid);
            onClose();
        }
    }
    /** receive message from connector or server and process by parent Agent,
     * need to find service and process that message on service */
    protected void receiveMessageToAgent(DistMessage receivedMsg) {
        touch();
        parentAgent.getAgentServices().receiveMessage(receivedMsg);
    }

}
