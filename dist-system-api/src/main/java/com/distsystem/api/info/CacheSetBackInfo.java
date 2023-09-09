package com.distsystem.api.info;

import java.util.List;

/** class to be put to cache - it contains object caches AND many other statistics
 * this cache is representing internal cache with object stored */
public class CacheSetBackInfo {

    /** info about previous objects in cache */
    private List<CacheObjectInfo> prevObjects;
    /** */
    private CacheObjectInfo currentObject;

    public CacheSetBackInfo(List<CacheObjectInfo> prevObjects, CacheObjectInfo currentObject) {
        this.prevObjects = prevObjects;
        this.currentObject = currentObject;
    }
    public List<CacheObjectInfo> getPrevObjects() {
        return prevObjects;
    }
    public CacheObjectInfo getCurrentObject() {
        return currentObject;
    }
}
