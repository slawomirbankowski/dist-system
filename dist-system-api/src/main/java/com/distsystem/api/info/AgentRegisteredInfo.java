package com.distsystem.api.info;

import java.time.LocalDateTime;

/** information about Registration class to register Agent */
public class AgentRegisteredInfo {

    private final String agentguid;
    private final String hostname;
    private final String hostip;
    private final int portnumber;
    private final LocalDateTime lastUpdated;
    private final long updatesCount;
    private final LocalDateTime lastPingDate;
    private final LocalDateTime createDate;
    private final int isactive;

    public AgentRegisteredInfo(String agentguid, String hostname, String hostip, int portnumber, LocalDateTime lastUpdated, long updatesCount, LocalDateTime lastPingDate, LocalDateTime createDate, int isactive) {
        this.agentguid = agentguid;
        this.hostname = hostname;
        this.hostip = hostip;
        this.portnumber = portnumber;
        this.lastUpdated = lastUpdated;
        this.updatesCount = updatesCount;
        this.lastPingDate = lastPingDate;
        this.createDate = createDate;
        this.isactive = isactive;
    }
    public String getAgentguid() {
        return agentguid;
    }
    public String getHostname() {
        return hostname;
    }
    public String getHostip() {
        return hostip;
    }
    public int getPortnumber() {
        return portnumber;
    }
    public String getLastUpdated() {
        return lastUpdated.toString();
    }
    public long getUpdatesCount() {
        return updatesCount;
    }
    public String getLastPingDate() {
        return lastPingDate.toString();
    }
    public String getCreateDate() {
        return createDate.toString();
    }
    public int getIsactive() {
        return isactive;
    }
}
