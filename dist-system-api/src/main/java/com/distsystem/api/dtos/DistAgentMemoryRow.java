package com.distsystem.api.dtos;

import com.distsystem.api.BaseRow;
import com.distsystem.api.DaoTable;

import java.time.LocalDateTime;
import java.util.Map;

/**  */
public class DistAgentMemoryRow extends BaseRow {

    private String agentGuid;
    private long agentWorkingTime;
    private long memorySeq;
    private long memMax;
    private long memTotal;
    private long memFree;
    private long memUsed;
    private long objectsCount;
    private LocalDateTime createdDate;
    private int isActive;
    private LocalDateTime lastUpdatedDate;

    public DistAgentMemoryRow(String agentGuid, long agentWorkingTime, long memorySeq, long memMax, long memTotal, long memFree, long memUsed, long objectsCount, LocalDateTime createdDate, int isActive, LocalDateTime lastUpdatedDate) {
        this.agentGuid = agentGuid;
        this.agentWorkingTime = agentWorkingTime;
        this.memorySeq = memorySeq;
        this.memMax = memMax;
        this.memTotal = memTotal;
        this.memFree = memFree;
        this.memUsed = memUsed;
        this.objectsCount = objectsCount;
        this.createdDate = createdDate;
        this.isActive = isActive;
        this.lastUpdatedDate = lastUpdatedDate;
    }
    public DistAgentMemoryRow(String agentGuid, long agentWorkingTime, long memorySeq, long memMax, long memTotal, long memFree, long memUsed, long objectsCount) {
        this.agentGuid = agentGuid;
        this.agentWorkingTime = agentWorkingTime;
        this.memorySeq = memorySeq;
        this.memMax = memMax;
        this.memTotal = memTotal;
        this.memFree = memFree;
        this.memUsed = memUsed;
        this.objectsCount = objectsCount;
        this.createdDate = LocalDateTime.now();
        this.isActive = 1;
        this.lastUpdatedDate = createdDate;
    }

    public Object[] toInsertRow() {
        return new Object[] { agentGuid, agentWorkingTime, memorySeq, memMax, memTotal, memFree, memUsed, objectsCount, createdDate, isActive, lastUpdatedDate };
    }
    public Map<String, String> toMap() {
        return Map.of("type", "memory",
                "agentguid", agentGuid,
                "memorySeq", ""+memorySeq,
                "memMax", ""+memMax,
                "memTotal", ""+memTotal,
                "memFree", ""+memFree,
                "isactive", "" + isActive,
                "createddate", createdDate.toString(),
                "lastUpdatedDate", lastUpdatedDate.toString());
    }

    /** get name of key attribute */
    public static String getKeyAttributeName() {
        return "agentGuid";
    }
}
