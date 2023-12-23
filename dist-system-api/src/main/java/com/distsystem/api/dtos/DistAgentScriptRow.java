package com.distsystem.api.dtos;

import com.distsystem.api.BaseRow;
import com.distsystem.api.DaoTable;
import com.distsystem.utils.JsonUtils;

import java.time.LocalDateTime;
import java.util.Map;

/**  */
public class DistAgentScriptRow extends BaseRow {

    private final String scriptName;
    private final String scriptType;
    private final String scriptContent;
    private final String scriptParameters;
    private final int isActive;
    private final LocalDateTime createdDate;
    private final LocalDateTime lastUpdatedDate;

    public DistAgentScriptRow(String scriptName, String scriptType, String scriptContent, Map<String, String> scriptParameters, int isActive, LocalDateTime createdDate, LocalDateTime lastUpdatedDate) {
        this.scriptName = scriptName;
        this.scriptType = scriptType;
        this.scriptContent = scriptContent;
        this.scriptParameters = JsonUtils.serialize(scriptParameters);
        this.isActive = isActive;
        this.createdDate = createdDate;
        this.lastUpdatedDate = lastUpdatedDate;
    }
    public DistAgentScriptRow(String scriptName, String scriptType, String scriptContent, Map<String, String> scriptParameters) {
        this.scriptName = scriptName;
        this.scriptType = scriptType;
        this.scriptContent = scriptContent;
        this.scriptParameters = JsonUtils.serialize(scriptParameters);
        this.isActive = 1;
        this.createdDate = LocalDateTime.now();
        this.lastUpdatedDate = createdDate;
    }

    public String getScriptName() {
        return scriptName;
    }

    public String getScriptType() {
        return scriptType;
    }

    public String getScriptContent() {
        return scriptContent;
    }

    public Map<String, String> getScriptParameters() {
        return JsonUtils.deserializeToMap(scriptParameters);
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
        return new Object[] { scriptName, scriptType, scriptContent, scriptParameters, isActive, createdDate, lastUpdatedDate };
    }
    public Map<String, String> toMap() {
        return Map.of("type", "DistAgentScriptRow",
                "scriptName", scriptName,
                "scriptType", scriptType,
                "scriptContent", scriptContent,
                "scriptParameters", scriptParameters,
                "isActive", ""+isActive,
                "createdDate", createdDate.toString(),
                "lastUpdatedDate", lastUpdatedDate.toString());
    }

    /** get name of key attribute */
    public static String getKeyAttributeName() {
        return "scriptName";
    }
}
