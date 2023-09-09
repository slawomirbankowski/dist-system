package com.distsystem.api.info;

import com.distsystem.api.CacheMode;

/** simple info about object in cache */
public class CacheObjectInfo {

    private final String key;
    private final long createdTimeMs;
    private final long objectSeq;
    private final int objSize;
    private final long acquireTimeMs;
    private final long usagesCount;
    private final long refreshesCount;
    private final CacheMode.Mode mode;
    private final long timeToLiveMs;
    private final long lastUseTime;
    private final long lastRefreshTime;
    private final String objectClassName;

    public CacheObjectInfo(String key, long createdTimeMs, long objectSeq, int objSize, long acquireTimeMs,
                           long usagesCount, long refreshesCount, CacheMode.Mode mode, long timeToLiveMs, long lastUseTime, long lastRefreshTime,
                           String objectClassName) {
        this.key = key;
        this.createdTimeMs = createdTimeMs;
        this.objectSeq = objectSeq;
        this.objSize = objSize;
        this.acquireTimeMs = acquireTimeMs;
        this.usagesCount = usagesCount;
        this.refreshesCount = refreshesCount;
        this.mode = mode;
        this.timeToLiveMs = timeToLiveMs;
        this.lastUseTime = lastUseTime;
        this.lastRefreshTime = lastRefreshTime;
        this.objectClassName = objectClassName;
    }
    public String getKey() {
        return key;
    }
    public long getCreatedTimeMs() {
        return createdTimeMs;
    }
    public long getObjectSeq() {
        return objectSeq;
    }
    public int getObjSize() {
        return objSize;
    }
    public long getAcquireTimeMs() {
        return acquireTimeMs;
    }
    public long getUsagesCount() {
        return usagesCount;
    }
    public CacheMode.Mode getMode() {
        return mode;
    }
    public long getTimeToLiveMs() {
        return timeToLiveMs;
    }
    public long getLastUseTime() {
        return lastUseTime;
    }
    public long getLastRefreshTime() {
        return lastRefreshTime;
    }
    public String getObjectClassName() {
        return objectClassName;
    }
    public long getRefreshesCount() {
        return refreshesCount;
    }

    @Override
    public String toString() {
        return "key="+ key+", createdTimeMs="+createdTimeMs+", objectSeq="+objectSeq+", objSize="+objSize+", acquireTimeMs="+acquireTimeMs+", usagesCount="+usagesCount+", refreshesCount="+refreshesCount+", mode="+mode+", timeToLiveMs="+timeToLiveMs+", lastUseTime="+lastUseTime+", lastRefreshTime="+lastRefreshTime+", objectClassName="+objectClassName;
    }
}
