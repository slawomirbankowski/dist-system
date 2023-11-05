package com.distsystem.api.info;

import java.time.LocalDateTime;

/** info about events */
public class EventsInfo {

    private long addedEventsCount;
    private long eventsCount;
    private LocalDateTime lastAddedDate;

    /** events info */
    public EventsInfo(long addedEventsCount, long eventsCount, LocalDateTime lastAddedDate) {
        this.addedEventsCount = addedEventsCount;
        this.eventsCount = eventsCount;
        this.lastAddedDate = lastAddedDate;
    }


}
