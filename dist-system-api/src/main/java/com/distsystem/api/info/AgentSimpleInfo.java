package com.distsystem.api.info;

import com.distsystem.api.DistServiceInfo;
import com.distsystem.utils.DistUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

/** information DTO about Agent - current state */
public class AgentSimpleInfo implements Serializable {

    private final String agentGuid;
    private final String environmentType;
    private final String environmentName;
    private final String distName;
    private final String agentName;
    private final LocalDateTime createDate;
    private final boolean closed;
    private final Set<String> tags;
    public AgentSimpleInfo(String agentGuid,
                           String environmentType,
                           String environmentName,
                           String distName,
                           String agentName,
                           LocalDateTime createDate,
                           boolean closed,
                           Set<String> tags) {
        this.agentGuid = agentGuid;
        this.environmentType = environmentType;
        this.environmentName = environmentName;
        this.distName = distName;
        this.agentName = agentName;
        this.createDate = createDate;
        this.closed = closed;
        this.tags = tags;
    }

    /** serialize this agent info */
    public String toString() {
        return "AGENT uid: " + agentGuid + ", created: " + createDate + ", closed: " + closed;
    }

    public String getAgentGuid() {
        return agentGuid;
    }

    public String getEnvironmentType() {
        return environmentType;
    }

    public String getEnvironmentName() {
        return environmentName;
    }

    public String getDistName() {
        return distName;
    }
    public String getAgentName() {
        return agentName;
    }
    public String getAgentHostName() { return DistUtils.getCurrentHostName(); }
    public String getCreateDate() {
        return createDate.toString();
    }
    /** get number of running minutes of this agent */
    public long getRunningMinutes() {
        return createDate.until(LocalDateTime.now(), ChronoUnit.MINUTES);
    }
    public boolean isClosed() {
        return closed;
    }
    public Set<String> getTags() {
        return tags;
    }
}
