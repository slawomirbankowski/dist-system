package com.distsystem.utils;

import com.distsystem.api.CacheObject;
import com.distsystem.interfaces.CacheStorage;

import java.util.concurrent.atomic.AtomicLong;

/** statistics about cache manager, objects, keys */
public class CacheStats {

    // TODO: finish statistics for cache, add more cache stats per key, storage, ...
    // valueLast, valueSum, valueCount, List<TimeBucket, valueAvg, valueSum, valueMin, valueMax, valueCount>,
    // getLast() getAvg()

    /** memory stats for this Cache instance - this is to compare items in cache with memory usage */
    private long freeMemory;
    private long totalMemory;
    private long maxMemory;

    private AtomicLong totalReads = new AtomicLong();
    private AtomicLong totalAcquires = new AtomicLong();
    private AtomicLong totalAcquireTimeSum = new AtomicLong();

    private AtomicLong totalMissCount = new AtomicLong();
    private AtomicLong totalHitCount = new AtomicLong();
    private AtomicLong totalSetCount = new AtomicLong();

    /** class to register hit/miss ratio with historical records */
    protected CacheHitRatio hitRatio = new CacheHitRatio();

    /** check sequence - this is number of executions of onTime() method */
    protected AtomicLong checkSequence = new AtomicLong();

    /** */
    protected AtomicLong addedIssuesSequence = new AtomicLong();
    protected AtomicLong processedMessagesSequence = new AtomicLong();
    protected AtomicLong handleRequestSequence = new AtomicLong();

    public CacheStats() {
        refreshMemory();
    }
    /** */
    public void refresh() {
        refreshMemory();
        refreshStats();
    }

    public long getFreeMemory() {
        return freeMemory;
    }
    public long getTotalMemory() {
        return totalMemory;
    }
    public long getMaxMemory() {
        return maxMemory;
    }

    public long check() {
        return checkSequence.incrementAndGet();
    }
    public long checksCount() {
        return checkSequence.get();
    }
    public void addIssue() {
        addedIssuesSequence.incrementAndGet();
    }
    public void writeObjectToStorages(CacheObject co) {

    }
    /** */
    public void setCacheObject(String key) {
        totalSetCount.incrementAndGet();
    }
    /** */
    public void getCacheObject(String key) {
        totalReads.incrementAndGet();
    }
    /** */
    public void getObjectMiss(String key) {
        totalMissCount.incrementAndGet();
    }
    /** */
    public void getObjectHit(String key, CacheStorage storage, long storageReadTimeMs) {
        totalHitCount.incrementAndGet();
    }

    public void getObjectErrorRead(String key, CacheStorage storage) {

    }
    /** */
    public long addedItemsCount() {
        return totalSetCount.get();
    }

    public long issuesAddedCount() {
        return addedIssuesSequence.get();
    }

    /** */
    public void processMessage() {
        processedMessagesSequence.incrementAndGet();
    }
    /** */
    public void handleRequest() {
        handleRequestSequence.incrementAndGet();
    }
    /** */
    public void initializeSingleStorage() {

    }
    public void acquireObject(String key, long ackTime) {
        totalAcquires.incrementAndGet();
        totalAcquireTimeSum.addAndGet(ackTime);
    }

    /** refresh memory from Runtime  */
    private void refreshMemory() {
        Runtime rt = java.lang.Runtime.getRuntime();
        freeMemory = rt.freeMemory();
        totalMemory = rt.totalMemory();
        maxMemory = rt.maxMemory();
    }
    private void refreshStats() {

    }

    /** String from this statistics */
    public String toString() {
        return "STATS, freeMemory: " + freeMemory + "";
    }

}
