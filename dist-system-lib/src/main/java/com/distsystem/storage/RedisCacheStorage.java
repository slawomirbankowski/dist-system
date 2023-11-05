package com.distsystem.storage;

import com.distsystem.api.*;
import com.distsystem.api.enums.CacheStorageType;
import com.distsystem.api.info.CacheObjectInfo;
import com.distsystem.base.CacheStorageBase;
import com.distsystem.interfaces.Cache;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.*;
import java.util.stream.Collectors;

/** cache with Redis as Cache Storage
 * TODO: Implement storage saving cache objects in Redis
 * */
public class RedisCacheStorage extends CacheStorageBase {

    /** Redis host name or address */
    private String redisHost;
    /** Redist port */
    private int redisPort;
    /** Redis client for read/write actions */
    private Jedis jedis;

    /** initialize Redis storage */
    public RedisCacheStorage(Cache cache) {
        super(cache);
        redisHost = cache.getConfig().getProperty(DistConfig.AGENT_CACHE_STORAGE_REDIS_HOST, "");
        redisPort = cache.getConfig().getPropertyAsInt(DistConfig.AGENT_CACHE_STORAGE_REDIS_PORT, 6379);
        jedis = new Jedis(redisHost, redisPort);
    }

    /** read configuration and re-initialize this component */
    public boolean componentReinitialize() {
        // TODO: implement reinitialization
        return true;
    }
    /** Redis is external storage */
    public  boolean isInternal() { return false; }

    /** returns true if storage is global,
     * it means that one global shared storage is available for all cache instances */
    public boolean isGlobal() { return true; }
    /** returns true Redis is connected/ available */
    public boolean isOperable() {
        try {
            return jedis.isConnected();
        } catch (Exception ex) {
            return false;
        }
    }
    /** get additional info parameters for this storage */
    public Map<String, Object> getStorageAdditionalInfo() {
        return Map.of();
    }

    /** get type of this storage */
    public CacheStorageType getStorageType() {
        return CacheStorageType.redis;
    }
    /** check if object has given key, optional with specific type */
    public boolean contains(String key) {
        String value = jedis.get(key);
        return value != null && !value.isEmpty() && !value.isBlank();
    }
    /** get object from Redis */
    public Optional<CacheObject> getObject(String key) {
        try {
            String value = jedis.get(key);
            if (value != null && !value.isEmpty() && !value.isBlank()) {
                return CacheObject.fromSerializedString(distSerializer, value);
            }
            return Optional.empty();
        } catch (Exception ex) {
            log.warn("");
            return Optional.empty();
        }
    }
    /** write object to Redis */
    public  Optional<CacheObject> setObject(CacheObject o) {
        try {
            var prevObj = getObject(o.getKey());
            String objStr = o.serializedFullCacheObjectToString(distSerializer);
            jedis.set(o.getKey(), objStr, SetParams.setParams().ex(o.getTimeToLive()/1000));
            return prevObj;
        } catch (Exception ex) {

            return Optional.empty();
        }
    }
    /** remove objects in cache storage by keys */
    public void removeObjectsByKeys(Collection<String> keys) {
        //jedis.del(keys.toArray(new String[0]));
        keys.stream().forEach(key -> {
            jedis.del(key);
        });
    }
    /** remove object in cache storage by key */
    public void removeObjectByKey(String key) {
        jedis.del(key);
    }
    /** get number of items in cache */
    public  int getItemsCount() {
        return jedis.keys("*").size();
    }
    /** get number of objects in this cache */
    public int getObjectsCount() {
        return jedis.keys("*").size();
    }
    /** get keys for all cache items */
    public Set<String> getKeys(String containsStr) {
        try {
            return jedis.keys("*" + containsStr + "*");
        } catch (Exception ex) {
            cache.addIssue("getKeys", ex);
            return new HashSet<String>();
        }
    }
    /** get info values */
    public List<CacheObjectInfo> getInfos(String containsStr) {
        return getValues(containsStr).stream().map(v -> v.getInfo()).collect(Collectors.toList());
    }
    /** get values of cache objects that contains given String in key */
    public List<CacheObject> getValues(String containsStr) {
        var keys = getKeys(containsStr);
        return keys.stream().flatMap(key -> getObject(key).stream()).collect(Collectors.toList());
    }
    /** clear caches with given clear cache */
    public int clearCacheForGroup(String groupName) {
        // TODO: clear cache for given group
        return 1;
    }
    /** clear cache contains given partial key */
    public int clearCacheContains(String str) {
        removeObjectsByKeys(getKeys(str));
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
    public void disposeStorage() {
        jedis.close();
    }
}
