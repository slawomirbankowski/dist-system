package com.distsystem.test.custom.cache;

import com.distsystem.DistFactory;
import com.distsystem.api.CacheMode;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.Cache;
import com.distsystem.utils.CacheStats;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

public class CacheStatsTest {
    private static final Logger log = LoggerFactory.getLogger(CacheStatsTest.class);

    @Test
    public void cleanTest() {
        log.info("START ------ clean test");
        Cache cache = DistFactory.buildDefaultFactory()
                .withUniverseName("GlobalCacheTest")
                .withCacheStorageHashMap()
                .withCacheObjectTimeToLive(CacheMode.TIME_FIVE_SECONDS)
                .withTimerStorageClean(1000L)
                .withCacheMaxObjectsAndItems(30, 100)
                .createCacheInstance();

        CacheStats stats = new CacheStats();
        stats.getObjectMiss("");


        stats.refresh();


        Agent agent = cache.getAgent();


        cache.withCache("key", x -> new Object());

        cache.close();

        agent.close();

        assertTrue(cache.isClosed(), "Cache should be closed");
        assertEquals(cache.getObjectsCount(), 0, "There should be no objects in cache after close");
        log.info("END-----");
    }
}
