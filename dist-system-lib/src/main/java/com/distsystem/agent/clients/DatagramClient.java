package com.distsystem.agent.clients;

import com.distsystem.api.*;
import com.distsystem.api.enums.DistClientType;
import com.distsystem.api.enums.DistMessageType;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.base.AgentClientBase;
import com.distsystem.base.dtos.DistAgentServerRow;
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
        this.connectedAgentGuid = srv.agentguid;
        initialize();
    }
    /** get type of client - socket, http, datagram, ... */
    public DistClientType getClientType() {
        return DistClientType.datagram;
    }
    /** get unified URL of this client */
    public String getUrl() {
        return srv.serverurl;
    }
    /** initialize client - connecting or reconnecting */
    public boolean initialize() {
        try {
            address = new InetSocketAddress(srv.serverhost, srv.serverport);
            datagramSocket = new DatagramSocket();
            log.info("Created new DATAGRAM client for server: " + srv.servertype + ", url: " + srv.serverurl + ", host: " + srv.serverhost + ", port: " + srv.serverport);
            AgentWelcomeMessage welcome = new AgentWelcomeMessage(parentAgent.getAgentInfo(), getClientInfo());
            DistMessage welcomeMsg = DistMessage.createMessage(DistMessageType.system, parentAgent.getAgentGuid(), DistServiceType.agent, connectedAgentGuid, DistServiceType.agent, "welcome",  welcome);
            send(welcomeMsg);
            return true;
        } catch (Exception ex) {
            log.warn("Cannot initialize client " + clientGuid + ", agent: " + this.parentAgent.getAgentGuid() + ", Exception at Start: "+ex.getMessage(), ex);
            return false;
        }
    }

    /** read configuration and re-initialize this component */
    public boolean componentReinitialize() {
        // TODO: reinitialize this component
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
            parentAgent.getAgentIssues().addIssue("DatagramClient.send", ex);
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

        }
    }

}