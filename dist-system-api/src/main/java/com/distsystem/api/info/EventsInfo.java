package com.distsystem.api.info;

import java.time.LocalDateTime;

/** info about events */
public class EventsInfo {

    private final long addedEventsCount;
    private final long eventsCount;
    private final LocalDateTime lastAddedDate;

    /** events info */
    public EventsInfo(long addedEventsCount, long eventsCount, LocalDateTime lastAddedDate) {
        this.addedEventsCount = addedEventsCount;
        this.eventsCount = eventsCount;
        this.lastAddedDate = lastAddedDate;
    }
    public long getAddedEventsCount() {
        return addedEventsCount;
    }
    public long getEventsCount() {
        return eventsCount;
    }
    public LocalDateTime getLastAddedDate() {
        return lastAddedDate;
    }
}
