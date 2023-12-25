package com.distsystem.api.info;

import com.distsystem.api.dtos.DistAgentMemoryRow;

import java.util.List;

/**
 * */
public class AgentMemoryInfo {

    private long memorySeq;
    private final long countRow;
    protected AgentMemoryRowInfo minRow;
    protected AgentMemoryRowInfo maxRow;
    protected AgentMemoryRowInfo avgRow;
    protected AgentMemoryRowInfo lastRow;

    public AgentMemoryInfo(long memorySeq, long countRow, AgentMemoryRowInfo minRow, AgentMemoryRowInfo maxRow, AgentMemoryRowInfo avgRow, AgentMemoryRowInfo lastRow) {
        this.memorySeq = memorySeq;
        this.countRow = countRow;
        this.minRow = minRow;
        this.maxRow = maxRow;
        this.avgRow = avgRow;
        this.lastRow = lastRow;
    }

    public long getMemorySeq() {
        return memorySeq;
    }

    public long getCountRow() {
        return countRow;
    }

    public AgentMemoryRowInfo getMinRow() {
        return minRow;
    }

    public AgentMemoryRowInfo getMaxRow() {
        return maxRow;
    }

    public AgentMemoryRowInfo getAvgRow() {
        return avgRow;
    }

    public AgentMemoryRowInfo getLastRow() {
        return lastRow;
    }
}
