package com.distsystem.api.info;


import com.distsystem.api.enums.DistClientType;

/** information class about Server manager in Agent. */
public class AgentServerInfo {
    private final DistClientType serverType;
    private final String serverGuid;
    private final String url;
    private final int port;
    private final boolean closed;
    private final long receivedMessages;

    public AgentServerInfo(DistClientType serverType, String serverGuid, String url, int port, boolean closed, long receivedMessages) {
        this.serverType = serverType;
        this.serverGuid = serverGuid;
        this.url = url;
        this.port = port;
        this.closed = closed;
        this.receivedMessages = receivedMessages;
    }

    public DistClientType getServerType() {
        return serverType;
    }

    public String getServerGuid() {
        return serverGuid;
    }

    public String getUrl() {
        return url;
    }

    public int getPort() {
        return port;
    }

    public boolean isClosed() {
        return closed;
    }

    public long getReceivedMessages() {
        return receivedMessages;
    }

    @Override
    public java.lang.String toString() {
        return "SERVER,type=" + serverType.name() + ",guid=" + serverGuid + "url=" + url;
    }
}
