package com.distsystem.test.custom.cache;

import com.distsystem.DistFactory;
import com.distsystem.api.*;
import com.distsystem.base.CacheStorageBase;
import com.distsystem.storage.InternalWithTtlAndPriorityCacheStorage;
import com.distsystem.agent.util.measure.Stopwatch;
import com.distsystem.utils.DistUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TestInternalTtlPriority {

  @Test
  @Tag("custom")
  public void testLimitsRespected() throws Exception {
    int maxObjects = 1_000, maxItems = 10_000;
    var storage = makeStorage(maxObjects, maxItems);

    var batch1 = makeN(storage, "batch1-", 100, 3, Long.MAX_VALUE, 1);
    var batch2 = makeN(storage, "batch2-", 200, 5, Long.MAX_VALUE, 2);
    var batch3 = makeN(storage, "batch3-", 1000, 1, Long.MAX_VALUE, 3);
    runAll(batch1, batch2, batch3);

    assertThat(storage.getObjectsCount(), lessThanOrEqualTo(maxObjects));
    storage.clearCache(CacheClearMode.ALL_ELEMENTS);

    var batch4 = makeN(storage, "batch4-", 5000, 15, Long.MAX_VALUE, 1);
    var batch5 = makeN(storage, "batch5-", 2000, 7, Long.MAX_VALUE, 1);
    var batch6 = makeN(storage, "batch6-", 2000, 3, Long.MAX_VALUE, 1);
    runAll(batch4, batch5, batch6);

    assertThat(storage.getObjectsCount(), lessThanOrEqualTo(maxObjects));
    assertThat(storage.getItemsCount(), lessThanOrEqualTo(maxItems));
    System.out.printf("obj count: %d, item count: %d\n", storage.getObjectsCount(), storage.getItemsCount());

  }

  @Test
  public void testClearContains() {
    var storage = makeStorage(1_000, 10_000);

    var batch1 = makeN(storage, "batch1-", 500, 1, Long.MAX_VALUE, 1);
    var batch2 = makeN(storage, "batch2-", 500, 2, Long.MAX_VALUE, 2);

    runAll(batch1, batch2);
    var batch1InCache = storage.getKeys("batch1").size();
    var batch2InCache = storage.getKeys("batch2").size();
    assertThat(batch1InCache, equalTo(500));
    assertThat(batch2InCache, equalTo(500));
    assertThat(storage.getObjectsCount(), equalTo(1000));
    assertThat(storage.getItemsCount(), equalTo(1500));

    storage.clearCacheContains("batch1");
    batch1InCache = storage.getKeys("batch1").size();
    batch2InCache = storage.getKeys("batch2").size();
    assertThat(batch1InCache, equalTo(0));
    assertThat(batch2InCache, equalTo(500));
    assertThat(storage.getObjectsCount(), equalTo(500));
    assertThat(storage.getItemsCount(), equalTo(1000));
  }

  @Test
  public void testTtl() {
    var storage = makeStorage(1_000, 10_000);
    var t0 = System.currentTimeMillis();

    var batch1 = makeN(storage, "batch1-", 200, 1, 400, 1);
    var batch2 = makeN(storage, "batch2-", 200, 1, 800, 1);
    var batch3 = makeN(storage, "batch3-", 200, 1, 1200, 1);
    runAll(batch1, batch2, batch3);
    System.out.printf("Inserted at t=%d\n", System.currentTimeMillis() - t0);

    assertThat(storage.getObjectsCount(), equalTo(600));

    DistUtils.sleep(400);
    storage.onTimeClean(0);
    System.out.printf("First cleanup finished at t=%d\n", System.currentTimeMillis() - t0);

    var keysAfterFirstClean = storage.getKeys("");
    assertThat(storage.getObjectsCount(), equalTo(400));

    DistUtils.sleep(400);
    storage.onTimeClean(0);
    System.out.printf("Second cleanup finished at t=%d\n", System.currentTimeMillis() - t0);

    BiFunction<Set<String>, String, Integer> keyCount = (set, key) -> set.stream().filter(k -> k.contains(key)).collect(Collectors.toSet()).size();

    var keysAfterSecondClean = storage.getKeys("");
    assertThat(storage.getObjectsCount(), equalTo(200));

    DistUtils.sleep(400);
    storage.onTimeClean(0);
    System.out.printf("Third cleanup finished at t=%d\n", System.currentTimeMillis() - t0);

    assertThat(storage.getObjectsCount(), equalTo(0));
    assertThat(storage.getKeys("").size(), equalTo(0));

    assertThat(keyCount.apply(keysAfterFirstClean, "batch1"), equalTo(0));
    assertThat(keyCount.apply(keysAfterFirstClean, "batch2"), equalTo(200));
    assertThat(keyCount.apply(keysAfterFirstClean, "batch3"), equalTo(200));

    assertThat(keyCount.apply(keysAfterSecondClean, "batch1"), equalTo(0));
    assertThat(keyCount.apply(keysAfterSecondClean, "batch2"), equalTo(0));
    assertThat(keyCount.apply(keysAfterSecondClean, "batch3"), equalTo(200));

  }

//  @Test
  public void testCleanupTimes() {
    var storage = makeStorage(500_000, 10_000_000);

    var stopwatch = Stopwatch.start();
    var batch1 = makeN(storage, "batch1-", 50000, 5, 4000, 1);
    var batch2 = makeN(storage, "batch2-", 50000, 5, 5000, 1);
    var batch3 = makeN(storage, "batch3-", 50000, 5, 6000, 1);
    var batch4 = makeN(storage, "batch4-", 50000, 5, 7000, 1);
    var batch5 = makeN(storage, "batch5-", 50000, 5, 8000, 1);
    var batch6 = makeN(storage, "batch6-", 50000, 5, 9000, 1);
    runAll(batch1, batch2, batch3, batch4, batch5, batch6);
    System.out.printf("Inserted in %s\n", stopwatch.tickPretty());

    for (int i = 0; i < 10; i++) {
      DistUtils.sleep(1000);
      storage.onTimeClean(0);
      System.out.printf("Cleanup no.%d done at t=%s\n", i + 1, stopwatch.tickPretty());
    }

  }

  private InternalWithTtlAndPriorityCacheStorage makeStorage(int maxObjects, int maxItems) {
    var props = new Properties();
    props.put(DistConfig.AGENT_CACHE_MAX_LOCAL_OBJECTS, "" + maxObjects);
    props.put(DistConfig.AGENT_CACHE_MAX_LOCAL_ITEMS, "" + maxItems);
    var cc = new DistConfig(props);
    var cache = DistFactory.createCacheInstance(props);
    return new InternalWithTtlAndPriorityCacheStorage(cache);
  }

  @SafeVarargs
  private void runAll(List<Runnable>... batches) {
    var rs = new LinkedList<Runnable>();
    for (var b : batches) rs.addAll(b);
    Collections.shuffle(rs);
    var futs = rs.stream().map(CompletableFuture::runAsync).collect(Collectors.toUnmodifiableList());
    CompletableFuture.allOf(futs.toArray(new CompletableFuture[rs.size()])).join();
  }

  private List<Runnable> makeN(CacheStorageBase storage, String keyPrefix, int nReps, int itemCount, long ttl, int priority) {
    return ns().limit(nReps)
        .map(i -> make(keyPrefix + i, itemCount, ttl, priority))
        .map(co -> (Runnable) () -> storage.setObject(co))
        .collect(Collectors.toUnmodifiableList());
  }

  private Stream<Integer> ns() {
    return Stream.iterate(1, i -> i + 1);
  }

  private CacheObject make(String key, int itemCount, long ttl, int priority) {
    var obj = new String[itemCount];
    return new CacheObject(key, obj, 0, new CacheMode(CacheMode.Mode.TTL, ttl, true, false, priority));
  }
}
