package com.distsystem.interfaces;

import com.distsystem.api.CacheEvent;

import java.util.Map;
import java.util.Queue;
import java.util.function.Function;

/** interface of agent event manager - object to store events,  */
public interface AgentEvents {
    /** */
    void addCallbackMethods(Map<String, Function<CacheEvent, String>> callbacksMethods);

    /** add new event and distribute it to callback methods,
     * event could be useful information about change of cache status, new connection, refresh of cache, clean */
    void addEvent(CacheEvent event);

    /** set new callback method for events for given type */
    void setCallback(String eventType, Function<CacheEvent, String> callback);

    /** get all recent events added to cache */
    Queue<CacheEvent> getEvents();

    /** close */
    void close();
}
