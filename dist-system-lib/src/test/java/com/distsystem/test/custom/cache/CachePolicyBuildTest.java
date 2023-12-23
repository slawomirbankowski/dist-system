package com.distsystem.test.custom.cache;

import com.distsystem.api.CacheMode;
import com.distsystem.api.CacheObject;
import com.distsystem.api.CachePolicy;
import com.distsystem.api.CachePolicyBuilder;
import com.distsystem.utils.CacheStats;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CachePolicyBuildTest {
    private static final Logger log = LoggerFactory.getLogger(CachePolicyBuildTest.class);

    @Test
    public void cachePolicyBuilderSimpleTest() {
        log.info("START ------ agent register test test");

        String fullPolicy = "sizeMin=10,sizeMax=30,applyPrioritySet=5 ; ttlMin=10000,ttlMax=40000,applyMode=5 ; ttlMin=1000000,applyMode=KEEP";
        CachePolicy policy = CachePolicyBuilder.empty().fromString(fullPolicy).create();

        log.info("Policy items count: " + policy.getItemsCount());
        log.info("Policy items: " + policy.getItemsDefinitions());
        log.info("Policy: " + policy);

        assertTrue(policy.getItemsCount() > 0, "There should be policy");

        long currTime = System.currentTimeMillis();
        Object obj = "objectInCache";
        String key = "key";
        int objSize = 10;
        long acquireTimeMs = 100;
        CacheObject co = new CacheObject(0, currTime, currTime, currTime, key,
                obj, x -> obj, objSize, acquireTimeMs, 0, 0, CacheMode.Mode.TTL, 5, 1000000, Set.of());

        CacheStats stats = new CacheStats();
        stats.refresh();
        policy.checkAndApply(co, stats);

        CachePolicy policy2 = CachePolicyBuilder
                .empty()
                .next().checkSizeMin(100).applyPriority(6)
                .next().checkThread("Main").checkMode(CacheMode.Mode.KEEP).applySizeMultiply(2)
                .next().checkAcquireTime(5000, 99999).applyPriorityIncrease(3)
                .next().checkPriority(0, 3).checkTtl(100000, 999999).applyMode(CacheMode.Mode.KEEP)
                .next().checkKeyContains("UserDto").applyPriorityIncrease(3)
                .next().checkMemoryFree(3000000).applyTtlDivide(10)
                .create();

        log.info("END-----");
    }
}
