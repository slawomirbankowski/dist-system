package com.distsystem.api.dtos;

import com.distsystem.api.BaseRow;
import com.distsystem.api.CacheMode;
import com.distsystem.api.enums.CacheStorageType;
import com.distsystem.utils.AdvancedMap;
import com.distsystem.utils.DistUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/** row for distagentcacheitem table
 * create table distcacheitem(cachekey varchar(4000), cachevalue text, objectclassname text, inserteddate timestamp default (now()), cacheguid text, lastusedate timestamp default (now()), enddate timestamp default (now()), createdtimems bigint, objectseq bigint, objsize bigint, acquiretimems bigint, cachemode int, cachepriority int, groupslist text)
 * */
public class DistAgentCacheItemRow extends BaseRow {

    /** key of this object stored in cache */
    private final String cacheKey;
    private final String cacheValue;
    private final String objectClassName;
    private final String cacheGuid;
    private final String groupsList;
    private final LocalDateTime endDate;
    private final long objectSeq;
    private final long objectSize;
    private long acquireTimeMs;
    private long objectUsages;
    private long objectRefreshes;
    private long timeToLiveMs;
    private int addToInternal;
    private int addToExternal;
    private final int cacheMode;
    private final int cachePriority;
    private final int isActive;
    private final LocalDateTime createdDate;
    private final LocalDateTime lastUpdatedDate;

    public DistAgentCacheItemRow(String cacheKey, String cacheValue, String objectClassName, String cacheGuid, String groupsList, LocalDateTime endDate, long objectSeq, long objectSize, long acquireTimeMs, long objectUsages, long objectRefreshes, long timeToLiveMs, int addToInternal, int addToExternal, int cacheMode, int cachePriority) {
        this.cacheKey = cacheKey;
        this.cacheValue = cacheValue;
        this.objectClassName = objectClassName;
        this.cacheGuid = cacheGuid;
        this.groupsList = groupsList;
        this.endDate = endDate;
        this.objectSeq = objectSeq;
        this.objectSize = objectSize;
        this.acquireTimeMs = acquireTimeMs;
        this.objectUsages = objectUsages;
        this.objectRefreshes = objectRefreshes;
        this.timeToLiveMs = timeToLiveMs;
        this.addToInternal = addToInternal;
        this.addToExternal = addToExternal;
        this.cacheMode = cacheMode;
        this.cachePriority = cachePriority;
        this.isActive = 1;
        this.createdDate = LocalDateTime.now();
        this.lastUpdatedDate = createdDate;
    }

    public Object[] toInsertRow() {
        return new Object[] { cacheKey, cacheValue, objectClassName, cacheGuid, groupsList, endDate, objectSeq,
                objectSize, acquireTimeMs, objectUsages, objectRefreshes, timeToLiveMs, addToInternal,
                addToExternal, cacheMode, cachePriority, isActive, createdDate, lastUpdatedDate };
    }

    /** get name of key attribute */
    public static String getKeyAttributeName() {
        return "cacheKey";
    }
    public Map<String, String> toMap() {
        return Map.of("type", "DistAgentCacheItemRow",
                "configValue", lastUpdatedDate.toString(),
                "createdDate", createdDate.toString());
    }
    public static DistAgentCacheItemRow fromMap(Map<String, Object> map) {
        AdvancedMap m = new AdvancedMap(map, true);
        return new DistAgentCacheItemRow(
                m.getStringOrEmpty("cacheKey"),
                m.getStringOrEmpty("cacheValue"),
                m.getStringOrEmpty("objectClassName"),
                m.getStringOrEmpty("cacheGuid"),
                m.getStringOrEmpty("groupsList"),
                m.getLocalDateOrNow("endDate"),
                m.getLongOrZero("objectSeq"),
                m.getLongOrZero("objectSize"),
                m.getLongOrZero("acquireTimeMs"),
                m.getLongOrZero("objectUsages"),
                m.getLongOrZero("objectRefreshes"),
                m.getLongOrZero("timeToLiveMs"),
                m.getIntOrZero("addToInternal"),
                m.getIntOrZero("addToExternal"),
                m.getIntOrZero("cacheMode"),
                m.getIntOrZero("cachePriority"));
    }
}
