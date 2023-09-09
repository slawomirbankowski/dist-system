package com.distsystem.utils;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

/** cache modes for objects to be kept in cache */
public class CacheHitRatio {

    /** */
    private Queue<Double> recentHitRatios = new LinkedList<>();
    private AtomicLong hits = new AtomicLong();
    private AtomicLong misses = new AtomicLong();
    /** */
    private String currentPeriod = "";

    public CacheHitRatio() {
    }
    public long hit() {
        return hits.incrementAndGet();
    }
    public long miss() {
        return misses.incrementAndGet();
    }

    public String currentPeriod() {
        return LocalDateTime.now().toString().substring(1, 10);
    }


}
class HitRatioStat {
    public String period;
    public Optional<Double> hitRatio;
    public long total;
    public HitRatioStat() {
    }
}