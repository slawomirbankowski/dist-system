package com.distsystem.api.dtos;

import com.distsystem.api.BaseRow;
import com.distsystem.api.DaoTable;

import java.time.LocalDateTime;
import java.util.Map;

/**  */
@DaoTable(tableName="DistAgentSetting", keyName="settingName", keyIsUnique=true)
public class DistAgentSettingRow extends BaseRow {

    private final String settingName;
    private final String settingCategory;
    private final String settingType;
    private final String settingValue;
    private final LocalDateTime createdDate;
    private final int isActive;
    private final LocalDateTime lastUpdatedDate;

    public DistAgentSettingRow(String settingName, String settingCategory, String settingType, String settingValue, LocalDateTime createdDate, int isActive, LocalDateTime lastUpdatedDate) {
        this.settingName = settingName;
        this.settingCategory = settingCategory;
        this.settingType = settingType;
        this.settingValue = settingValue;
        this.createdDate = createdDate;
        this.isActive = isActive;
        this.lastUpdatedDate = lastUpdatedDate;
    }
    public DistAgentSettingRow(String settingName, String settingCategory, String settingType, String settingValue) {
        this.settingName = settingName;
        this.settingCategory = settingCategory;
        this.settingType = settingType;
        this.settingValue = settingValue;
        this.createdDate = LocalDateTime.now();
        this.isActive = 1;
        this.lastUpdatedDate = createdDate;
    }

    public String getSettingName() {
        return settingName;
    }

    public String getSettingCategory() {
        return settingCategory;
    }

    public String getSettingType() {
        return settingType;
    }

    public String getSettingValue() {
        return settingValue;
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
        return new Object[] { settingName, settingCategory, settingType, settingValue, createdDate, isActive, lastUpdatedDate };
    }
    public Map<String, String> toMap() {
        return Map.of("type", "DistAgentSettingRow",
                "settingName", settingName,
                "settingCategory", settingCategory,
                "settingType", settingType,
                "settingValue", settingValue,
                "createdDate", createdDate.toString(),
                "lastUpdatedDate", lastUpdatedDate.toString());
    }

    /** get name of key attribute */
    public static String getKeyAttributeName() {
        return "settingName";
    }
}
