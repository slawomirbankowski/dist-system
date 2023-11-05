package com.distsystem.test.cache;

import com.distsystem.DistFactory;
import com.distsystem.interfaces.Cache;
import com.distsystem.api.CacheMode;
import com.distsystem.utils.DistUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CacheMultiThreadTest {
    private static final Logger log = LoggerFactory.getLogger(CacheMultiThreadTest.class);

    @Test
    public void testMultiThread() {
        log.info("START------");
        Cache cache = DistFactory.buildEmptyFactory()
                .withUniverseName("GlobalCacheTest")
                .withCacheStorageHashMap()
                .withCacheObjectTimeToLive(CacheMode.TIME_ONE_HOUR)
                .withCacheMaxObjectsAndItems(1000, 3000)
                .createCacheInstance();
        log.info("Cache storages: " + cache.getStorageKeys());
        int maxThreads = 10;
        ReadingWritingThread[] threaads = new ReadingWritingThread[maxThreads];
        for (int th=0; th<maxThreads; th++) {
            ReadingWritingThread thread = new ReadingWritingThread();
            thread.cache = cache;
            thread.start();
            threaads[th] = thread;
        }
        // test should take 5 seconds
        DistUtils.sleep(5000);
        for (int th=0; th<maxThreads; th++) {
            threaads[th].working = false;
        }
        assertTrue(cache.getCacheValues("").size() > 100);
        // wait 1 second to finish all tests
        DistUtils.sleep(1000);
        cache.close();
        log.info("END-----");
    }

}

class ReadingWritingThread extends Thread {
    private static final Logger log = LoggerFactory.getLogger(ReadingWritingThread.class);
    public Cache cache;
    public int maxKeys = 500;
    public boolean working = true;

    public void run() {
        while (working) {
            try {
                long startTime = System.currentTimeMillis();
                for (int i=0; i<50; i++) {
                    String key = "key" + DistUtils.randomInt(maxKeys);
                    String v = cache.withCache(key, k -> {
                        DistUtils.sleep(1);
                        return "value for " + k;
                    }, CacheMode.modeTtlOneHour);
                    String keyToClear = "key" + DistUtils.randomInt(5000);
                    cache.clearCacheContains(keyToClear);
                }
                long totalTime = System.currentTimeMillis() - startTime;

            } catch (Exception ex) {
                log.error("Error while testing multi-thread cache, reason: " + ex.getMessage(), ex);
            }
            DistUtils.sleep(1);
        }
    }

}