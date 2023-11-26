package com.distsystem.api.dtos;

import com.distsystem.api.BaseRow;
import com.distsystem.api.DaoTable;

import java.time.LocalDateTime;
import java.util.Map;

/**  */
@DaoTable(tableName="DistAgentStorage", keyName="storageName", keyIsUnique=true)
public class DistAgentStorageRow extends BaseRow {

    private String storageName;
    private String storageType;
    private String storageCategory;
    private String storageUrl;
    private String storageHost;
    private String storagePort;
    private String storageUser;
    private String storagePassword;
    private String storageDefinition;
    private String storageParams;
    private LocalDateTime createdDate;
    private int isActive;
    private LocalDateTime lastUpdatedDate;

    public DistAgentStorageRow(String storageName, String storageType, String storageCategory, String storageUrl, String storageHost, String storagePort, String storageUser, String storagePassword, String storageDefinition, String storageParams, LocalDateTime createdDate, int isActive, LocalDateTime lastUpdatedDate) {
        this.storageName = storageName;
        this.storageType = storageType;
        this.storageCategory = storageCategory;
        this.storageUrl = storageUrl;
        this.storageHost = storageHost;
        this.storagePort = storagePort;
        this.storageUser = storageUser;
        this.storagePassword = storagePassword;
        this.storageDefinition = storageDefinition;
        this.storageParams = storageParams;
        this.createdDate = createdDate;
        this.isActive = isActive;
        this.lastUpdatedDate = lastUpdatedDate;
    }
    public DistAgentStorageRow(String storageName, String storageType, String storageCategory, String storageUrl, String storageHost, String storagePort, String storageUser, String storagePassword, String storageDefinition, String storageParams) {
        this.storageName = storageName;
        this.storageType = storageType;
        this.storageCategory = storageCategory;
        this.storageUrl = storageUrl;
        this.storageHost = storageHost;
        this.storagePort = storagePort;
        this.storageUser = storageUser;
        this.storagePassword = storagePassword;
        this.storageDefinition = storageDefinition;
        this.storageParams = storageParams;
        this.createdDate = LocalDateTime.now();
        this.isActive = 1;
        this.lastUpdatedDate = createdDate;
    }

    public String getStorageName() {
        return storageName;
    }

    public String getStorageType() {
        return storageType;
    }

    public String getStorageCategory() {
        return storageCategory;
    }

    public String getStorageUrl() {
        return storageUrl;
    }

    public String getStorageHost() {
        return storageHost;
    }

    public String getStoragePort() {
        return storagePort;
    }

    public String getStorageUser() {
        return storageUser;
    }

    public String getStoragePassword() {
        return storagePassword;
    }

    public String getStorageDefinition() {
        return storageDefinition;
    }

    public String getStorageParams() {
        return storageParams;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public int getIsActive() {
        return isActive;
    }

    public LocalDateTime getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public Object[] toInsertRow() {
        return new Object[] { storageName, storageType, storageCategory, storageUrl, storageHost, storagePort, storageUser, storagePassword, storageDefinition, storageParams, createdDate, isActive, lastUpdatedDate };
    }

    public Map<String, String> toMap() {
        return Map.of("type", "DistAgentStorageRow",
                "storageName", storageName,
                "storageType", storageType,
                "storageCategory", storageCategory,
                "storageUrl", storageUrl,
                "storageHost", "" + storageHost,
                "storagePort", "" + storagePort,
                "createdDate", createdDate.toString(),
                "lastUpdatedDate", lastUpdatedDate.toString());
    }

}
