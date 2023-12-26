package com.distsystem.api.info;

import com.distsystem.api.AgentEvent;
import com.distsystem.api.dtos.DistAgentEventRow;

import java.time.LocalDateTime;
import java.util.List;

/** info about events */
public class EventsInfo {

    private final long addedEventsCount;
    private final long eventsCount;
    private final LocalDateTime lastAddedDate;
    private final List<DistAgentEventRow> latestEvents;

    /** events info */
    public EventsInfo(long addedEventsCount, long eventsCount, LocalDateTime lastAddedDate, List<DistAgentEventRow> latestEvents) {
        this.addedEventsCount = addedEventsCount;
        this.eventsCount = eventsCount;
        this.lastAddedDate = lastAddedDate;
        this.latestEvents = latestEvents;
    }
    public long getAddedEventsCount() {
        return addedEventsCount;
    }
    public long getEventsCount() {
        return eventsCount;
    }
    public String getLastAddedDate() {
        return lastAddedDate.toString();
    }
    public List<DistAgentEventRow> getLatestEvents() {
        return latestEvents;
    }
}
