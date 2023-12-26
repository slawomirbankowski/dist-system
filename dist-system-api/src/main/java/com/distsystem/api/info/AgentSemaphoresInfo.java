package com.distsystem.api.info;

import java.util.List;

/**
 * */
public class AgentSemaphoresInfo {

    private List<AgentSemaphoreManagerInfo> semaphoreManagers;

    public AgentSemaphoresInfo(List<AgentSemaphoreManagerInfo> semaphoreManagers) {
        this.semaphoreManagers = semaphoreManagers;
    }

    public List<AgentSemaphoreManagerInfo> getSemaphoreManagers() {
        return semaphoreManagers;
    }
}
