package com.distsystem.test.cache;

import com.distsystem.DistFactory;
import com.distsystem.api.CacheMode;
import com.distsystem.interfaces.Cache;
import com.distsystem.utils.DistUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class CacheStorageElasticsearchSimpleTest {
    private static final Logger log = LoggerFactory.getLogger(CacheStorageElasticsearchSimpleTest.class);

    @Test
    public void elasticsearchStorageSimpleTest() {
        log.info("START ------ ");

        String elasticUrl = "https://localhost:9200";
        Cache cache = DistFactory.buildEmptyFactory()
                .withUniverseName("GlobalCacheTest")
                .withCacheStorageElasticsearch(elasticUrl, "elastic", "${ELASTICSEARCH_PASS}")
                .withCacheObjectTimeToLive(CacheMode.TIME_ONE_DAY)
                .withCacheObjectTimeToLive(CacheMode.TIME_ONE_HOUR)
                .withCacheMaxObjectsAndItems(3000, 10000000)
                .createCacheInstance();

        //assertEquals(cache.getObjectsCount(), 0, "There should be NO objects in cache");
        log.info("CACHE STORAGES: " + cache.getStorageKeys());
        for (int i=0; i<10; i++) {
            cache.withCache("key" + i, key -> "value", CacheMode.modeTtlOneMinute);
            //assertEquals(i+1, cache.getObjectsCount(), "There should be " + (i+1) + " values in cache");
            var value = cache.getCacheObject("key" + i);
            //assertTrue(value.isPresent(), "There should be value for key: key" + i);
            //assertEquals("value", value.get().getValue());
            log.info("################# Key:" + i + ", present: " + value.isPresent());
        }
        log.info("Objects in cache: " + cache.getObjectsCount());
        for (int i=0; i<100; i++) {
            int keyNum = DistUtils.randomInt(10);
            String v = cache.withCache("key"+keyNum, key -> "value" + keyNum, CacheMode.modeTtlOneMinute);
        }
        //assertEquals(10, cache.getObjectsCount(), "There should be 10 values in cache");
        //assertEquals(1, cache.getCacheKeys("key7", true).size(), "There should be 1 key in cache that contains key7");
        cache.clearCacheContains("key");
       // assertEquals(0, cache.getObjectsCount(), "There should be NO objects in cache");
        //cache.clearCacheContains("");
        cache.close();
        log.info("END-----");
    }
}
