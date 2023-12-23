package com.distsystem.test.custom.cache;

import com.distsystem.DistFactory;
import com.distsystem.api.*;
import com.distsystem.interfaces.Cache;
import com.distsystem.utils.DistUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

public class CacheSimpleTest {
    private static final Logger log = LoggerFactory.getLogger(CacheSimpleTest.class);

    @Test
    public void simpleTest() {
        log.info("START------ cache simple test");

        Cache cache = DistFactory.buildEmptyFactory()
                .withUniverseName("GlobalCacheTest")
                .withCacheStorageHashMap()
                .withCacheMaxObjectsAndItems(30, 100)
                .createCacheInstance();

        assertNotNull(cache, "Created cache should not be null");
        assertNotNull( cache.getConfig().getConfigGuid(), "Config GUID should not be null");
        assertNotNull( cache.getCacheGuid(), "Cache GUID should not be null");
        assertEquals(cache.getObjectsCount(), 0, "There should be 0 objects in cache");
        assertEquals(cache.getItemsCount(), 0, "There should be 0 objects in cache");
        assertEquals(cache.isClosed(), false, "Cache should not be closed");
        assertEquals(cache.getStorageKeys().size(), 1, "There should be 1 storage in cache");
        log.info("Config GUID: " + cache.getConfig().getConfigGuid());
        log.info("Cache GUID: " + cache.getCacheGuid());
        log.info("Cache getObjectsCount: " + cache.getObjectsCount());
        log.info("Cache getItemsCount: " + cache.getItemsCount());
        log.info("Cache getClosed: " + cache.isClosed());
        log.info("Cache storages: " + cache.getStorageKeys());

        for (int i=0; i<30; i++) {
            // get 30 times the same value
            String v = cache.withCache("key", key -> {
                DistUtils.sleep(100);
                return "value ";
            }, CacheMode.modeTtlTenSeconds);
            log.info("Key=" + i + ", value= " + v);
            DistUtils.sleep(50);
        }
        assertEquals(cache.getObjectsCount(), 1, "There should be 1 objects in cache");
        assertEquals(cache.getItemsCount(), 1, "There should be 1 objects in cache");
        log.info("Cache getItemsCount: " + cache.getItemsCount());
        cache.close();
        log.info("END-----");
    }
}
