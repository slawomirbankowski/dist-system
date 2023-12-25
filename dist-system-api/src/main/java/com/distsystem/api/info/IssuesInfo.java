package com.distsystem.api.info;

import com.distsystem.api.AgentEvent;
import com.distsystem.api.DistIssue;
import com.distsystem.api.dtos.DistAgentIssueRow;

import java.time.LocalDateTime;
import java.util.List;

/** info about events */
public class IssuesInfo {

    private final long addedIssuesCount;
    private final long issuesCount;
    private final List<DistAgentIssueRow> latestIssues;

    /** events info */
    public IssuesInfo(long addedIssuesCount, long issuesCount,  List<DistAgentIssueRow> latestIssues) {
        this.addedIssuesCount = addedIssuesCount;
        this.issuesCount = issuesCount;
        this.latestIssues = latestIssues;
    }
    public long getAddedIssuesCount() {
        return addedIssuesCount;
    }
    public long getIssuesCount() {
        return issuesCount;
    }
    public List<DistAgentIssueRow> getLatestIssues() {
        return latestIssues;
    }
}
