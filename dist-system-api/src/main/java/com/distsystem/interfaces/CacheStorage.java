package com.distsystem.interfaces;

import com.distsystem.api.enums.CacheStorageType;
import com.distsystem.api.info.StorageInfo;

/** interface for cache storages to keep cache objects with faster access to be read */
public interface CacheStorage {

    /** get unique storage ID */
    String getStorageUid();
    /** get unique ID of cache assigned to this storage */
    String getCacheUid();
    /** get type of this storage */
    CacheStorageType getStorageType();
    /** get name of this storage - by default it is simple name of this class */
    String getStorageName();
    /** get information about this storage */
    StorageInfo getStorageInfo();
    /** get number of objects in cache storage */
    int getObjectsCount();
    /** get number of items in cache storage */
    int getItemsCount();
    /** returns true if storage is internal and cache objects are kept in local memory
     * false if storage is external and cache objects are kept in any storages like Redis, Elasticsearch, DB*/
    boolean isInternal();
    /** returns true if storage is global,
     * it means that one global shared storage is available for all cache instances*/
    boolean isGlobal();

    /** dispose this storage if needed */
    void disposeStorage();

}
