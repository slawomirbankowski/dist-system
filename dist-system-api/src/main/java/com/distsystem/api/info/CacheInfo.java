package com.distsystem.api.info;

import java.time.LocalDateTime;
import java.util.List;

/** simple info about cache - this is to be returned in dist-cache application */
public class CacheInfo {

    private final String cacheManagerGuid;
    private final LocalDateTime createdDateTime;
    private final long checkSequence;
    private final long addedItemsSequence;
    private final boolean isClosed;
    private final long issuesCount;
    private final long eventsCount;
    private final int itemsCount;
    private final int objectsCount;
    /** */
    private final List<StorageInfo> storages;

    public CacheInfo(String cacheManagerGuid, LocalDateTime createdDateTime, long checkSequence, long addedItemsSequence,
                     boolean isClosed, long issuesCount, long eventsCount, int itemsCount, int objectsCount,
                     List<StorageInfo> storages) {
        this.cacheManagerGuid = cacheManagerGuid;
        this.createdDateTime = createdDateTime;
        this.checkSequence = checkSequence;
        this.addedItemsSequence = addedItemsSequence;
        this.isClosed = isClosed;
        this.issuesCount = issuesCount;
        this.eventsCount = eventsCount;
        this.itemsCount = itemsCount;
        this.objectsCount = objectsCount;
        this.storages = storages;
    }

    public String getCacheManagerGuid() {
        return cacheManagerGuid;
    }

    public String getCreatedDateTime() {
        return createdDateTime.toString();
    }

    public long getCheckSequence() {
        return checkSequence;
    }

    public long getAddedItemsSequence() {
        return addedItemsSequence;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public long getIssuesCount() {
        return issuesCount;
    }

    public long getEventsCount() {
        return eventsCount;
    }

    public int getItemsCount() {
        return itemsCount;
    }

    public int getObjectsCount() {
        return objectsCount;
    }
    public List<StorageInfo> getStorages() {
        return storages;
    }
}

