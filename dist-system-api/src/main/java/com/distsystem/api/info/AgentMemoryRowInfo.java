package com.distsystem.api.info;

import com.distsystem.api.dtos.DistAgentMemoryRow;

import java.time.LocalDateTime;
import java.util.List;

/**
 * */
public class AgentMemoryRowInfo {
    private long memorySeq;
    private long memMax;
    private long memTotal;
    private long memFree;
    private long memUsed;
    private long objectsCount;
    private LocalDateTime createdDate;

    public AgentMemoryRowInfo(long memorySeq, long memMax, long memTotal, long memFree, long memUsed, long objectsCount, LocalDateTime createdDate) {
        this.memorySeq = memorySeq;
        this.memMax = memMax;
        this.memTotal = memTotal;
        this.memFree = memFree;
        this.memUsed = memUsed;
        this.objectsCount = objectsCount;
        this.createdDate = createdDate;
    }
    public void min(AgentMemoryRowInfo row) {
        this.memMax = Math.min(this.memMax, row.memMax);
        this.memTotal = Math.min(this.memTotal, row.memTotal);
        this.memFree = Math.min(this.memFree, row.memFree);
        this.memUsed = Math.min(this.memUsed, row.memUsed);
        this.objectsCount = Math.min(this.objectsCount, row.objectsCount);
    }
    public void max(AgentMemoryRowInfo row) {
        this.memMax = Math.max(this.memMax, row.memMax);
        this.memTotal = Math.max(this.memTotal, row.memTotal);
        this.memFree = Math.max(this.memFree, row.memFree);
        this.memUsed = Math.max(this.memUsed, row.memUsed);
        this.objectsCount = Math.max(this.objectsCount, row.objectsCount);
    }
    public void sum(AgentMemoryRowInfo row) {
        this.memMax += row.memMax;
        this.memTotal += row.memTotal;
        this.memFree = + row.memFree;
        this.memUsed = + row.memUsed;
        this.objectsCount = + row.objectsCount;
    }
    /** */
    public AgentMemoryRowInfo produceAvg(long cnt) {
        return new AgentMemoryRowInfo(memorySeq, memMax/cnt, memTotal/cnt, memFree/cnt, memUsed/cnt, objectsCount/cnt, createdDate);
    }

    public long getMemorySeq() {
        return memorySeq;
    }

    public long getMemMax() {
        return memMax;
    }

    public long getMemTotal() {
        return memTotal;
    }

    public long getMemFree() {
        return memFree;
    }

    public long getMemUsed() {
        return memUsed;
    }

    public long getObjectsCount() {
        return objectsCount;
    }

    public String getCreatedDate() {
        return createdDate.toString();
    }
    public double getUsedPercentage() {
        return 100.0 * memUsed / (double)memTotal;
    }
    public double getUsedToMaxPercentage() {
        return 100.0 * memUsed / (double)memMax;
    }
    public double getFreePercentage() {
        return 100.0 * memFree / (double)memTotal;
    }
    public double getFreeToMaxPercentage() {
        return 100.0 * memFree / (double)memMax;
    }
    public long getMemUsedMb() {
        return memUsed / 1024L / 1024L;
    }
    public long getMemFreeMb() {
        return memFree / 1024L / 1024L;
    }

}

