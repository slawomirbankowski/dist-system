package com.distsystem.api.info;

public class DistThreadInfo {

    private final String threadName;
    private final String threadPart;
    private final String threadGroup;
    private final long threadId;
    private final String threadState;
    private final int threadPriority;
    private final boolean daemon;
    private final boolean alive;
    private final boolean interrupted;
    private final String stackTrace;

    public DistThreadInfo(String threadName, String threadPart, String threadGroup, long threadId, String threadState, int threadPriority, boolean daemon, boolean alive, boolean interrupted, String stackTrace) {
        this.threadName = threadName;
        this.threadPart = threadPart;
        this.threadGroup = threadGroup;
        this.threadId = threadId;
        this.threadState = threadState;
        this.threadPriority = threadPriority;
        this.daemon = daemon;
        this.alive = alive;
        this.interrupted = interrupted;
        this.stackTrace = stackTrace;
    }

    public String getThreadName() {
        return threadName;
    }

    public String getThreadPart() {
        return threadPart;
    }

    public String getThreadGroup() {
        return threadGroup;
    }

    public long getThreadId() {
        return threadId;
    }

    public String getThreadState() {
        return threadState;
    }

    public int getThreadPriority() {
        return threadPriority;
    }

    public boolean isDaemon() {
        return daemon;
    }

    public boolean isAlive() {
        return alive;
    }

    public boolean isInterrupted() {
        return interrupted;
    }

    public String getStackTrace() {
        return stackTrace;
    }

}
