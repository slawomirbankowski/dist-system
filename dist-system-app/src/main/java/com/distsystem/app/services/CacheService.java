package com.distsystem.app.services;

import com.distsystem.DistFactory;
import com.distsystem.api.*;
import com.distsystem.api.info.CacheInfo;
import com.distsystem.api.info.CacheSetBackInfo;
import com.distsystem.app.DistSystemApp;
import com.distsystem.interfaces.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

/** singleton service to keep caches */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class CacheService {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(CacheService.class);
    /** list of local caches */
    private final HashMap<String, Cache> caches = new HashMap<>();

    /** create cache service with initialization of default cache */
    public CacheService() {
        DistConfig cfg = DistFactory.buildDefaultFactory()
                .withServerSocketDefaultPort()
                .withEnvironmentVariables()
                .withCommonProperties()
                .withCommandLineArguments(DistSystemApp.getCommandLineArguments())
                .extractConfig();
        initializeCache(new CacheRegister("default", cfg.getHashMap(true)));
    }
    /** initialize cache for given name */
    public CacheInfo initializeCache(CacheRegister register) {
        synchronized (caches) {
            log.info("Initializing cache for guid: " + register.cacheGuid + ", properties: " + register.properties.size());
            Cache currentCache = caches.get(register.cacheGuid);

            if (currentCache == null) {
                log.info("New cache to be created for guid: " + register.cacheGuid);
                Cache cache = DistFactory.buildEmptyFactory().withMap(register.properties).createCacheInstance();
                caches.put(register.cacheGuid, cache);
                return cache.getCacheInfo();
            } else {
                log.info("Cache already exists for guid: " + register.cacheGuid);
                return currentCache.getCacheInfo();
            }
        }
    }
    /** list all cache keys */
    public Set<String> listCaches() {
        return caches.keySet();
    }
    /** get info about cache by key */
    public CacheInfo getCacheInfoByKey(String id) {
        log.info("Get new cache for guid: " + id);
        var currentCache = caches.get(id);
        if (currentCache != null) {
            return currentCache.getCacheInfo();
        } else {
            return null;
        }
    }

    /** deactivate cache for given key */
    public ControllerStatus deactivateCache(String id) {
        var currentCache = caches.get(id);
        if (currentCache != null) {
            log.info("Deactivate cache for guid: " + id);
            currentCache.close();
            return ControllerStatus.statusCacheClosed;
        } else {
            log.info("No cache to be deactivated for guid: " + id);
            return ControllerStatus.statusCacheNotFound;
        }
    }

    /** get cache object for cache group and object key */
    public Optional<CacheObject> getObject(String cacheGroup, String objectKey) {
        var cache = caches.get(cacheGroup);
        if (cache != null) {
            return cache.getCacheObject(objectKey);
        } else {
            return Optional.empty();
        }
    }
    /** get cache object for cache group and object key */
    public String getObjectAsString(String cacheGroup, String objectKey) {
        var cache = caches.get(cacheGroup);
        if (cache != null) {
            return cache.getCacheObjectAsString(objectKey);
        } else {
            return "";
        }
    }
    /** get cache object for cache group and object key */
    public CacheSetBackInfo setObject(String cacheGroup, String objectKey, String contentValue, String mode, Set<String> groups) {
        var cache = caches.get(cacheGroup);
        if (cache != null) {
            CacheSetBack addedObjs = cache.setCacheObject(objectKey, contentValue, CacheMode.fromString(mode), groups);
            return addedObjs.toInfo();
        } else {
            return null;
        }
    }
    /** clear all caches inside group contains given key */
    public int clearCaches(String cacheGroup, String objectKey) {
        var cache = caches.get(cacheGroup);
        if (cache != null) {
            return cache.clearCacheContains(objectKey);
        } else {
            return 0;
        }
    }

}
