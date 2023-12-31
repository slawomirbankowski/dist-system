package com.distsystem.agent.services;

import com.distsystem.api.*;
import com.distsystem.api.dtos.DistAgentEventRow;
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
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

/** service for events generated by all agent services and subscription to specific type of events
 * each event has: parent component, method, type, parameters, creation date */
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
    /** last date and time of added an event */
    private LocalDateTime lastAddedDate = LocalDateTime.now();

    /** creates new manager for events in agent */
    public AgentEventsImpl(Agent parentAgent) {
        super(parentAgent);
        parentAgent.getServices().registerService(this);
    }

    /** count objects in this agentable object including this object */
    public long countObjectsService() {
        // TODO: calculate counts of objects
        return 2L;
    }

    /** get type of service: cache, measure, report, flow, space, ... */
    public DistServiceType getServiceType() {
        return DistServiceType.events;
    }
    /** get description of this service */
    public String getServiceDescription() {
        return "Service to register events from Agent to check what was happening.";
    }

    /** update configuration of this Service to add registrations, services, servers, ... */
    public void updateConfig(DistConfig newCfg) {
    }

    /** additional web API endpoints */
    protected DistWebApiProcessor additionalWebApiProcessor() {
        return new DistWebApiProcessor(getServiceType())
                .addHandlerGet("events", (m, req) -> req.responseOkJsonSerialize(getEventRowsLast()))
                .addHandlerGet("events-search", (m, req) -> req.responseOkJsonSerialize(getEventRowsSearch(req.getParamOne())))
                .addHandlerGet("callback-keys", (m, req) -> req.responseOkJsonSerialize(callbacks.keys()))
                .addHandlerPost("clear", (m, req) -> req.responseOkJsonSerialize(clearEvents()))
                .addHandlerGet("events-count", (m, req) -> req.responseOkText(""+events.size()))
                .addHandlerGet("events-by-method", (m, req) -> req.responseOkJsonSerialize(eventsByMethod()))
                .addHandlerGet("events-by-type", (m, req) -> req.responseOkJsonSerialize(eventsByType()))
                .addHandlerGet("info", (m, req) -> req.responseOkJsonSerialize(getInfo()));
    }
    /** read configuration and re-initialize this component */
    protected boolean onReinitialize() {
        // nothing to be done here
        return true;
    }
    /** add callback method to call back when there is any event going on */
    public void addCallbackMethods(Map<String, Function<AgentEvent, String>> callbacksMethods) {
        callbacksMethods.entrySet().stream().forEach(cb -> callbacks.put(cb.getKey(), cb.getValue()));
    }

    /** get info about events */
    public EventsInfo getInfo() {
        return new EventsInfo(addedEventsCount.get(), events.size(), lastAddedDate,
                events.stream().limit(5).map(AgentEvent::getEventRow).collect(Collectors.toList()));
    }
    /** get number of events per method */
    public Map<String, Long> eventsByMethod() {
        return events.stream().collect(Collectors.groupingBy(AgentEvent::getMethod, Collectors.counting()));
    }
    /** get number of events by type */
    public Map<String, Long> eventsByType() {
        return events.stream().collect(Collectors.groupingBy(AgentEvent::getEventType, Collectors.counting()));
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
                addIssueToAgent("addEvent", ex);
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

    /** get event rows for last 100 events */
    public List<DistAgentEventRow> getEventRowsLast() {
        return events.stream().limit(100).map(e -> e.getEventRow()).toList();
    }
    /** get event rows with search */
    public List<DistAgentEventRow> getEventRowsSearch(String str) {
        return events.stream().filter(e -> e.search(str)).limit(100).map(e -> e.getEventRow()).toList();
    }
    /** close */
    protected void onClose() {
        synchronized (events) {
            log.info("Closing events, clearing ALL, count: " + events.size());
            events.clear();
        }
    }

}
