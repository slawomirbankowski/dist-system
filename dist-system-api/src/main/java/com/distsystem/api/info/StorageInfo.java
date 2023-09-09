package com.distsystem.api.info;

import java.time.LocalDateTime;
import java.util.Map;

/** information about storage - object to be serialized to JSON and returned to user via dist-cache application */
public class StorageInfo {
    /** UID for storage */
    private String storageUid;
    /** storage date and time of creation*/
    private LocalDateTime storageCreatedDate;
    private String storageClassName;
    private int itemsCount;
    private int objectsCount;
    private boolean internal;
    private boolean global;
    private Map<String, Object> parameters;

    public StorageInfo() {
        this.storageUid = null;
        this.storageCreatedDate = null;
        this.storageClassName = null;
        this.itemsCount = -1;
        this.objectsCount = -1;
        this.internal = true;
        this.global = false;
        this.parameters = Map.of();
    }
    public StorageInfo(String storageUid, LocalDateTime storageCreatedDate, String storageClassName, int itemsCount, int objectsCount, boolean internal, boolean global, Map<String, Object> parameters) {
        this.storageUid = storageUid;
        this.storageCreatedDate = storageCreatedDate;
        this.storageClassName = storageClassName;
        this.itemsCount = itemsCount;
        this.objectsCount = objectsCount;
        this.internal = internal;
        this.global = global;
        this.parameters = parameters;
    }

    public String getStorageUid() {
        return storageUid;
    }

    public String getStorageCreatedDate() {
        return storageCreatedDate.toString();
    }

    public String getStorageClassName() {
        return storageClassName;
    }

    public int getItemsCount() {
        return itemsCount;
    }

    public int getObjectsCount() {
        return objectsCount;
    }

    public boolean isInternal() {
        return internal;
    }

    public boolean isGlobal() {
        return global;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }
}
