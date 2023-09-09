package com.distsystem.test.cache;

import com.distsystem.DistFactory;
import com.distsystem.api.*;
import com.distsystem.interfaces.Cache;
import com.distsystem.utils.DistUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class    CacheEventTest {
    private static final Logger log = LoggerFactory.getLogger(CacheEventTest.class);

    @Test
    public void eventsTest() {
        log.info("START------");
        AtomicLong cleanEvents = new AtomicLong();
        AtomicLong storageEvents = new AtomicLong();
        Cache cache = DistFactory.buildDefaultFactory()
                .withName("GlobalCacheTest")
                .withCacheStorageHashMap()
                .withCacheObjectTimeToLive(CacheMode.TIME_TEN_SECONDS)
                .withCallback(CacheEvent.EVENT_CACHE_START, x -> {
                    log.info("::::::::: EVENT START CALLBACK");

                    return "OK";
                })
                .withCallback(CacheEvent.EVENT_TIMER_CLEAN, x -> {
                    log.info("::::::::: EVENT TIMER CLEAN");
                    cleanEvents.incrementAndGet();
                    return "OK";
                })
                .withCallback(CacheEvent.EVENT_INITIALIZE_STORAGE, x -> {
                    log.info("::::::::: STORAGE NEW");
                    storageEvents.incrementAndGet();
                    return "OK";
                })
                .withTimerStorageClean(1000L)
                .withMaxEvents(100)
                .withCacheMaxObjectsAndItems(30, 100)
                .createCacheInstance();
        DistUtils.sleep(15000);
        assertTrue(storageEvents.get() >= 1, "There should be 1 event for storage");
        assertTrue(cleanEvents.get() >= 13, "There should be at least 13 events for clean");
        cache.close();
        log.info("END-----");
    }
}
