package com.distsystem.api.info;

import com.distsystem.api.DistServiceInfo;
import com.distsystem.utils.DistUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

/** information DTO about Agent - current state */
public class AgentInfo implements Serializable {

    private final String agentGuid;
    private final String distName;
    private final String agentName;
    private final LocalDateTime createDate;
    private final boolean closed;
    private final Set<String> tags;
    private final List<String> components;
    private final AgentConfigReaderInfo configReader;
    private final AgentMessageProcessorInfo messageProcessor;
    List<DistServiceInfo> services;
    private final List<DistConfigGroupInfo> configGroupInfos;

    public AgentInfo(String agentGuid,
                     String distName,
                     String agentName,
                     LocalDateTime createDate,
                     boolean closed,
                     Set<String> tags,
                     List<String> components,
                     AgentConfigReaderInfo configReader,
                     AgentMessageProcessorInfo messageProcessor,
                     List<DistServiceInfo> services,
                     List<DistConfigGroupInfo> configGroupInfos) {
        this.agentGuid = agentGuid;
        this.distName = distName;
        this.agentName = agentName;
        this.createDate = createDate;
        this.closed = closed;
        this.messageProcessor = messageProcessor;
        this.tags = tags;
        this.components = components;
        this.configReader = configReader;
        this.services = services;
        this.configGroupInfos = configGroupInfos;
    }

    /** serialize this agent info */
    public String toString() {
        return "AGENT uid: " + agentGuid + ", created: " + createDate + ", closed: " + closed;
    }

    public String getAgentGuid() {
        return agentGuid;
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
    public List<String> getComponents() {
        return components;
    }
    public AgentConfigReaderInfo getConfigReader() {
        return configReader;
    }
    public AgentMessageProcessorInfo getMessageProcessor() {
        return messageProcessor;
    }
    public List<DistServiceInfo> getServices() {
        return services;
    }

}
