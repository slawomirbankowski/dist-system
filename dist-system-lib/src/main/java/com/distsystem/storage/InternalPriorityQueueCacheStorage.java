package com.distsystem.storage;

import com.distsystem.api.*;
import com.distsystem.api.enums.CacheStorageType;
import com.distsystem.api.info.CacheObjectInfo;
import com.distsystem.base.CacheStorageBase;
import com.distsystem.interfaces.Cache;

import java.util.*;

/** cache with internal HashMap */
public class InternalPriorityQueueCacheStorage extends CacheStorageBase {

    /** objects in cache */
    private final java.util.concurrent.ConcurrentHashMap<String, CacheObject> localCache = new java.util.concurrent.ConcurrentHashMap<>();
    private final PriorityQueue<CacheObject> queue = new PriorityQueue<>();

    public InternalPriorityQueueCacheStorage(Cache cache) {
        super(cache);
    }
    /** HashMap is internal storage */
    public  boolean isInternal() { return true; }
    /** returns true if storage is global,
     * it means that one global shared storage is available for all cache instances*/
    public boolean isGlobal() { return false; }
    /** get additional info parameters for this storage */
    public Map<String, Object> getStorageAdditionalInfo() {
        return Map.of("className", localCache.getClass().getName(),
                "queueSize", queue.size());
    }
    /** read configuration and re-initialize this component */
    public boolean componentReinitialize() {
        // nothing to be done here
        return true;
    }
    /** get type of this storage */
    public CacheStorageType getStorageType() {
        return CacheStorageType.memory;
    }
    /** check if object has given key, optional with specific type */
    public boolean contains(String key) {
        return localCache.containsKey(key);
    }
    /** get item from cache */
    public Optional<CacheObject> getObject(String key) {
        return Optional.ofNullable(localCache.get(key));
    }
    /** add item into cache  */
    public Optional<CacheObject> setObject(CacheObject o) {
        log.trace("Set new item for cache, key: " + o.getKey());
        CacheObject prev = localCache.put(o.getKey(), o);
        if (prev != null) {
            prev.releaseObject();
        }
        // TODO: need to dispose object after removing from cache - this would be based on policy
        return Optional.empty();
    }
    /** remove objects in cache storage by keys */
    public void removeObjectsByKeys(Collection<String> keys) {
    }
    /** remove object in cache storage by key */
    public void removeObjectByKey(String key) {
    }
    /** get number of items in cache */
    public int getItemsCount() {
        return localCache.size();
    }
    /** get number of objects in this cache */
    public int getObjectsCount() { return localCache.size(); }

    /** get keys for all cache items */
    public Set<String> getKeys(String containsStr) {
        return new HashSet<String>();
    }
    /** get info values */
    public List<CacheObjectInfo> getInfos(String containsStr) {
        return new LinkedList<CacheObjectInfo>();
    }

    /** get values of cache objects that contains given String in key */
    public List<CacheObject> getValues(String containsStr) {
        return new LinkedList<CacheObject>();
    }
    public void onTimeClean(long checkSeq) {
        for (Map.Entry<String, CacheObject> e: localCache.entrySet()) {

            // TODO: clear
            //e.getKey();
            //e.getValue().releaseObject();

        }
    }
    /** clear caches with given clear cache */
    public int clearCacheForGroup(String groupName) {
        return 0;
    }
    /** clear cache by given mode
     * returns estimated of elements cleared */
    public int clearCache(CacheClearMode clearMode) {
        return -1;
    }
    /** clear cache contains given partial key */
    public int clearCacheContains(String str) {
        return 0;
    }
}
