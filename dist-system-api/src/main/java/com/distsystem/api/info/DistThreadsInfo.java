package com.distsystem.api.info;

import java.util.List;

/** object to keep information about Threads managed by Dist Agent system */
public class DistThreadsInfo {
    private final int threadsCount;
    private final List<AgentThreadInfo> threads;

    public DistThreadsInfo(int threadsCount, List<AgentThreadInfo> threads) {
        this.threadsCount = threadsCount;
        this.threads = threads;
    }
    public int getThreadsCount() {
        return threadsCount;
    }
    public List<AgentThreadInfo> getThreads() {
        return threads;
    }
}
