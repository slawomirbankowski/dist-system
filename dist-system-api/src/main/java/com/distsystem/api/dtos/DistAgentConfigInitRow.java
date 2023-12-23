package com.distsystem.api.dtos;

import com.distsystem.api.BaseRow;
import com.distsystem.api.DaoTable;
import com.distsystem.utils.AdvancedMap;
import com.distsystem.utils.DistUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 *
 * */
public class DistAgentConfigInitRow extends BaseRow {

    private final String configGuid;
    private final String distName;
    private final String configName;
    private final String configValue;
    private final int isActive;
    private final LocalDateTime createdDate;
    private final LocalDateTime lastUpdatedDate;

    public DistAgentConfigInitRow(String distname, String configname, String configvalue) {
        this.configGuid = DistUtils.generateCustomGuid("config");
        this.distName = distname;
        this.configName = configname;
        this.configValue = configvalue;
        this.isActive = 1;
        this.createdDate = LocalDateTime.now();
        this.lastUpdatedDate = createdDate;
    }

    public String getDistName() {
        return distName;
    }

    public String getConfigName() {
        return configName;
    }

    public String getConfigValue() {
        return configValue;
    }

    public String getConfigGuid() {
        return configGuid;
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
        return new Object[] { distName, configName, configValue };
    }

    public Map<String, String> toMap() {
        return Map.of("type", "DistAgentAuthDomainRow",
                "distName", distName,
                "configName", configName,
                "configValue", configValue);
    }

    /** get name of key attribute */
    public static String getKeyAttributeName() {
        return "configGuid";
    }
    public static DistAgentConfigInitRow fromMap(Map<String, Object> map) {
        AdvancedMap m = new AdvancedMap(map, true);
        return new DistAgentConfigInitRow(
                m.getStringOrEmpty("distname"),
                m.getStringOrEmpty("configname"),
                m.getStringOrEmpty("configvalue")
        );
    }

}
