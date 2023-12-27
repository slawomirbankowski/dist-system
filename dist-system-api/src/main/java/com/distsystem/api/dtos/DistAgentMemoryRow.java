package com.distsystem.api.dtos;

import com.distsystem.api.BaseRow;
import com.distsystem.api.DaoTable;
import com.distsystem.api.info.AgentMemoryRowInfo;
import com.distsystem.utils.DistUtils;

import java.time.LocalDateTime;
import java.util.Map;

/**  */
public class DistAgentMemoryRow extends BaseRow {

    private String memoryGuid;
    private String agentGuid;
    private long agentWorkingTime;
    private long memorySeq;
    private long memMax;
    private long memTotal;
    private long memFree;
    private long memUsed;
    private long objectsCount;
    private int isActive;
    private LocalDateTime createdDate;
    private LocalDateTime lastUpdatedDate;

    public DistAgentMemoryRow(String agentGuid, long agentWorkingTime, long memorySeq, long memMax, long memTotal, long memFree, long memUsed, long objectsCount, LocalDateTime createdDate, int isActive, LocalDateTime lastUpdatedDate) {
        this.memoryGuid = DistUtils.generateCustomGuid("MEM");
        this.agentGuid = agentGuid;
        this.agentWorkingTime = agentWorkingTime;
        this.memorySeq = memorySeq;
        this.memMax = memMax;
        this.memTotal = memTotal;
        this.memFree = memFree;
        this.memUsed = memUsed;
        this.objectsCount = objectsCount;
        this.isActive = isActive;
        this.createdDate = createdDate;
        this.lastUpdatedDate = lastUpdatedDate;
    }
    public DistAgentMemoryRow(String agentGuid, long agentWorkingTime, long memorySeq, long memMax, long memTotal, long memFree, long memUsed, long objectsCount) {
        this.memoryGuid = DistUtils.generateCustomGuid("MEM");
        this.agentGuid = agentGuid;
        this.agentWorkingTime = agentWorkingTime;
        this.memorySeq = memorySeq;
        this.memMax = memMax;
        this.memTotal = memTotal;
        this.memFree = memFree;
        this.memUsed = memUsed;
        this.objectsCount = objectsCount;
        this.isActive = 1;
        this.createdDate = LocalDateTime.now();
        this.lastUpdatedDate = createdDate;
    }

    public Object[] toInsertRow() {
        return new Object[] { memoryGuid, agentGuid, agentWorkingTime, memorySeq, memMax, memTotal, memFree, memUsed, objectsCount, isActive, createdDate, lastUpdatedDate };
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

    public String getMemoryGuid() {
        return memoryGuid;
    }

    public String getAgentGuid() {
        return agentGuid;
    }

    public long getAgentWorkingTime() {
        return agentWorkingTime;
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

    public int getIsActive() {
        return isActive;
    }

    public String getCreatedDate() {
        return createdDate.toString();
    }

    public String getLastUpdatedDate() {
        return lastUpdatedDate.toString();
    }

    /** create info from this row  */
    public AgentMemoryRowInfo toInfo() {
        return new AgentMemoryRowInfo(memorySeq, memMax, memTotal, memFree, memUsed, objectsCount, createdDate);
    }
    /** get name of key attribute */
    public static String getKeyAttributeName() {
        return "agentGuid";
    }
}
