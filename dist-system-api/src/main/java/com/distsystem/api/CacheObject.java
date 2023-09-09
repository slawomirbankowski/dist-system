package com.distsystem.api;

import com.distsystem.api.enums.CacheStorageType;
import com.distsystem.api.info.CacheObjectInfo;
import com.distsystem.interfaces.DistSerializer;
import com.distsystem.utils.DistUtils;

import java.io.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/** class to be put to cache - it contains object caches AND many other statistics
 * this cache is representing internal cache with object stored */
public class CacheObject {

    /** sequence of object in this JVM */
    public static AtomicLong globalObjectSeq = new AtomicLong();
    /** sequence of created object in this JVM */
    public long objectSeq = globalObjectSeq.incrementAndGet();
    /** created time of this object in cache */
    private long createdTimeMs = System.currentTimeMillis();
    /** last use time of this object in cache */
    private long lastUseTime = System.currentTimeMillis();
    private long lastRefreshTime = System.currentTimeMillis();
    /** key of this object stored in cache */
    private final String key;
    /** object to be push to cache */
    private Object objectInCache;
    /** method to refresh this object in local cache */
    private final Function<String, ?> methodToAcquire;
    /** size of object */
    private int objSize = 1;
    /** time of get this object from external sources, time to acquire in milliseconds */
    private long acquireTimeMs;

    /** counter of usages for current object in cache */
    private final AtomicLong usages = new AtomicLong();
    /** counter for refreshes */
    private final AtomicLong refreshes = new AtomicLong();
    /** cache mode */
    private CacheMode.Mode mode;
    /** time to live milliseconds */
    private long timeToLiveMs;
    /** priority in cache */
    private int priority;
    /** set of groups to identify cache with, these groups are helpful with clearing caches */
    private Set<String> groups;
    /** get storage types */
    private Set<CacheStorageType> storageTypes = CacheStorageType.allStorages;

    /** creates object from deserialization */
    public CacheObject(long objectSeq, long createdTimeMs, long lastUseTime, long lastRefreshTime, String key,
                       Object objectInCache, Function<String, ?> methodToAcquire, int objSize, long acquireTimeMs,
                       long usages, long refreshes, CacheMode.Mode mode, int priority, long timeToLive, Set<String> groups) {
        this.objectSeq = objectSeq;
        this.createdTimeMs = createdTimeMs;
        this.lastUseTime = lastUseTime;
        this.lastRefreshTime = lastRefreshTime;
        this.key = key;
        this.objectInCache = objectInCache;
        this.methodToAcquire = methodToAcquire;
        this.objSize = objSize;
        this.acquireTimeMs = acquireTimeMs;
        this.usages.set(usages);
        this.refreshes.set(refreshes);
        this.mode = mode;
        this.timeToLiveMs = timeToLive;
        this.priority = priority;
        this.groups = groups;
    }
    /** creates new object in memory */
    public CacheObject(String key, Object o, long acqTimeMs, Function<String, ?> method, CacheMode cm, Set<String> groups) {
        this.key = key;
        this.objSize = DistUtils.estimateSize(o);
        this.objectInCache = o;
        this.acquireTimeMs = acqTimeMs;
        this.methodToAcquire = method;
        this.mode = cm.getMode();
        this.timeToLiveMs = cm.getTimeToLiveMs();
        this.priority = cm.getPriority();
        this.groups = groups;
        calculateSize();
    }
    /** creates new object in memory */
    public CacheObject(String key, Object o, CacheMode cm, Set<String> groups) {
        this(key, o, 0, k -> o, cm, groups);
    }
    /** creates new object in memory */
    public CacheObject(String key, Object o, long acqTimeMs, CacheMode mode, Set<String> groups) {
        this(key, o, acqTimeMs, k -> o, mode, groups);
    }
    /** creates new object in memory */
    public CacheObject(String key, Object o, long ackTimeMs, CacheMode mode) {
        this(key, o, ackTimeMs, mode, new HashSet<>());
    }
    /** creates new object in memory */
    public CacheObject(String key, Object o, long ackTimeMs) {
        this(key, o, ackTimeMs, CacheMode.modeTtlOneHour);
    }
    /** creates new object in memory */
    public CacheObject(String key, Object o) {
        this(key, o, 0, CacheMode.modeTtlOneHour);
    }
    /** get simple serializable information about this object in cache */
    public CacheObjectInfo getInfo() {
        return new CacheObjectInfo(key, createdTimeMs, objectSeq, objSize, acquireTimeMs,
                usages.get(), refreshes.get(),
                mode, timeToLive(), lastUseTime, lastRefreshTime,
                objectInCache.getClass().getName());
    }

    /** get list of supported storages for this cache object */
    public Set<CacheStorageType> getSupportedStorages() {
        return storageTypes;
    }
    /** try to calculate size of this object as estimated number of objects */
    private void calculateSize() {
        this.objSize = DistUtils.estimateSize(objectInCache);
    }
    /** get key of object in cache */
    public String getKey() {
        return key;
    }
    /** get value of object in cache */
    public Object getValue() {
        return objectInCache;
    }
    /** get estimated size of object in cache - the size is counting objects in memory rather than bytes or real memory used */
    public int getSize() { return objSize; }
    public Class getObjectClass() { return objectInCache.getClass(); }
    /** get full name of class in cache */
    public String getClassName() {
        return objectInCache.getClass().getName();
    }
    /** get sequence of object in cache - sequence is created from 1 */
    public long getSeq() { return objectSeq; }
    /** get mode of this cache object */
    public CacheMode.Mode getMode() {
        return mode;
    }
    /** get number of automatic refreshed of this value */
    public long getRefreshes() {
        return refreshes.get();
    }
    /** get time of last use this cache */
    public long getLastUseTime() { return lastUseTime; }
    /** get acquire time of getting this object from aquire method */
    public long getAcquireTimeMs() { return acquireTimeMs; }
    /** */
    public long getTimeToLive() { return timeToLiveMs; }

    /** release action for this cache object - by default there is no action for releasing
     * GC should normally dispose this object */
    public void releaseObject() {
        // TODO: check via reflection if there is any method like close() or dispose() and run it

    }
    /** returns true if cache time to live is finished */
    public boolean isOutdated() {
        return timeToLive() <= 0;
    }
    /** get priority of this cache object */
    public int getPriority() {
        return priority;
    }
    /** set new size of this cache object */
    void setSize(int newObjSize) {
        this.objSize += newObjSize;
    }
    /** set new time to live for this cache object */
    void setTtl(long newTtl) {
        this.timeToLiveMs = newTtl;
    }
    /** set new mode for this object */
    void setMode(CacheMode.Mode m) {
        this.mode = m;
    }
    void setPriority(int priority) {
        this.priority = priority;
    }
    /** set supported storages */
    void setSupportedStorages(Set<CacheStorageType> stTypes) {
        this.storageTypes = stTypes;
    }
    /** */
    void setGroups(Set<String> grs) {
        this.groups = grs;
    }
    /** */
    public void renew() {
        lastUseTime = System.currentTimeMillis();
    }
    /** check if key of this object contains given string */
    public boolean keyContains(String str) {
        return key.contains(str);
    }
    /** use of this cache object */
    public long use() {
        lastUseTime = System.currentTimeMillis();
        return usages.incrementAndGet();
    }
    /** get actual live time for this object */
    public long liveTime() {
        return System.currentTimeMillis() - createdTimeMs;
    }
    /** calculate current time to live for this object */
    public long timeToLive() {
        return timeToLiveMs - (System.currentTimeMillis() - createdTimeMs);
    }
    /** if this object is old - it means that is TTL mode and live time is longer than declared time to live */
    public boolean isOld() {
        return mode.isTtl() && (liveTime() > getTimeToLive());
    }
    /** check if this object should be refreshed
     * it means that object has REFRESH mode and last refresh time is longer than time to live */
    public boolean shouldBeRefreshed() {
        return mode.isRefresh() && (System.currentTimeMillis()- lastRefreshTime>getTimeToLive());
    }
    /** refresh object using acquire method if object should be refreshed
     * returns size of object OR
     * -1 => Exception while acquiring
     * -2 => method to acquire is null
     * 0 => no need to refresh object
     * */
    public int refreshIfNeeded() {
        boolean sbr = shouldBeRefreshed();
        //System.out.println("Should be refreshed: " + sbr + ", key=" + key + ", lastRefreshTime=" + lastRefreshTime + ", ttl=" + getTimeToLive() + ", curr=" + System.currentTimeMillis() + ", isRefresh=" + mode.isRefresh());
        if (sbr) {
            lastRefreshTime = System.currentTimeMillis();
            if (methodToAcquire != null) {
                try {
                    long startAckTime = System.currentTimeMillis();
                    refreshes.incrementAndGet();
                    objectInCache = methodToAcquire.apply(getKey());
                    acquireTimeMs = System.currentTimeMillis()-startAckTime;
                    calculateSize();
                    return objSize;
                } catch (Exception ex) {
                    return -1;
                }
            }
            return -2;
        }
        return 0;
    }
    /** serialize object in cache to byte[] */
    public byte[] serializeObjectInCache(DistSerializer serializer) {
        return serializer.serialize(objectInCache);
    }

    /** serialize CacheObject to CacheObjectSerialized */
    public CacheObjectSerialized serializedFullCacheObject(DistSerializer serializer) {
        String serializedObj = serializer.serializeToString(objectInCache);
        return new CacheObjectSerialized(objectSeq, createdTimeMs, lastUseTime, lastRefreshTime, key,
                serializedObj, objectInCache.getClass().getName(),
                objSize, acquireTimeMs, usages.get(), refreshes.get(),
                mode, getTimeToLive(), getPriority(), true, true,
                groups);
    }

    /** serialize CacheObject to CacheObjectSerialized */
    public String serializedFullCacheObjectToString(DistSerializer serializer) {
        CacheObjectSerialized ser = serializedFullCacheObject(serializer);
        return serializer.serializeToString(ser);
    }

    /** write to Stream as blob, returns number of bytes written OR -1 if there is error while writing */
    public int writeToStream(DistSerializer serializer, OutputStream outStream) {
        try {
            byte[] b = serializeObjectInCache(serializer);
            outStream.write(b);
            return b.length;
        } catch (IOException ex) {
            return -1;
        }
    }
    /** create cache object from serialized CacheObjectSerialized */
    public static CacheObject fromSerialized(DistSerializer serializer, CacheObjectSerialized serialized) {
        return serialized.toCacheObject(serializer);
    }
    /**  */
    public static Optional<CacheObject> fromSerializedString(DistSerializer serializer, String serialized) {
        try {
            CacheObjectSerialized cos = (CacheObjectSerialized)serializer.deserializeFromString(CacheObjectSerialized.class.getName(), serialized);
            return Optional.of(fromSerialized(serializer, cos));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

}
