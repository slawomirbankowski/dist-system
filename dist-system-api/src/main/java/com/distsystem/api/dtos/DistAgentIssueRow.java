package com.distsystem.api.dtos;

import com.distsystem.api.BaseRow;
import com.distsystem.api.DaoTable;
import com.distsystem.utils.JsonUtils;

import java.time.LocalDateTime;
import java.util.Map;

/** row for issues
 * */
@DaoTable(tableName="distagentissue", keyName="guid", keyIsUnique=true)
public class DistAgentIssueRow extends BaseRow {


    /** globally unique ID of this issue */
    private final String issueGuid;
    private final String agentGuid;
    private final String methodName;
    private final String exceptionClass;
    private final String exceptionMessage;
    private final String exceptionSerialized;
    private final String params;

    protected int isActive;
    protected LocalDateTime createdDate;
    protected LocalDateTime lastUpdatedDate;

    public DistAgentIssueRow(String guid, String agentGuid, String methodName, String exceptionClass, String exceptionMessage, String exceptionSerialized, String params, int isActive, LocalDateTime createdDate, LocalDateTime lastUpdatedDate) {
        this.issueGuid = guid;
        this.agentGuid = agentGuid;
        this.methodName = methodName;
        this.exceptionClass = exceptionClass;
        this.exceptionMessage = exceptionMessage;
        this.exceptionSerialized = exceptionSerialized;
        this.params = params;
        this.isActive = isActive;
        this.createdDate = createdDate;
        this.lastUpdatedDate = lastUpdatedDate;

    }
    public DistAgentIssueRow(String guid, String agentGuid, String methodName, String exceptionClass, String exceptionMessage, String exceptionSerialized, String params) {
        this.issueGuid = guid;
        this.agentGuid = agentGuid;
        this.methodName = methodName;
        this.exceptionClass = exceptionClass;
        this.exceptionMessage = exceptionMessage;
        this.exceptionSerialized = exceptionSerialized;
        this.params = params;
        this.isActive = 1;
        this.createdDate = LocalDateTime.now();
        this.lastUpdatedDate = createdDate;

    }


    public String getGuid() {
        return issueGuid;
    }

    public String getAgentGuid() {
        return agentGuid;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getExceptionClass() {
        return exceptionClass;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public String getExceptionSerialized() {
        return exceptionSerialized;
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
        return new Object[] { issueGuid, agentGuid, methodName, exceptionClass, exceptionMessage,
                exceptionSerialized, JsonUtils.serialize(params),
                isActive, createdDate, lastUpdatedDate
        };
    }


    /** get name of key attribute */
    public static String getKeyAttributeName() {
        return "issueGuid";
    }
    public String getParams() {
        return params;
    }
    public Map<String, String> toMap() {
        return Map.of("type", "DistAgentIssueRow",
                "issueGuid", issueGuid,
                "agentGuid", agentGuid,
                "methodName", methodName,
                "exceptionClass", exceptionClass,
                "exceptionMessage", exceptionMessage,
                "exceptionSerialized", exceptionSerialized,
                "params", "");
    }



}
