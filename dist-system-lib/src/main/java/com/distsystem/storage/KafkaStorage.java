package com.distsystem.storage;

import com.distsystem.api.*;
import com.distsystem.api.enums.CacheStorageType;
import com.distsystem.api.info.CacheObjectInfo;
import com.distsystem.base.CacheStorageBase;
import com.distsystem.interfaces.Cache;

import java.util.*;

/** cache with Kafka topic - key would be key for object
 *
 * TODO: Implement storage saving cache objects in Kafka
 * */
public class KafkaStorage extends CacheStorageBase {

    /** TODO: init Kafka storage */
    public KafkaStorage(Cache cache) {
        super(cache);
    }
    /** Kafka is external storage */
    public  boolean isInternal() { return false; }
    /** returns true if storage is global,
     * it means that one global shared storage is available for all cache instances*/
    public boolean isGlobal() { return true; }
    /** returns true if storage is operable and can be used
     * returns false is this storage cannot be used right now - it might be incorrect or turned off
     * this is mostly for external cache storages like JDBC DB that might be not connected
     * */
    public boolean isOperable() {
        // TODO: implement checking connection to Kafka
        return true;
    }

    /** get additional info parameters for this storage */
    public Map<String, Object> getStorageAdditionalInfo() {
        return Map.of();
    }

    /** get type of this storage */
    public CacheStorageType getStorageType() {
        return CacheStorageType.kafka;
    }
    /** check if object has given key, optional with specific type */
    public boolean contains(String key) {
        return false;
    }
    /** TODO: get item from Kafka */
    public Optional<CacheObject> getObject(String key) {
        return Optional.empty();
    }
    public Optional<CacheObject> setObject(CacheObject o) {
        return Optional.empty();
    }
    /** remove objects in cache storage by keys */
    public void removeObjectsByKeys(Collection<String> keys) {
    }
    /** remove object in cache storage by key */
    public void removeObjectByKey(String key) {
    }
    /** get number of items in cache */
    public  int getItemsCount() {
        return 0;
    }
    /** get number of objects in this cache */
    public int getObjectsCount() { return 0; }
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
    /** clear caches with given clear cache */
    public int clearCacheForGroup(String groupName) {

        return 1;
    }
    /** clear cache contains given partial key */
    public int clearCacheContains(String str) {
        return 1;
    }
    /** clear cache by given mode
     * returns estimated of elements cleared */
    public int clearCache(CacheClearMode clearMode) {
        return -1;
    }
    /** check cache every X seconds to clear TTL caches */
    public void onTimeClean(long checkSeq) {

    }
}
