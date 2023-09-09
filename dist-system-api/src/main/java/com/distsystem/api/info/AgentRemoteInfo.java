package com.distsystem.api.info;

import com.distsystem.base.dtos.DistAgentRegisterRow;

import java.time.LocalDateTime;
import java.util.Set;

/** Information class about remote agent maintained by other system */
public class AgentRemoteInfo {

    private LocalDateTime createDate;
    private LocalDateTime lastUpdated;
    private long updatesCount;
    private Set<String> registrationKeys;
    private DistAgentRegisterRow simplified;

    public AgentRemoteInfo() {

    }

    public AgentRemoteInfo(LocalDateTime createDate, LocalDateTime lastUpdated, long updatesCount, Set<String> registrationKeys, DistAgentRegisterRow simplified) {
        this.createDate = createDate;
        this.lastUpdated = lastUpdated;
        this.updatesCount = updatesCount;
        this.registrationKeys = registrationKeys;
        this.simplified = simplified;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public long getUpdatesCount() {
        return updatesCount;
    }

    public Set<String> getRegistrationKeys() {
        return registrationKeys;
    }

    public DistAgentRegisterRow getSimplified() {
        return simplified;
    }
}
