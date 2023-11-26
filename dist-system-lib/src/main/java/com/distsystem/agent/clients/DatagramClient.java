package com.distsystem.agent.clients;

import com.distsystem.api.*;
import com.distsystem.api.enums.DistClientType;
import com.distsystem.api.enums.DistMessageType;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.base.AgentClientBase;
import com.distsystem.api.dtos.DistAgentServerRow;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

/** Datagram client with client communications */
public class DatagramClient extends AgentClientBase implements AgentClient {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(DatagramClient.class);

    private DatagramSocket datagramSocket;
    private DistAgentServerRow srv;
    private InetSocketAddress address;

    /** creates new Datagram client  */
    public DatagramClient(Agent parentAgent, DistAgentServerRow srv) {
        super(parentAgent, srv);
        this.srv = srv;
        this.connectedAgentGuid = srv.getAgentGuid();
        initialize();
    }

    /** count objects in this agentable object including this object */
    public long countObjectsAgentable() {
        return 2L;
    }
    /** get type of client - socket, http, datagram, ... */
    public DistClientType getClientType() {
        return DistClientType.datagram;
    }
    /** get unified URL of this client */
    public String getUrl() {
        return srv.getServerUrl();
    }
    /** initialize client - connecting or reconnecting */
    public boolean initialize() {
        try {
            address = new InetSocketAddress(srv.getServerHost(), srv.getServerPort());
            datagramSocket = new DatagramSocket();
            log.info("Created new DATAGRAM client for server: " + srv.simpleInfo());
            AgentWelcomeMessage welcome = new AgentWelcomeMessage(parentAgent.getAgentInfo(), getClientInfo());
            DistMessage welcomeMsg = DistMessage.createMessage(DistMessageType.system, parentAgent.getAgentGuid(), DistServiceType.agent, connectedAgentGuid, DistServiceType.agent, "welcome",  welcome);
            send(welcomeMsg);
            return true;
        } catch (Exception ex) {
            addIssueToAgent("initialize", ex);
            log.warn("Cannot initialize client " + clientGuid + ", agent: " + this.parentAgent.getAgentGuid() + ", Exception at Start: "+ex.getMessage(), ex);
            return false;
        }
    }

    /** read configuration and re-initialize this component */
    public boolean componentReinitialize() {
        // TODO: reinitialize this component - restart datagram server on port
        return true;
    }
    /** send message to this client */
    public boolean send(DistMessage msg) {
        try {
            byte[] sendBuf  = parentAgent.getSerializer().serialize(msg);
            log.info("Writing line to be sent using DATAGRAM client: " + clientGuid + ", SIZE=" + sendBuf.length + ", serializer: " + parentAgent.getSerializer().getClass().getName() + ", message: " + msg.toString());
            DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length, address);
            datagramSocket.send(packet);
            return true;
        } catch (Exception ex) {
            log.warn("Error while sending Datagram packet for client: " + clientGuid + ", reason: " + ex.getMessage(), ex);
            addIssueToAgent("send", ex);
            return false;
        }
    }

    /** close this client */
    protected void onClose() {
        log.info("Closing Datagram client for GUID: " + clientGuid);
        try {
            AgentWelcomeMessage welcome = new AgentWelcomeMessage(parentAgent.getAgentInfo(), getClientInfo());
            DistMessage closeMsg = DistMessage.createMessage(DistMessageType.system, parentAgent.getAgentGuid(), DistServiceType.agent, connectedAgentGuid, DistServiceType.agent, "close",  welcome);
            send(closeMsg);
            datagramSocket.close();
            working = false;
        } catch (Exception ex) {
            log.info(" Error while closing Datagram client connection, reason: "+ex.getMessage());
            addIssueToAgent("onClose", ex);
        }
    }

}