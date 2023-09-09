package com.distsystem.api;

import java.util.HashMap;

/** registering cache object through REST endpoint */
public class CacheRegister {

    /** unique ID of this cache */
    public String cacheGuid;
    /** properties for the stream */
    public HashMap<String, String> properties;

    public CacheRegister() {
        this.cacheGuid = "";
        this.properties = new HashMap<>();
    }
    public CacheRegister(String cacheGuid) {
        this.cacheGuid = cacheGuid;
        this.properties = new HashMap<>();
    }
    public CacheRegister(String cacheGuid, HashMap<String, String> properties) {
        this.cacheGuid = cacheGuid;
        this.properties = properties;
    }

}
