package com.distsystem.api;

import java.time.LocalDateTime;

/** simplified agent information about GUID, host, port and create date
 * This class is returned by Registration services  */
public class AgentSimplified {

    private String agentGuid;
    private String hostName;
    private String hostIp;
    private int port;
    private LocalDateTime createDate;

    public AgentSimplified() {
        this.agentGuid = null;
        this.hostName = null;
        this.hostIp = null;
        this.port = -1;
        this.createDate = null;
    }
    public AgentSimplified(String agentGuid, String hostName, String hostIp, int port, LocalDateTime createDate) {
        this.agentGuid = agentGuid;
        this.hostName = hostName;
        this.hostIp = hostIp;
        this.port = port;
        this.createDate = createDate;
    }

    public String getAgentGuid() {
        return agentGuid;
    }

    public String getHostName() {
        return hostName;
    }

    public String getHostIp() {
        return hostIp;
    }

    public int getPort() {
        return port;
    }

    public String getCreateDate() {
        return createDate.toString();
    }

}
