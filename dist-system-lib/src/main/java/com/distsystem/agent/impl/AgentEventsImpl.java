package com.distsystem.agent.impl;

import com.distsystem.api.*;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.api.info.EventsInfo;
import com.distsystem.base.ServiceBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentEvents;
import com.distsystem.utils.DistWebApiProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/** implementation of issues manager */
public class AgentEventsImpl extends ServiceBase implements AgentEvents {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(AgentEventsImpl.class);
    /** callbacks - methods to be called when given event is happening
     * only one callback per event type is allowed */
    protected java.util.concurrent.ConcurrentHashMap<String, Function<AgentEvent, String>> callbacks = new java.util.concurrent.ConcurrentHashMap<>();
    /** queue of events that would be added to callback methods */
    protected final Queue<AgentEvent> events = new LinkedList<>();
    /** created counter */
    private final AtomicLong addedEventsCount = new AtomicLong();
    /** */
    private LocalDateTime lastAddedDate = LocalDateTime.now();


    /** creates new manager for events in agent */
    public AgentEventsImpl(Agent parentAgent) {
        super(parentAgent);
        parentAgent.getAgentServices().registerService(this);
    }

    /** get type of service: cache, measure, report, flow, space, ... */
    public DistServiceType getServiceType() {
        return DistServiceType.events;
    }
    /** process message, returns message with status */
    public DistMessage processMessage(DistMessage msg) {
        return msg.notSupported();
    }

    /** update configuration of this Service to add registrations, services, servers, ... */
    public void updateConfig(DistConfig newCfg) {
    }

    /** additional web API endpoints */
    protected DistWebApiProcessor additionalWebApiProcessor() {
        return new DistWebApiProcessor(getServiceType())
                .addHandlerGet("events", (m, req) -> req.responseOkJsonSerialize(events.stream().map(e -> e.getEventRow()).toList()))
                .addHandlerPost("clear", (m, req) -> req.responseOkJsonSerialize(clearEvents()))
                .addHandlerGet("info", (m, req) -> req.responseOkJsonSerialize(getInfo()));
    }
    /** read configuration and re-initialize this component */
    public boolean reinitialize() {
        // nothing to be done here
        return true;
    }
    /** add callback method to call back when there is any event going on */
    public void addCallbackMethods(Map<String, Function<AgentEvent, String>> callbacksMethods) {
        callbacksMethods.entrySet().stream().forEach(cb -> callbacks.put(cb.getKey(), cb.getValue()));
    }

    /** get info about events */
    public EventsInfo getInfo() {
        //events.stream().toList().subList(0, 99);
        return new EventsInfo(addedEventsCount.get(), events.size(), lastAddedDate);
    }
    /** clear events */
    public EventsInfo clearEvents() {
        events.clear();
        return getInfo();
    }
    /** add new event and distribute it to callback methods,
     * event could be useful information about change of cache status, new connection, refresh of cache, clean */
    public void addEvent(AgentEvent event) {
        synchronized (events) {
            addedEventsCount.incrementAndGet();
            events.add(event);
            lastAddedDate = LocalDateTime.now();
            while (events.size() > parentAgent.getConfig().getPropertyAsLong(DistConfig.AGENT_CACHE_EVENTS_MAX_COUNT, DistConfig.AGENT_CACHE_EVENTS_MAX_COUNT_VALUE)) {
                events.poll();
            }
        }
        Function<AgentEvent, String> callback = callbacks.get(event.getEventType());
        if (callback != null) {
            try {
                callback.apply(event);
            } catch (Exception ex) {
                log.warn("Exception while running callback for event " + event.getEventType());
            }
        }
    }
    /** set new callback method for events for given type */
    public void setCallback(String eventType, Function<AgentEvent, String> callback) {
        log.info("Set callback method for events" + eventType);
        callbacks.put(eventType, callback);
    }
    /** get all recent events added to cache */
    public Queue<AgentEvent> getEvents() {
        return events;
    }
    /** close */
    protected void onClose() {
        synchronized (events) {
            log.info("Closing events, clearing ALL, count: " + events.size());
            events.clear();
        }
    }

}
