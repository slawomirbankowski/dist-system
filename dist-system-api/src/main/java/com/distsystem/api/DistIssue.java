package com.distsystem.api;

import com.distsystem.base.dtos.DistAgentIssueRow;
import com.distsystem.interfaces.AgentComponent;
import com.distsystem.utils.DistUtils;

import java.time.LocalDateTime;

/** internal issue in Dist environment - this is full version of object */
public class DistIssue {

    /** date ant time of creation for this server */
    private final LocalDateTime createDate = LocalDateTime.now();
    /** globally unique ID of this issue */
    private final String guid = DistUtils.generateCustomTimeGuid("ISSUE_");
    /** parent component that raised this issue*/
    private final AgentComponent parent;
    /** method of this issue */
    private final String methodName;
    /** Exception raised */
    private final Exception ex;
    /** table of additional parameters for issue */
    private final Object[] params;

    public DistIssue(AgentComponent parent, String methodName, Exception ex, Object... params) {
        this.parent = parent;
        this.methodName = methodName;
        this.ex = ex;
        this.params = params;
    }
    public DistIssue(AgentComponent parent, String methodName, Exception ex) {
        this.parent = parent;
        this.methodName = methodName;
        this.ex = ex;
        this.params = new Object[0];
    }
    public LocalDateTime getCreateDate() {
        return createDate;
    }
    public String getGuid() {
        return guid;
    }
    public Object getParent() {
        return parent;
    }
    public String getMethodName() {
        return methodName;
    }
    public Exception getEx() {
        return ex;
    }
    public String getExceptionMessage() {
        return ""+ex.getMessage();
    }
    public String getExceptionSerialized() {
        return "";
    }
    public Object[] getParams() {
        return params;
    }

    /** convert this rich object to serializable row to be sent or stored */
    public DistAgentIssueRow toRow() {
        return new DistAgentIssueRow(createDate, guid, parent.getAgent().getAgentGuid(), methodName, ex.getClass().getName(), ex.getMessage(), "", params);
    }
    public static String ISSUE_INTERNAL_EXCEPTION = "ISSUE_INTERNAL_EXCEPTION";
    public static String ISSUE_ALREADY_CLOSED = "ISSUE_ALREADY_CLOSED";

    @Override
    public java.lang.String toString() {
        return "ISSUE,guid=" + guid + ",parentGuid=" + parent.getGuid() + ",methodName=" + methodName;
    }
}
