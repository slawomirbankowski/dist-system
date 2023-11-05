package com.distsystem.api.info;

import java.time.LocalDateTime;

/** Information about Dist Agent managed Thread */
public class AgentThreadInfo {

    /** date and time of creation */
    private LocalDateTime createdDate;
    private String threadGuid;
    private String threadFriendlyName;
    private String threadName;
    private String threadState;
    private int threadPriority;
    private long threadId;
    private boolean isAlive;
    private String threadClassName;

    public AgentThreadInfo(LocalDateTime createdDate, String threadGuid, String threadFriendlyName, String threadName, String threadState, int threadPriority, long threadId, boolean isAlive, String threadClassName) {
        this.createdDate = createdDate;
        this.threadGuid = threadGuid;
        this.threadFriendlyName = threadFriendlyName;
        this.threadName = threadName;
        this.threadState = threadState;
        this.threadPriority = threadPriority;
        this.threadId = threadId;
        this.isAlive = isAlive;
        this.threadClassName = threadClassName;
    }

    public String getCreatedDate() {
        return createdDate.toString();
    }
    public String getThreadGuid() {
        return threadGuid;
    }
    public String getThreadFriendlyName() {
        return threadFriendlyName;
    }
    public String getThreadName() {
        return threadName;
    }
    public String getThreadState() {
        return threadState;
    }
    public int getThreadPriority() {
        return threadPriority;
    }
    public long getThreadId() {
        return threadId;
    }
    public boolean isAlive() {
        return isAlive;
    }
    public String getThreadClassName() {
        return threadClassName;
    }
}
