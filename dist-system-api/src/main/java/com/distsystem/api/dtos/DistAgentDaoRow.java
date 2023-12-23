package com.distsystem.api.dtos;

import com.distsystem.api.BaseRow;
import com.distsystem.api.DaoTable;
import com.distsystem.utils.AdvancedMap;
import com.distsystem.utils.DistUtils;
import com.distsystem.utils.JsonUtils;

import java.time.LocalDateTime;
import java.util.Map;

/**  */
public class DistAgentDaoRow extends BaseRow {

    private final String daoGuid;
    private final String agentGuid;
    private final String daoKey;
    private final String daoType;
    private final String daoUrl;
    private final String structureList;

    private final int isActive;
    private final LocalDateTime createdDate;
    private final LocalDateTime lastUpdatedDate;

    public DistAgentDaoRow(String agentGuid, String daoKey, String daoType, String daoUrl, String structureList, int isActive, LocalDateTime createdDate, LocalDateTime lastUpdatedDate) {
        this.daoGuid = DistUtils.generateCustomGuid("");
        this.agentGuid = agentGuid;
        this.daoKey = daoKey;
        this.daoType = daoType;
        this.daoUrl = daoUrl;
        this.structureList = structureList;
        this.isActive = isActive;
        this.createdDate = createdDate;
        this.lastUpdatedDate = lastUpdatedDate;
    }
    public DistAgentDaoRow(String agentGuid, String daoKey, String daoType, String daoUrl, String structureList) {
        this.daoGuid = DistUtils.generateCustomGuid("");
        this.agentGuid = agentGuid;
        this.daoKey = daoKey;
        this.daoType = daoType;
        this.daoUrl = daoUrl;
        this.structureList = structureList;
        this.createdDate = LocalDateTime.now();
        this.isActive = 1;
        this.lastUpdatedDate = createdDate;
    }
    public String getDaoGuid() {
        return daoGuid;
    }
    public String getAgentGuid() {
        return agentGuid;
    }

    public String getDaoKey() {
        return daoKey;
    }

    public String getDaoType() {
        return daoType;
    }

    public String getDaoUrl() {
        return daoUrl;
    }

    public String getStructureList() {
        return structureList;
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
        return new Object[] { agentGuid, daoKey, daoType, daoUrl, structureList, createdDate, isActive, lastUpdatedDate };
    }
    public Map<String, String> toMap() {
        return Map.of("type", "DistAgentDaoRow",
                "agentGuid", agentGuid,
                "daoKey", daoKey,
                "daoType", daoType,
                "daoUrl", daoUrl,
                "structureList", structureList,
                "createdDate", createdDate.toString(),
                "lastUpdatedDate", lastUpdatedDate.toString());
    }

    /** get name of key attribute */
    public static String getKeyAttributeName() {
        return "daoGuid";
    }
    public static DistAgentDaoRow fromMap(Map<String, Object> map) {
        AdvancedMap m = new AdvancedMap(map, true);
        log.info("Creating row from map: " + JsonUtils.serialize(map));
        return new DistAgentDaoRow(
                m.getStringOrEmpty("agentguid"),
                m.getStringOrEmpty("daokey"),
                m.getStringOrEmpty("daotype"),
                m.getStringOrEmpty("daourl"),
                m.getStringOrEmpty("structurelist"),
                m.getInt("isactive", 1),
                m.getLocalDateOrNow("createddate"),
                m.getLocalDateOrNow("lastupdateddate")
        );
    }
}
