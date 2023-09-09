package com.distsystem.test.cache;

import com.distsystem.DistFactory;
import com.distsystem.api.*;
import com.distsystem.interfaces.Cache;
import com.distsystem.utils.DistUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CachePerformanceTest {
    private static final Logger log = LoggerFactory.getLogger(CachePerformanceTest.class);

    @Test
    public void cachePerformanceTest() {
        log.info("START------");
        Cache cache = DistFactory.buildEmptyFactory()
                .withName("GlobalCacheTest")
                .withCacheStoragePriorityQueue()
                .withCacheObjectTimeToLive(CacheMode.TIME_ONE_HOUR)
                .withCacheMaxObjectsAndItems(100, 1000)
                .createCacheInstance();

        log.info("Cache storages: " + cache.getStorageKeys());
        log.info("!!!!!!!!!!!!!!!!!! OBJECTS " + cache.getObjectsCount());
        ArrayList<TestTimeResult> testResults = new ArrayList<>();
        // run 50 tests and 500 random keys get
        // each key is taking 10ms
        // each test is trying to get 50 keys
        for (int test=0; test<10; test++) {
            long startTime = System.currentTimeMillis();
            for (int i=0; i<50; i++) {
                String key = "key" + DistUtils.randomInt(200);
                String v = cache.withCache(key, k -> {
                    DistUtils.sleep(50);
                    return "value for " + k;
                }, CacheMode.modeTtlOneHour);
                //log.info("Test=" + test + ", i=" + i + ", value= " + v);
            }
            long totalTime = System.currentTimeMillis() - startTime;
            testResults.add(new TestTimeResult(test, totalTime, cache.getObjectsCount()));
        }
        log.info("!!!!!!!!!!!!!!!!!! OBJECTS AFTER TEST: " + cache.getObjectsCount());
        assertEquals(100, cache.getObjectsCount(), "There should be 100 objects in cache");
        DistUtils.sleep(1000);
        log.info("!!!!!!!!!!!!!!!!!! OBJECTS AFTER 10 seconds: " + cache.getObjectsCount());
        assertTrue(testResults.size()==10, "There should be 50 test results");
        assertTrue(testResults.get(0).testTimeMs > testResults.get(1).testTimeMs, "First test time should be longer than second");
        assertTrue(testResults.get(1).testTimeMs > testResults.get(2).testTimeMs, "Second test time should be longer than third");
        // show all test results
        testResults.stream().forEach(tr -> {
            log.info("!!!!!!!!!!!!!!!!!! Test " + tr.testNum + ", time: " + tr.testTimeMs + ", objectsInCache: " + tr.objectsInCache);
        });
        cache.close();
        log.info("END-----");
    }
}
class TestTimeResult {
    public int testNum;
    public long testTimeMs;
    public int objectsInCache;

    public TestTimeResult(int testNum, long testTimeMs, int objectsInCache) {
        this.testNum = testNum;
        this.testTimeMs = testTimeMs;
        this.objectsInCache = objectsInCache;
    }
}