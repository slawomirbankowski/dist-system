package com.distsystem.test.cache;

import com.distsystem.DistFactory;
import com.distsystem.api.CacheMode;
import com.distsystem.interfaces.Cache;
import com.distsystem.utils.DistUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

public class CacheDistributeClearTest {
    private static final Logger log = LoggerFactory.getLogger(CacheDistributeClearTest.class);

    AtomicLong seq = new AtomicLong();
    @Test
    public void cacheClearDistributedTest() {
        log.info("START ------ clean test");

        Cache cache1 = DistFactory.buildDefaultFactory()
                .withUniverseName("GlobalCacheTest")
                .withRegistrationJdbc("jdbc:postgresql://localhost:5432/cache01", "org.postgresql.Driver",
                        "cache_user", "${JDBC_PASS}")
                .withCacheStorageHashMap()
                .withServerSocketPort(9991)
                .withWebApiPort(9999)
                .withCacheObjectTimeToLive(CacheMode.TIME_TEN_MINUTES)
                .withTimerStorageClean(1000)
                .withTimerRegistrationPeriod(500)
                .withTimerServerPeriod(500)
                .withCacheMaxObjectsAndItems(100, 1000)
                .createCacheInstance();

        Cache cache2 = DistFactory.buildDefaultFactory()
                .withUniverseName("GlobalCacheTest")
                .withRegistrationJdbc("jdbc:postgresql://localhost:5432/cache01", "org.postgresql.Driver",
                        "cache_user", "${JDBC_PASS}")
                .withCacheStorageHashMap()
                .withServerSocketPort(9992)
                .withWebApiPort(9998)
                .withCacheObjectTimeToLive(CacheMode.TIME_TEN_MINUTES)
                .withTimerStorageClean(1000)
                .withTimerRegistrationPeriod(1000)
                .withTimerServerPeriod(1000)
                .withCacheMaxObjectsAndItems(100, 1000)
                .createCacheInstance();

        assertNotNull(cache1, "Created cache should not be null");
        assertNotNull(cache2, "Created cache should not be null");

        log.info("Test waiting for Agent registrations and servers");

        DistUtils.sleep(2000);

        assertEquals(1, cache1.getAgent().getConnectors().getServersCount(), "There should be 1 server for agent1");
        assertEquals(1, cache2.getAgent().getConnectors().getServersCount(), "There should be 1 server for agent2");

        DistUtils.sleep(2000);
        assertEquals(1, cache1.getAgent().getConnectors().getClientsCount(), "There should be 1 client for agent1");
        assertEquals(1, cache2.getAgent().getConnectors().getClientsCount(), "There should be 1 client for agent2");

        log.info("Empty caches");
        assertEquals(cache1.getObjectsCount(), 0, "There should be no objects in cache");
        assertEquals(cache2.getObjectsCount(), 0, "There should be no objects in cache");

        log.info("Adding Objects to cache1");
        for (int i=0; i<10; i++) {
            String v = cache1.withCache("key"+i, key -> getNextValue(key), CacheMode.modeRefreshTenSeconds);
        }

        log.info("Adding Objects to cache2");
        for (int i=0; i<10; i++) {
            String v = cache2.withCache("key"+i, key -> getNextValue(key), CacheMode.modeRefreshTenSeconds);
        }

        assertEquals(cache1.getObjectsCount(), 10, "There should be 10 objets in cache1");
        assertEquals(cache1.getObjectsCount(), 10, "There should be 10 objets in cache2");

        log.info("Clearing cache1 for key7");
        cache1.clearCacheContains("key7");

        log.info("Clearing cache2 for key5");
        cache2.clearCacheContains("key5");

        DistUtils.sleep(1000); // distributing clear should take around 500ms to be send from agent1 to agent2 AND from agent2 to agent1

        log.info("After clear cache in agents, cache1: " + cache1.getObjectsCount() + ", cache2: " + cache1.getObjectsCount());
        assertEquals(8, cache1.getObjectsCount(),  "There should be 8 objets in cache1");
        assertEquals(8, cache1.getObjectsCount(),  "There should be 8 objets in cache2");

        cache1.close();
        cache2.close();
        assertTrue(cache1.isClosed(), "Cache1 should be closed");
        assertTrue(cache2.isClosed(), "Cache2 should be closed");

        log.info("END-----");
    }
    public String getNextValue(String key) {
        return "value" + seq.incrementAndGet();
    }
}
