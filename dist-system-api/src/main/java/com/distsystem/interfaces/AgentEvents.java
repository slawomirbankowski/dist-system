package com.distsystem.interfaces;

import com.distsystem.api.AgentEvent;
import com.distsystem.api.info.EventsInfo;

import java.util.Map;
import java.util.Queue;
import java.util.function.Function;

/** interface of agent event manager - object to store events,  */
public interface AgentEvents extends DistService {

    /** add callback methods to specific event types*/
    void addCallbackMethods(Map<String, Function<AgentEvent, String>> callbacksMethods);
    /** add new event and distribute it to callback methods,
     * event could be useful information about change of cache status, new connection, refresh of cache, clean */
    void addEvent(AgentEvent event);
    /** set new callback method for events for given type */
    void setCallback(String eventType, Function<AgentEvent, String> callback);
    /** get all recent events added to cache */
    Queue<AgentEvent> getEvents();
    /** get number of events per method */
    Map<String, Long> eventsByMethod();
    /** get number of events by type */
    Map<String, Long> eventsByType();
    /** get info about events */
    EventsInfo getInfo();
    /** clear events */
    EventsInfo clearEvents();

}
