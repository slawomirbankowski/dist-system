package com.distsystem.api.info;

import java.util.List;

/** object to keep information about Threads managed by Dist Agent system */
public class DistThreadsInfo {
    private final int threadsCount;
    private final List<AgentThreadInfo> infos;

    public DistThreadsInfo(int threadsCount, List<AgentThreadInfo> infos) {
        this.threadsCount = threadsCount;
        this.infos = infos;
    }
    public int getThreadsCount() {
        return threadsCount;
    }
    public List<AgentThreadInfo> getInfos() {
        return infos;
    }
}
