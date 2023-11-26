package com.distsystem.api.dtos;

import com.distsystem.api.BaseRow;
import com.distsystem.api.DaoTable;
import com.distsystem.utils.AdvancedMap;

import java.time.LocalDateTime;
import java.util.Map;

/**
 *
 * */
@DaoTable(tableName="DistAgentAuthKey", keyName="keyName", keyIsUnique=true)
public class DistAgentAuthKeyRow extends BaseRow {

    private final String keyName;
    private final String keyType;
    private final String keyValue;
    private final int isActive;
    private final LocalDateTime createdDate;
    private final LocalDateTime lastUpdatedDate;

    // create table distagentauthkey(keyname text not null, keytype text not null, keyvalue text not null, isactive int not null, createddate timestamp not null)

    public DistAgentAuthKeyRow(String keyName, String keyType, String keyValue, int isActive, LocalDateTime createdDate) {
        this.keyName = keyName;
        this.keyType = keyType;
        this.keyValue = keyValue;
        this.isActive = isActive;
        this.createdDate = createdDate;
        this.lastUpdatedDate = createdDate;
    }
    public DistAgentAuthKeyRow(String keyName, String keyType, String keyValue) {
        this.keyName = keyName;
        this.keyType = keyType;
        this.keyValue = keyValue;
        this.isActive = 1;
        this.createdDate = LocalDateTime.now();
        this.lastUpdatedDate = createdDate;
    }

    public String getKeyName() {
        return keyName;
    }

    public String getKeyType() {
        return keyType;
    }

    public String getKeyValue() {
        return keyValue;
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
        return new Object[] { keyName, keyType, keyValue, isActive, createdDate };
    }

    public Map<String, String> toMap() {
        return Map.of("type", "DistAgentAuthDomainRow",
                "keyName", keyName,
                "keyType", keyType,
                "keyValue", keyValue,
                "isActive", ""+isActive,
                "createdDate", createdDate.toString());
    }
    /** get name of key attribute */
    public static String getKeyAttributeName() {
        return "identityName";
    }
    public static DistAgentAuthKeyRow fromMap(Map<String, Object> map) {
        AdvancedMap m = new AdvancedMap(map, true);
        return new DistAgentAuthKeyRow(
                m.getStringOrEmpty("keyname"),
                m.getStringOrEmpty("keytype"),
                m.getStringOrEmpty("keyvalue"),
                m.getIntOrZero("isactive"),
                m.getLocalDateOrNow("createddate")
        );
    }

}
