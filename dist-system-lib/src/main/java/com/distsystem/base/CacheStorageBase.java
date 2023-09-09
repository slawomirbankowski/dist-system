package com.distsystem.base;

import com.distsystem.api.*;
import com.distsystem.api.enums.CacheStorageType;
import com.distsystem.api.enums.DistComponentType;
import com.distsystem.api.info.CacheObjectInfo;
import com.distsystem.api.info.StorageInfo;
import com.distsystem.interfaces.*;
import com.distsystem.utils.DistUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;

/** base abstract class for storage to keep caches
 * storage could be:
 * internal (HashMap, WeakHashMap) - kept in local JVM memory
 * external (Elasticsearch, Redis, DB, LocalDisk) - kept somewhere outside JVM memory
 * */
public abstract class CacheStorageBase implements CacheStorage, AgentComponent {

    protected static final Logger log = LoggerFactory.getLogger(CacheStorageBase.class);
    /** unique identifier of this storage */
    private final String storageUid;
    /** date and time of creation of this storage */
    private final LocalDateTime storageCreatedDate = LocalDateTime.now();
    /** maximum number of items in cache,
     * this is not ceil that is strict but above that more clearing will be done */
    protected final long maxItems;
    /** maximum number of objects in cache,
     * this is not ceil that is strict but above that more clearing will be done */
    protected final long maxObjects;
    /** serializer dedicated for this cache storage */
    protected final DistSerializer distSerializer;
    /** parent cache for this storage */
    protected Cache cache;

    /** base constructor to pass initialization parameters */
    public CacheStorageBase(Cache cache) {
        this.cache = cache;
        this.storageUid = DistUtils.generateStorageGuid(getClass().getSimpleName());
        this.distSerializer = cache.getAgent().getSerializer();
        this.maxObjects = cache.getConfig().getPropertyAsLong(DistConfig.CACHE_MAX_LOCAL_OBJECTS, 1000);
        this.maxItems = cache.getConfig().getPropertyAsLong(DistConfig.CACHE_MAX_LOCAL_ITEMS, 1000);
    }

    /** get type of this component */
    public DistComponentType getComponentType() {
        return DistComponentType.registration;
    }
    @Override
    public String getGuid() {
        return cache.getCacheGuid();
    }
    /** get agent */
    public Agent getAgent() {
        return cache.getAgent();
    }
    /** get date and time of creation of this component */
    public LocalDateTime getCreateDate() {
        return storageCreatedDate;
    }

    /** get unique storage ID */
    public String getStorageUid() { return storageUid; }
    /** get UID of parent cache */
    public String getCacheUid() { return cache.getCacheGuid(); }
    /** get type of this storage */
    public abstract CacheStorageType getStorageType();
    /** get name of this storage - by default it is simple name of this class */
    public String getStorageName() {
        return getClass().getSimpleName();
    }
    /** get information about this storage */
    public StorageInfo getStorageInfo() {
        return new StorageInfo(storageUid, storageCreatedDate, this.getClass().getName(),
                getItemsCount(), getObjectsCount(), isInternal(), isGlobal(), getStorageAdditionalInfo());
    }
    /** to override - get additional info parameters for this storage*/
    public Map<String, Object> getStorageAdditionalInfo() {
        return Map.of();
    }
    /** get key encoder - this is a class to encode key to protect passwords, secrets of a key */
    public CacheKeyEncoder getKeyEncoder() {
        return cache.getKeyEncoder();
    }
    /** encode key of cache object to be file name end like .98c1247b349c87238a724.cache */
    protected String encodeKeyToFileEnd(String key) {
        return "." +  DistUtils.stringToHex(getKeyEncoder().encodeKey(key)) + ".cache";
    }
    /** */
    protected String encodeKey(String key) {
        return DistUtils.stringToHex(getKeyEncoder().encodeKey(key));
    }
    /** get CacheObject item from cache by full key */
    public abstract Optional<CacheObject> getObject(String key);
    /** set item to cache and get previous item in cache for the same key */
    public abstract Optional<CacheObject> setObject(CacheObject o);
    /** remove objects in cache by keys */
    public abstract void removeObjectsByKeys(Collection<String> keys);
    /** remove single object by key */
    public void removeObjectByKey(String key) {
        removeObjectsByKeys(List.of(key));
    }
    /** get number of objects in cache storage */
    public abstract int getObjectsCount();
    /** get number of items in cache storage */
    public abstract int getItemsCount();
    public abstract Set<String> getKeys(String containsStr);
    /** get info values */
    public abstract List<CacheObject> getValues(String containsStr);
    /** get information objects for cache values */
    public abstract List<CacheObjectInfo> getInfos(String containsStr);

    /** clear caches with given clear cache */
    public abstract int clearCache(CacheClearMode clearMode);

    /** clear cache contains given partial key */
    public abstract int clearCacheContains(String str);
    /** clear cache for given group */
    public abstract int clearCacheForGroup(String groupName);

    /** check cache every X seconds to clear TTL caches
     * onTime should be run by parent manager in cycles */
    public abstract void onTimeClean(long checkSeq);

    /** check cache every X seconds to clear TTL caches
     * onTime should be run by parent manager in cycles */
    public void timeToClean(long checkSeq, long lastCleanTime) {
        // TODO: add minimum time between clean
        if (checkSeq % timeCleanEvery() == 0) {
            onTimeClean(checkSeq);
        }
    }
    /** every this value storage would be cleared */
    protected int timeCleanEvery() {
        return 2;
    }
    /** returns true if storage is internal and cache objects are kept in local memory
     * false if storage is external and cache objects are kept in any storages like Redis, Elasticsearch, DB */
    public abstract boolean isInternal();
    /** returns true if storage is global,
     * it means that one global shared storage is available for all cache instances*/
    public abstract boolean isGlobal();

    /** returns true if storage is operable and can be used
     * returns false is this storage cannot be used right now - it might be incorrect or turned off
     * this is mostly for external cache storages like JDBC DB that might be not connected
     * */
    public boolean isOperable() {
        return true;
    }

    /** dispose this storage if needed */
    public void disposeStorage() {
        // by default no dispose - it could be overridden by any storage
    }

}
