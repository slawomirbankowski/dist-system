package com.distsystem.api.info;

import java.time.LocalDateTime;

/** information about Registration class to register Agent */
public class AgentRegisteredInfo {

    private String agentguid;
    private String hostname;
    private String hostip;
    private int portnumber;
    private LocalDateTime lastUpdated;
    private long updatesCount;
    private LocalDateTime lastPingDate;
    private LocalDateTime createDate;
    private int isactive;

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
