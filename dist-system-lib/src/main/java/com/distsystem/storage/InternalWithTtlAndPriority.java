package com.distsystem.storage;

import com.distsystem.api.*;
import com.distsystem.api.enums.CacheStorageType;
import com.distsystem.api.info.CacheObjectInfo;
import com.distsystem.base.CacheStorageBase;
import com.distsystem.interfaces.Cache;
import com.distsystem.util.measure.TimedResult;
import com.distsystem.utils.DistUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class InternalWithTtlAndPriority extends CacheStorageBase {
  private static final Logger log = LoggerFactory.getLogger(InternalWithTtlAndPriority.class);


  private final int maxObjectCount;
  private final int maxItemCount;

  private final AtomicInteger objCount = new AtomicInteger(0);
  private final AtomicInteger itemCount = new AtomicInteger(0);

  private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(true);

  private Map<String, CacheObject> byKey;
  private NavigableMap<Integer, LinkedList<String>> byPriority;

  public InternalWithTtlAndPriority(Cache cache) {
    super(cache);
    this.maxObjectCount = cache.getConfig().getPropertyAsInt(DistConfig.CACHE_MAX_LOCAL_OBJECTS, DistConfig.CACHE_MAX_LOCAL_OBJECTS_VALUE);
    this.maxItemCount = cache.getConfig().getPropertyAsInt(DistConfig.CACHE_MAX_LOCAL_ITEMS, DistConfig.CACHE_MAX_LOCAL_ITEMS_VALUE);
    this.byPriority = new TreeMap<>();
    this.byKey = new HashMap<>();
  }

  @Override
  public Optional<CacheObject> getObject(String key) {
    return withReadLock(() -> Optional.ofNullable(byKey.get(key)));
  }

  @Override
  public Optional<CacheObject> setObject(CacheObject o) {
    var itemCount = DistUtils.itemCount(o);

    withWriteLock(() -> {
      log.trace("Set object {} with {} items, thread {} got the lock", o.getKey(), itemCount, Thread.currentThread().getId());
      var overLimit = this.overLimit(itemCount);
      if (overLimit.isOverLimit()) removeOverLimit(overLimit);

      byKey.put(o.getKey(), o);
      byPriority.computeIfAbsent(o.getPriority(), __ -> new LinkedList<>()).addFirst(o.getKey());
      log.trace("Object {} added successfully", o.getKey());
      this.itemCount.addAndGet(itemCount);
      objCount.incrementAndGet();
    });

    return Optional.of(o);
  }

  @Override
  public void removeObjectsByKeys(Collection<String> keys) {
    withWriteLock(() -> {
      var counters = new int[]{0, 0};
      keys.forEach(k -> {
        counters[0] += 1;
        counters[1] += DistUtils.itemCount(byKey.get(k));
        byKey.remove(k);
      });
      objCount.addAndGet(-counters[0]);
      itemCount.addAndGet(-counters[1]);
    });
  }

  @Override
  public void removeObjectByKey(String key) {
    byKey.remove(key);
  }

  @Override
  public int getObjectsCount() {
    return objCount.get();
  }

  @Override
  public int getItemsCount() {
    return itemCount.get();
  }

  @Override
  public Set<String> getKeys(String containsStr) {
    if (containsStr == null) return Collections.emptySet();
    rwLock.readLock().lock();
    var res = new HashSet<String>();
    byKey.forEach((k, v) -> {
      if (k.contains(containsStr)) res.add(k);
    });
    rwLock.readLock().unlock();
    return res;
  }

  @Override
  public List<CacheObjectInfo> getInfos(String containsStr) {
    return withReadLock(() ->
        byKey.keySet().stream()
            .filter(k -> k.contains(containsStr))
            .map(byKey::get)
            .map(CacheObject::getInfo)
            .collect(Collectors.toUnmodifiableList())
    );
  }

  /** get values of cache objects that contains given String in key */
  public List<CacheObject> getValues(String containsStr) {
    return withReadLock(() ->
            byKey.keySet().stream()
                    .filter(k -> k.contains(containsStr))
                    .map(byKey::get)
                    .collect(Collectors.toUnmodifiableList())
    );
  }
  /** clear cache by given mode
   * returns estimated of elements cleared */
  public int clearCache(CacheClearMode clearMode) {
    return withWriteLock(() -> {
      var sum = byKey.size() + byPriority.size();
      byKey = new HashMap<>();
      byPriority = new TreeMap<>();
      this.objCount.set(0);
      this.itemCount.set(0);
      return sum;
    });
  }
  @Override
  public int clearCacheForGroup(String groupName) {
    return withWriteLock(() -> {
      var sum = byKey.size() + byPriority.size();
      byKey = new HashMap<>();
      byPriority = new TreeMap<>();
      this.objCount.set(0);
      this.itemCount.set(0);
      return sum;
    });
  }

  @Override
  public int clearCacheContains(String str) {
    log.trace("clearCacheContains({})", str);
    var counters = new int[]{0, 0};
    withWriteLock(() -> {
      var keysToDelete = byKey.keySet().stream()
          .filter(k -> k.contains(str))
          .collect(Collectors.toList());
      keysToDelete.forEach(k -> {
        var removed = byKey.remove(k);
        counters[0] += 1;
        counters[1] += DistUtils.itemCount(removed);
      });
      log.debug("removed {} objects and {} items", counters[0], counters[1]);
      this.objCount.addAndGet(-counters[0]);
      this.itemCount.addAndGet(-counters[1]);
    });

    return counters[0];
  }

  @Override
  public void onTimeClean(long checkSeq) {
    var counters = new int[]{0, 0};
    var timers = new long[]{0, 0};
    log.trace("Running mark and sweep");
    var candidates = withReadLock(() -> {
      var t0 = System.nanoTime();
      var res = byKey.values().stream().filter(CacheObject::isOutdated).map(CacheObject::getKey).collect(Collectors.toSet());
      timers[0] = System.nanoTime() - t0;
      return res;
    });

    log.debug("Collected {} candidates for removal in {}", candidates.size(), TimedResult.prettyNs(timers[0]));

    withWriteLock(() -> {
      var t0 = System.nanoTime();
      var prioritiesToRemove = new HashSet<Integer>();
      byPriority.forEach((priority, keys) -> {
        keys.removeAll(candidates);
        if (keys.isEmpty()) prioritiesToRemove.add(priority);
      });
      prioritiesToRemove.forEach(byPriority::remove);

      candidates.forEach(c -> {
        var co = byKey.get(c);
        if (co != null && co.isOutdated()) {
          counters[0] += 1;
          counters[1] += DistUtils.itemCount(co);
          removeObjectByKey(co.getKey());
        } else {
          log.debug("Candidate {} not found, or it has been renewed in the meantime", c);
        }
      });
      timers[1] = System.nanoTime() - t0;
      this.objCount.addAndGet(-counters[0]);
      this.itemCount.addAndGet(-counters[1]);
    });
    log.debug("Removed {}/{} objects/items in {} during sweep",
        counters[0], counters[1], TimedResult.prettyNs(timers[1]));
  }

  @Override
  public boolean isInternal() {
    return true;
  }
  /** returns true if storage is global,
   * it means that one global shared storage is available for all cache instances*/
  public boolean isGlobal() { return false; }
  /** get additional info parameters for this storage */
  public Map<String, Object> getStorageAdditionalInfo() {
    return Map.of("className", byKey.getClass().getName(),
             " byPrioritySize", byPriority.size());
  }
  /** get type of this storage */
  public CacheStorageType getStorageType() {
    return CacheStorageType.memory;
  }

  private OverLimit overLimit(int itemCount) {
    return new OverLimit(
        objCount.get() < maxObjectCount ? 0 : 1,
        Math.max(this.itemCount.get() + itemCount - maxItemCount, 0)
    );
  }

  /**
   * !!! Run inside a write lock !!!
   * @return oldest key of lowest priority
   */
  private String pollMinPriority() {
    if (!rwLock.isWriteLockedByCurrentThread()) throw new IllegalStateException("Run only inside a write lock");
    var minEntry = byPriority.firstEntry();
    var keys = minEntry.getValue();
    var candidate = keys.pollLast();
    while (candidate == null || !byKey.containsKey(candidate)) {
      if (candidate == null) {
        byPriority.pollFirstEntry();
        minEntry = byPriority.firstEntry();
        keys = minEntry.getValue();
      }
      candidate = keys.pollLast();
    }
    while (keys.peekLast() != null && keys.peekLast().equals(candidate)) keys.pollLast();

    return candidate;
  }

  private void removeOverLimit(OverLimit overLimit) {
    log.trace("Removing {}/{} objects/items over item limit", overLimit.objects, overLimit.items);
    withWriteLock(() -> {
      if (overLimit.items <= 1) {
        var keyToRemove = pollMinPriority();
        if (keyToRemove == null) return; // no more keys in byPriority
        var removed = byKey.remove(keyToRemove);

        if (removed != null) {
          this.objCount.decrementAndGet();
          this.itemCount.addAndGet(-DistUtils.itemCount(removed));
        }
        if (byPriority.firstEntry().getValue().isEmpty()) byPriority.pollFirstEntry();
      } else {
        var keysToRemove = new HashSet<String>();

        var removedSoFar = 0;
        while (removedSoFar < overLimit.items) {
          var currentEntry = byPriority.firstEntry();
          if (currentEntry == null) break;
          var currentKey = currentEntry.getKey();
          var currentObjects = currentEntry.getValue();
          while (removedSoFar < overLimit.items) {
            var keyToRemove = currentObjects.pollLast();
            if (keyToRemove == null) {
              byPriority.remove(currentKey);
              break;
            } else {
              var itemsRemoved = DistUtils.itemCount(byKey.get(keyToRemove));
              removedSoFar += itemsRemoved;
              keysToRemove.add(keyToRemove);
            }
          }
        }
        removeObjectsByKeys(List.copyOf(keysToRemove));
      }
    });
    log.trace("After removing items over the limit. Obj count: {}, item count: {}", this.getObjectsCount(), this.getItemsCount());
  }

  private <T> T withReadLock(Supplier<T> op) {
    rwLock.readLock().lock();
    var res = op.get();
    rwLock.readLock().unlock();
    return res;
  }

  private void withReadLock(Runnable op) {
    withReadLock(() -> {
      op.run();
      return null;
    });
  }

  private <T> T withWriteLock(Supplier<T> op) {
    rwLock.writeLock().lock();

    var res = op.get();
    rwLock.writeLock().unlock();
    return res;
  }

  private void withWriteLock(Runnable op) {
    withWriteLock(() -> {
      op.run();
      return null;
    });
  }

  private static class OverLimit {
    final int objects;
    final int items;

    OverLimit(int objects, int items) {
      this.objects = objects;
      this.items = items;
    }

    boolean isOverLimit() {
      return objects > 0 || items > 0;
    }
  }
}