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

    private String agentGuid;
    private String distName;
    private String agentName;
    private LocalDateTime createDate;
    private boolean closed;
    private Set<String> tags;
    private List<String> components;
    private AgentConfigReaderInfo configReader;
    private AgentMessageProcessorInfo messageProcessor;
    private AgentApisInfo apis;
    List<DistServiceInfo> services;
    private AgentConnectorsInfo connectors;
    private AgentRegistrationsInfo registrations;
    private AgentTimerInfo timers;
    private DistThreadsInfo threads;
    private AgentDaosInfo daos;
    private int eventsCount;
    private int issuesCount;

    public AgentInfo(String agentGuid,
                     String distName,
                     String agentName,
                     LocalDateTime createDate,
                     boolean closed,
                     Set<String> tags,
                     List<String> components,
                     AgentConfigReaderInfo configReader,
                     AgentMessageProcessorInfo messageProcessor,
                     AgentApisInfo apis,
                     AgentConnectorsInfo connectors,
                     List<DistServiceInfo> services,
                     AgentRegistrationsInfo registrations,
                     AgentTimerInfo timers,
                     DistThreadsInfo threads,
                     AgentDaosInfo daos,
                     int eventsCount, int issuesCount) {
        this.agentGuid = agentGuid;
        this.distName = distName;
        this.agentName = agentName;
        this.createDate = createDate;
        this.closed = closed;
        this.messageProcessor = messageProcessor;
        this.tags = tags;
        this.components = components;
        this.configReader = configReader;
        this.apis = apis;
        this.connectors = connectors;
        this.services = services;
        this.registrations = registrations;
        this.timers = timers;
        this.threads = threads;
        this.daos = daos;
        this.eventsCount = eventsCount;
        this.issuesCount = issuesCount;
    }

    /** serialize this agent info */
    public String toString() {
        return "AGENT uid: " + agentGuid + ", created: " + createDate + ", closed: " + closed
                + ", registrationsCount: " + registrations.getRegistrations().size() + ", agents: " + registrations.getAgents().size()
                + ", timerTasksCount: " + timers.getTasks().size() + ", threadsCount: " + threads.getThreadsCount()
                + ", eventsCount: " + eventsCount + ", issuesCount: " + issuesCount;
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
    public AgentApisInfo getApis() {
        return apis;
    }
    public List<DistServiceInfo> getServices() {
        return services;
    }
    public AgentConnectorsInfo getConnectors() {
        return connectors;
    }
    public AgentRegistrationsInfo getRegistrations() {
        return registrations;
    }
    public AgentTimerInfo getTimers() {
        return timers;
    }
    public DistThreadsInfo getThreads() {
        return threads;
    }
    public AgentDaosInfo getDaos() {
        return daos;
    }
    public int getEventsCount() {
        return eventsCount;
    }

    public int getIssuesCount() {
        return issuesCount;
    }
}
