package com.distsystem.base;

import com.distsystem.api.enums.DistComponentType;
import com.distsystem.api.info.ClientInfo;
import com.distsystem.api.dtos.DistAgentServerRow;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentClient;
import com.distsystem.interfaces.AgentComponent;
import com.distsystem.utils.DistUtils;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/** base class for any client for connectors to other agents
 * This could be HTTP client, Datagram client, Socket client, ...
 * */
public abstract class AgentClientBase extends AgentableBase implements AgentClient, AgentComponent {

    /** connected Agent GUID or empty if there is not just one agent on the other side */
    protected String connectedAgentGuid = "";
    /** unique ID of this client */
    protected final String clientGuid = DistUtils.generateClientGuid(this.getClass().getSimpleName());
    /** true is this client is still working */
    protected boolean working = true;
    /** tags for this client */
    protected Set<String> tags = Set.of();
    /** sequence for number of received messages */
    protected AtomicLong receivedMessages = new AtomicLong();
    /** sequence for number of received messages */
    protected AtomicLong sentMessages = new AtomicLong();
    /** server row */
    protected DistAgentServerRow serverRow;

    /** creates new client */
    public AgentClientBase(Agent parentAgent, DistAgentServerRow srv) {
        super(parentAgent);
        parentAgent.addComponent(this);
        this.serverRow = srv;
    }

    /** get GUID for this client */
    public String getClientGuid() {
        return clientGuid;
    }
    public String getGuid() {
        return clientGuid;
    }
    /** get type of this component */
    public DistComponentType getComponentType() {
        return DistComponentType.client;
    }
    /** get information about this client */
    public ClientInfo getClientInfo() {
        // DistClientType clientType, String clientClassName, String url, boolean working, String clientGuid, Set<String> tags, long receivedMessages, long sentMessages
        return new ClientInfo(getClientType(), getClass().getName(), getUrl(), working, getClientGuid(), tags, receivedMessages.get(), sentMessages.get());
    }

    /** true if client is still working */
    public boolean isWorking() {
        return working;
    }
    /** check if this client has given tag */
    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }
    /** check if this client has any of tags given */
    public boolean hasTags(String[] tgs) {
        return Arrays.stream(tgs).anyMatch(tag -> tags.contains(tag));
    }

}
