package com.distsystem.test.cache;

import com.distsystem.DistFactory;
import com.distsystem.api.CacheMode;
import com.distsystem.interfaces.Cache;
import com.distsystem.utils.DistUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import redis.clients.jedis.Jedis;
//import redis.clients.jedis.params.SetParams;
import static org.junit.jupiter.api.Assertions.*;


public class CacheStorageRedisSimpleTest {
    private static final Logger log = LoggerFactory.getLogger(CacheStorageRedisSimpleTest.class);

    @Test
    public void redisStorageSimpleTest() {
        log.info("START ------ ");

        String host = "localhost";
        int port = 6379;
        Cache cache = DistFactory.buildEmptyFactory()
                .withName("GlobalCacheTest")
                .withCacheStorageRedis(host, port)
                .withCacheObjectTimeToLive(CacheMode.TIME_ONE_DAY)
                .withTimerStorageClean(CacheMode.TIME_ONE_HOUR)
                .withCacheMaxObjectsAndItems(3000, 10000000)
                .createCacheInstance();

        cache.clearCacheContains("");
        assertEquals(0, cache.getObjectsCount(), "There should be NO objects in cache");

        for (int i=0; i<10; i++) {
            cache.withCache("key" + i, key -> "value", CacheMode.modeTtlOneMinute);
            assertEquals(i+1, cache.getObjectsCount(), "There should be " + (i+1) + " values in cache");
            var value = cache.getCacheObject("key" + i);
            assertTrue(value.isPresent(), "There should be value for key: key" + i);
            assertEquals("value", value.get().getValue());
            log.info("Objects in cache: " + cache.getObjectsCount() + ", keys: " + cache.getCacheKeys("") + ", current:" + value.get().getInfo());
        }
        for (int i=0; i<10; i++) {
            int keyNum = DistUtils.randomInt(10);
            String v = cache.withCache("key"+keyNum, key -> "value" + keyNum, CacheMode.modeTtlOneMinute);
            log.debug("Objects in cache: " + cache.getObjectsCount());
        }
        assertEquals(10, cache.getObjectsCount(), "There should be 10 values in cache");

        assertEquals(1, cache.getCacheKeys("key7", true).size(), "There should be 1 key in cache that contains key7");
        assertEquals(1, cache.getCacheKeys("key8", true).size(), "There should be 1 key in cache that contains key8");

        cache.clearCacheContains("key7");
        assertEquals(9, cache.getObjectsCount(), "There should be 9 objects in cache");
        assertTrue(cache.getCacheKeys("key7", true).isEmpty(), "There should be no cache for key: key7");

        cache.clearCacheContains("key");
        assertEquals(0, cache.getObjectsCount(), "There should be NO objects in cache");

        cache.clearCacheContains("");

        cache.close();
/*
        Jedis jedis = new Jedis(host, port);
        log.info("Redis client ID: " + jedis.clientId());
        log.info("Redis client info: " + jedis.clientInfo());
        log.info("Redis client name: " + jedis.clientGetname());
        //log.info("Redis who-am-i: " + jedis.aclWhoAmI());
        //log.info("Redis ping: " + jedis.ping());
        //log.info("Redis info: " + jedis.info());
        //log.info("Redis randomKey: " + jedis.randomKey());

        log.info("Redis set1: " + jedis.set("key1", "value1", SetParams.setParams().ex(10)));
        log.info("Redis get1: " + jedis.get("key1"));

        log.info("Redis set2: " + jedis.set("key2", "value2", SetParams.setParams().ex(10)));
        log.info("Redis get2: " + jedis.get("key2"));

        log.info("Redis set3: " + jedis.set("key3", "value3", SetParams.setParams().ex(10)));
        log.info("Redis get3: " + jedis.get("key3"));

        log.info("Redis KEYS: " + jedis.keys("key*"));

        CacheUtils.sleep(11000);

        log.info("Redis get2: " + jedis.get("key1"));

        jedis.close();

 */
        log.info("END-----");
    }
}
