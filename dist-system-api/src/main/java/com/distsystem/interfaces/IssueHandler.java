package com.distsystem.interfaces;

/** interface for handling issue */
public interface IssueHandler {
    /** add issue with method and exception - issue can be sent to logger or get by parent applications to check what is going on */
    void addIssue(String methodName, Exception ex);
}
