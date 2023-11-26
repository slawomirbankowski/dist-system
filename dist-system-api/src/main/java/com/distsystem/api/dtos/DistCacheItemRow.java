package com.distsystem.api.dtos;

import com.distsystem.api.BaseRow;
import com.distsystem.api.DaoTable;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;


@DaoTable(tableName="DistCacheItem", keyName="cachekey", keyIsUnique=true)
public class DistCacheItemRow extends BaseRow {
    private final String cachekey;
    private final String cachevalue;
    private final java.util.Date inserteddate;

    private int isActive;
    private LocalDateTime createdDate;
    private LocalDateTime lastUpdatedDate;

    public DistCacheItemRow(String cachekey, String cachevalue, java.util.Date inserteddate) {
        this.cachekey = cachekey;
        this.cachevalue = cachevalue;
        this.inserteddate = inserteddate;
    }
    public DistCacheItemRow(String cachekey, String cachevalue) {
        this.cachekey = cachekey;
        this.cachevalue = cachevalue;
        this.inserteddate = new java.util.Date();
    }

    public String getCachekey() {
        return cachekey;
    }
    public String getCachevalue() {
        return cachevalue;
    }
    public Date getInserteddate() {
        return inserteddate;
    }

    public int getIsActive() {
        return isActive;
    }
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public LocalDateTime getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public Object[] toInsertRow() {
        return new Object[] { cachekey, cachevalue, inserteddate };
    }

    public Map<String, String> toMap() {
        return Map.of("type", "DistCacheItemRow",
                "cachekey", cachekey,
                "cachevalue", cachevalue,
                "lastUpdatedDate", inserteddate.toString());
    }

    public static DistCacheItemRow fromMap(Map<String, Object> map) {
        return new DistCacheItemRow(map.getOrDefault("cachekey", "").toString(),
                map.getOrDefault("cachevalue", "").toString());
    }
}
