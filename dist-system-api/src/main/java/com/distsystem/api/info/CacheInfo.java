package com.distsystem.api.info;

import java.time.LocalDateTime;
import java.util.List;

/** simple info about cache - this is to be returned in dist-cache application */
public class CacheInfo {

    private String cacheManagerGuid;
    private LocalDateTime createdDateTime;
    private long checkSequence;
    private long addedItemsSequence;
    private boolean isClosed;
    private long issuesCount;
    private long eventsCount;
    private int itemsCount;
    private int objectsCount;
    /** */
    private List<StorageInfo> storages;

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

