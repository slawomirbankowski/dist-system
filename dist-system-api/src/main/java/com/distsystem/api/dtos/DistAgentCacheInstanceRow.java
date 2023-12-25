package com.distsystem.api.dtos;

import com.distsystem.api.BaseRow;
import com.distsystem.utils.AdvancedMap;

import java.time.LocalDateTime;
import java.util.Map;

/** row for DistAgentCacheInstanceRow table
 * */
public class DistAgentCacheInstanceRow extends BaseRow {

    /** */
    private final String cacheInstanceGuid;
    private final String agentGuid;
    private final long objectsCount;
    private final int isActive;
    private final LocalDateTime createdDate;
    private final LocalDateTime lastUpdatedDate;

    public DistAgentCacheInstanceRow(String cacheInstanceGuid, String agentGuid, long objectsCount, int isActive, LocalDateTime createdDate, LocalDateTime lastUpdatedDate) {
        this.cacheInstanceGuid = cacheInstanceGuid;
        this.agentGuid = agentGuid;
        this.objectsCount = objectsCount;
        this.isActive = isActive;
        this.createdDate = createdDate;
        this.lastUpdatedDate = lastUpdatedDate;
    }
    public DistAgentCacheInstanceRow(String cacheInstanceGuid, String agentGuid) {
        this.cacheInstanceGuid = cacheInstanceGuid;
        this.agentGuid = agentGuid;
        this.objectsCount = 0L;
        this.isActive = 1;
        this.createdDate = LocalDateTime.now();
        this.lastUpdatedDate = createdDate;
    }

    public Object[] toInsertRow() {
        return new Object[] { cacheInstanceGuid, agentGuid, objectsCount, isActive, createdDate, lastUpdatedDate };
    }

    /** get name of key attribute */
    public static String getKeyAttributeName() {
        return "cacheInstanceGuid";
    }
    public Map<String, String> toMap() {
        return Map.of("type", "DistAgentCacheInstanceRow",
                "agentGuid", agentGuid,
                "lastUpdatedDate", lastUpdatedDate.toString(),
                "createdDate", createdDate.toString());
    }
    public static DistAgentCacheInstanceRow fromMap(Map<String, Object> map) {
        AdvancedMap m = new AdvancedMap(map, true);
        return new DistAgentCacheInstanceRow(
                m.getStringOrEmpty("cacheInstanceGuid"),
                m.getStringOrEmpty("agentGuid"),
                m.getLongOrZero("objectsCount"),
                m.getIntOrZero("isActive"),
                m.getLocalDateOrNow("createdDate"),
                m.getLocalDateOrNow("lastUpdatedDate"));
    }
}
