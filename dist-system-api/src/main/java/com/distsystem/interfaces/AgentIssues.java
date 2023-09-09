package com.distsystem.interfaces;

import com.distsystem.api.DistIssue;

import java.util.Queue;

/** Interface for Issue manager to have possibilities to add and list issues added by services connected to Agent.
 * Issues are waiting in a queue with limited size, it means that the oldest issues would be removed forever.
 * */
public interface AgentIssues extends IssueHandler {

    /** add issue to agent to be revoked by parent
     * issue could be Exception, Error, problem with connecting to storage,
     * internal error, not consistent state that is unknown and could be used by parent manager */
    void addIssue(DistIssue issue);
    /** get all recent issues */
    Queue<DistIssue> getIssues();
    /** close this manager for issues */
    void close();
}
