package com.distsystem.base.dtos;

import java.util.Map;


public class DistCacheItemRow {
    public String cachekey;
    public String cachevalue;
    public java.util.Date inserteddate;

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
    public static DistCacheItemRow fromMap(Map<String, Object> map) {
        return new DistCacheItemRow(map.getOrDefault("cachekey", "").toString(),
                map.getOrDefault("cachevalue", "").toString());
    }
}
