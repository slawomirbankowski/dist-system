package com.distsystem.api.dtos;

import com.distsystem.api.BaseRow;
import com.distsystem.utils.AdvancedMap;
import com.distsystem.utils.DistUtils;

import java.time.LocalDateTime;
import java.util.Map;

/** row for distagentconfig table
 * create table distagentconfig(agentguid varchar(300), configname varchar(300), configvalue varchar(300), createddate timestamp, lastupdateddate timestamp)
 *
 * */
public class DistAgentConfigRow extends BaseRow {

    private final String configGuid;
    private final String agentGuid;
    private final String configName;
    private final String configValue;
    private final int isActive;
    private final LocalDateTime createdDate;
    private final LocalDateTime lastUpdatedDate;

    public DistAgentConfigRow(String configGuid, String agentGuid, String configName, String configValue, int isActive, LocalDateTime createdDate, LocalDateTime lastUpdatedDate) {
        this.configGuid = configGuid;
        this.agentGuid = agentGuid;
        this.configName = configName;
        this.configValue = configValue;
        this.isActive = isActive;
        this.createdDate = createdDate;
        this.lastUpdatedDate = lastUpdatedDate;
    }

    public DistAgentConfigRow(String agentGuid, String configName, String configValue) {
        this.configGuid = DistUtils.generateCustomGuid("config");
        this.agentGuid = agentGuid;
        this.configName = configName;
        this.configValue = configValue;
        this.isActive = 1;
        this.createdDate = LocalDateTime.now();
        this.lastUpdatedDate = createdDate;
    }
    public String getAgentGuid() {
        return agentGuid;
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
        return new Object[] { configGuid, agentGuid, configName, configValue, isActive, createdDate, lastUpdatedDate };
    }

    /** get name of key attribute */
    public static String getKeyAttributeName() {
        return "configGuid";
    }
    public Map<String, String> toMap() {
        return Map.of("type", "DistAgentConfigRow",
                "agentGuid", agentGuid,
                "configName", configName,
                "configValue", configValue,
                "configValue", lastUpdatedDate.toString(),
                "createdDate", createdDate.toString());
    }
    public static DistAgentConfigRow fromMap(Map<String, Object> map) {
        AdvancedMap m = new AdvancedMap(map, true);
        return new DistAgentConfigRow(
                m.getStringOrEmpty("configGuid"),
                m.getStringOrEmpty("agentguid"),
                m.getStringOrEmpty("configname"),
                m.getStringOrEmpty("configvalue"),
                m.getIntOrZero("isActive"),
                m.getLocalDateOrNow("createddate"),
                m.getLocalDateOrNow("lastupdateddate")
        );
    }
}
