package com.distsystem.agent.servers;

import com.distsystem.api.ServiceObjectParams;
import com.distsystem.api.enums.DistClientType;
import com.distsystem.api.DistConfig;
import com.distsystem.api.DistMessage;
import com.distsystem.base.ServerBase;
import com.distsystem.interfaces.AgentServer;
import com.distsystem.utils.DistUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.*;

/**
 * Server for communication between Agents based on UDP packets - datagrams. */
public class AgentDatagramServer extends ServerBase implements AgentServer, Runnable {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(AgentDatagramServer.class);

    /** Datagram server - UDP to receive packets/ datagram */
    private DatagramSocket datagramSocket;
    private int workingPort;
    private String url;
    private Thread mainThread;

    public AgentDatagramServer(ServiceObjectParams params) {
        super(params);
        initialize();
    }

    /** count objects in this agentable object including this object */
    public long countObjectsAgentable() {
        return 4L;
    }
    /** initialize this server */
    public void initialize() {
        try {
            workingPort = getConfigPropertyAsInt(DistConfig.PORT, DistConfig.AGENT_SERVER_DATAGRAM_PORT_DEFAULT_VALUE);
            int soTimeout = getConfigPropertyAsInt(DistConfig.TIMEOUT, 1000); //  parentAgent.getConfig().getPropertyAsInt(DistConfig.AGENT_CONNECTORS_SERVER_DATAGRAM_TIMEOUT,1000);
            datagramSocket = new DatagramSocket(workingPort);
            datagramSocket.setSoTimeout(soTimeout);
            url = "udp://" + DistUtils.getCurrentHostName() + ":" + workingPort + "/";
            mainThread = new Thread(this);
            mainThread.setDaemon(true);
            mainThread.start();
            parentAgent.getThreads().registerThread(this, mainThread, "datagram-server-" + workingPort);
            log.info("Started new DATAGRAM server at port:" + workingPort + ", timeout: " + soTimeout + ", agent: " + parentAgent.getAgentGuid());
        } catch (Exception ex) {
            log.info("Cannot start DATAGRAM server, reason: " + ex.getMessage());
            parentAgent.getIssues().addIssue("AgentDatagramServer.initialize", ex);
        }
    }
    /** read configuration and re-initialize this component */
    public boolean componentReinitialize() {
        // TODO: implement reinitialization
        return true;
    }
    /** get type of clients to be connected to this server */
    public DistClientType getClientType() {
        return DistClientType.datagram;
    }
    /** get port of this server */
    public int getPort() {
        return workingPort;
    }
    /** get URL of this server */
    public String getUrl() {
        return url;
    }

    /** run in separated thread to get datagram packets */
    public void run() {
        log.info("......... Accepting UDP connections on port " + workingPort);
        while (!closed) {
            try {
                byte[] receiveBuff = new byte[4096];
                DatagramPacket packet = new DatagramPacket(receiveBuff, receiveBuff.length);
                datagramSocket.receive(packet);
                byte[] received = packet.getData();
                if (received != null && received.length > 0) {
                    DistMessage receivedMsg = (DistMessage)parentAgent.getSerializer().deserialize(DistMessage.class.getName(), received);
                    log.info("......... Got message from from UDP:  " + receivedMsg.toString());
                }
            } catch (SocketTimeoutException ex) {
            } catch (IOException ex) {
                log.error("!!!!! IOException on Datagram server working on port " + workingPort + ", reason: " + ex.getMessage(), ex);
            } catch (Exception ex) {
                log.error("!!!!! Unknown exception on Datagram server working on port " + workingPort + ", reason: " + ex.getMessage(), ex);
                parentAgent.getIssues().addIssue("AgentDatagramServer.run", ex);
            }
            DistUtils.sleep(2000);
        }
    }

    /** update configuration of this Service */
    public void updateConfig(DistConfig newCfg) {
        // TODO: update configuration of this service
    }

    protected void onClose() {
        try {
            log.info("Try to close DATAGRAM server for Agent: " + parentAgent.getAgentGuid());
            datagramSocket.close();
            mainThread.join(2000);
        } catch (Exception ex) {
            log.warn("Exception while closing Datagram server for agent: " + parentAgent.getAgentGuid() +", reason: " + ex.getMessage());
            parentAgent.getIssues().addIssue("AgentDatagramServer.close", ex);
        }
    }


}
