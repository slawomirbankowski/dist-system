package com.distsystem.test.custom.cache;

import com.distsystem.DistFactory;
import com.distsystem.interfaces.Cache;
import com.distsystem.api.CacheMode;
import com.distsystem.utils.DistUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CachePriorityClearTest {
    private static final Logger log = LoggerFactory.getLogger(CachePriorityClearTest.class);

    @Test
    public void performanceTest() {
        log.info("START------");
        Cache cache = DistFactory.buildEmptyFactory()
                .withUniverseName("GlobalCacheTest")
                .withCacheStorageHashMap()
                .withCacheObjectTimeToLive(CacheMode.TIME_ONE_HOUR)
                .withCacheMaxObjectsAndItems(500, 3000)
                .createCacheInstance();
        log.info("Cache storages: " + cache.getStorageKeys());
        LinkedList<Object[]> testResults = new LinkedList<>();
        // run 50 tests and 500 random keys get
        // each key is taking 10ms
        // each test is trying to get 50 keys
        for (int test=0; test<50; test++) {
            long startTime = System.currentTimeMillis();
            for (int i=0; i<50; i++) {
                String key = "key" + DistUtils.randomInt(500);
                String v = cache.withCache(key, k -> {
                    DistUtils.sleep(10);
                    return "value for " + k;
                }, CacheMode.modeTtlOneHour);
                //log.info("Test=" + test + ", i=" + i + ", value= " + v);
            }
            long totalTime = System.currentTimeMillis() - startTime;
            testResults.add(new Object[] { test, totalTime, cache.getObjectsCount() });
        }
        assertTrue(testResults.size()==50, "There should be 50 test results");
        // show all test results
        testResults.stream().forEach(tr -> {
            log.info("!!!!!!!!!!!!!!!!!! Test " + tr[0] + ", time: " + tr[1] + ", objectsInCache: " + tr[2]);
        });
        cache.close();
        log.info("END-----");
    }
}
