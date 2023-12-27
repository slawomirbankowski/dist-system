package com.distsystem.api;

import com.distsystem.api.dtos.DistAgentRegisterRow;

import java.time.LocalDateTime;
import java.util.List;

/**  */
public class AgentSemaphoreLocal {

    /** unique ID of this agent */
    private String semaphoreName;

    public AgentSemaphoreLocal(String semaphoreName) {
    }

    public String getSemaphoreName() {
        return semaphoreName;
    }
    public void tryLock(boolean locked) {

    }
    public void tryUnlock(boolean locked) {

    }
}
