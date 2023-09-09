package com.distsystem.base.dtos;

import java.time.LocalDateTime;
import java.util.Map;

/** row for issues
 * */
public class DistAgentIssueRow {

    /** date ant time of creation for this server */
    private final LocalDateTime createDate;
    /** globally unique ID of this issue */
    private final String guid;
    private final String agentGuid;
    private final String methodName;
    private final String exceptionClass;
    private final String exceptionMessage;
    private final String exceptionSerialized;
    private final Object[] params;

    public DistAgentIssueRow(LocalDateTime createDate, String guid, String agentGuid, String methodName, String exceptionClass, String exceptionMessage, String exceptionSerialized, Object[] params) {
        this.createDate = createDate;
        this.guid = guid;
        this.agentGuid = agentGuid;
        this.methodName = methodName;
        this.exceptionClass = exceptionClass;
        this.exceptionMessage = exceptionMessage;
        this.exceptionSerialized = exceptionSerialized;
        this.params = params;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public String getGuid() {
        return guid;
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

    public Object[] getParams() {
        return params;
    }
    public Map<String, String> toMap() {
        return Map.of("createDate", createDate.toString(),
                "issueGuid", guid,
                "agentGuid", agentGuid,
                "methodName", methodName,
                "exceptionClass", exceptionClass,
                "exceptionMessage", exceptionMessage,
                "exceptionSerialized", exceptionSerialized,
                "params", "");
    }



}
