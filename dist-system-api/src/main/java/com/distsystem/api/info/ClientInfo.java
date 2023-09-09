package com.distsystem.api.info;

import com.distsystem.api.enums.DistClientType;

import java.io.Serializable;
import java.util.Set;

/** information of client connecting to another Agent */
public class ClientInfo implements Serializable {

    /** type of client */
    private DistClientType clientType;
    /** class name of client */
    private String clientClassName;
    /** URL of client */
    private String url;
    /** true is this client is still working */
    private boolean working;
    /** unique ID of this client */
    private String clientGuid;
    /** tags for this client */
    private Set<String> tags;
    /** number of received messages */
    private long receivedMessages;
    /** number of received messages */
    private long sentMessages;

    public ClientInfo(DistClientType clientType, String clientClassName, String url, boolean working, String clientGuid, Set<String> tags, long receivedMessages, long sentMessages) {
        this.clientType = clientType;
        this.clientClassName = clientClassName;
        this.url = url;
        this.working = working;
        this.clientGuid = clientGuid;
        this.tags = tags;
        this.receivedMessages = receivedMessages;
        this.sentMessages = sentMessages;
    }

    public DistClientType getClientType() {
        return clientType;
    }

    public String getClientClassName() {
        return clientClassName;
    }

    public String getUrl() {
        return url;
    }

    public boolean isWorking() {
        return working;
    }

    public String getClientGuid() {
        return clientGuid;
    }

    public Set<String> getTags() {
        return tags;
    }

    public long getReceivedMessages() {
        return receivedMessages;
    }

    public long getSentMessages() {
        return sentMessages;
    }

    @Override
    public java.lang.String toString() {
        return "CLIENT,clientType=" + clientType.name() + ",clientClassName=" + clientClassName + "url=" + url;
    }
}
