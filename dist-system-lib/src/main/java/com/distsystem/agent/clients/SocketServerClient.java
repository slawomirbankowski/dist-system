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

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

/** socket client with client communications */
public class SocketServerClient extends AgentClientBase implements AgentClient, Runnable {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(SocketServerClient.class);
    /** socket to connect to SockerServer */
    protected Socket socket;
    /** if this client is directly connected with local server */
    protected boolean isServer;
    /** name of connected host */
    protected String host = "";
    /** port number */
    protected int port = 0;
    protected BufferedReader inSocket;
    protected PrintWriter outSocket;
    protected Thread receivingThread;
    protected int sleepThreadTime = 1000; // TODO: change this to be configured

    /** creates new socket client */
    public SocketServerClient(Agent parentAgent, Socket socket) {
        super(parentAgent, new DistAgentServerRow(""));
        log.info("Open new socket client on agent: " + parentAgent.getAgentGuid() + ", local host: " + socket.getLocalAddress().getHostName() + ":" + socket.getLocalPort() + ", remote: " + socket.getRemoteSocketAddress().toString() + ", uid: " + clientGuid);
        this.socket = socket;
        connectedAgentGuid = "UNKNOWN";
        isServer = true;
        initialize();
    }
    /** creates new socket  */
    public SocketServerClient(Agent parentAgent, DistAgentServerRow srv) {
        super(parentAgent, srv);
        try {
            connectedAgentGuid = srv.agentguid;
            log.info("Creates new socket client for server: " + srv.servertype + ", host: " + srv.serverhost + ", port: " + srv.serverport);
            isServer = false;
            socket = new Socket(srv.serverhost, srv.serverport);
        } catch (Exception ex) {
            parentAgent.getAgentIssues().addIssue("SocketServerClient", ex);
            log.warn("Cannot initialize socket to host: " + srv.serverhost +", port: " + srv.serverport + ", current agent: " + parentAgent.getAgentGuid() + ", connecting to agent: " + srv.agentguid + ", reason: " + ex.getMessage());
        }
        initialize();
    }
    /** get type of client - socket, http, datagram, ... */
    public DistClientType getClientType() {
        return DistClientType.socket;
    }
    /** get unified URL of this client */
    public String getUrl() {
        return "socket://" + host + ":" + port;
    }

    /** read configuration and re-initialize this component */
    public boolean componentReinitialize() {
        // TODO: implement reinitialization
        return true;
    }

    /** initialize client - connecting or reconnecting */
    public boolean initialize() {
        try {
            log.info("Initializing socket client for agent: " + parentAgent.getAgentGuid() + ", isServer: " + isServer + ", host: " + host + ", port: " + port + ", client UID: " + clientGuid);
            int socketTimeout = parentAgent.getConfig().getPropertyAsInt(DistConfig.AGENT_CONNECTORS_SERVER_SOCKET_CLIENT_TIMEOUT, DistConfig.AGENT_CONNECTORS_SERVER_SOCKET_CLIENT_TIMEOUT_DEFAULT_VALUE);
            socket.setSoTimeout(socketTimeout);
            host = socket.getInetAddress().getHostAddress();
            port = socket.getPort();
            if (this.socket.isConnected()) {
                log.info("Client from agent " + parentAgent.getAgentGuid() + " IS CONNECTED to socket for server ");
            } else {
                log.warn("Client from agent " + parentAgent.getAgentGuid() + " IS NOT CONNECTED to socket for server ");
            }
            if (this.socket.isInputShutdown()) {
                log.warn("Client from agent " + parentAgent.getAgentGuid() + ", INPUT IS SHUTDOWN !!! ");
            }
            if (this.socket.isOutputShutdown()) {
                log.warn("Client from agent " + parentAgent.getAgentGuid() + ", OUTPUT IS SHUTDOWN !!! ");
            }
            inSocket = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outSocket = new PrintWriter(socket.getOutputStream(),true);
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
            }
            working = true;
            receivingThread = new Thread(this);
            receivingThread.start();
            parentAgent.getAgentThreads().registerThread(this, receivingThread, "socket-client-" + port);
            AgentWelcomeMessage welcome = new AgentWelcomeMessage(parentAgent.getAgentInfo(), getClientInfo());
            DistMessage welcomeMsg = DistMessage.createMessage(DistMessageType.system, parentAgent.getAgentGuid(), DistServiceType.agent, connectedAgentGuid, DistServiceType.agent, "welcome",  welcome);
            send(welcomeMsg);
            return true;
        } catch (SocketException ex) {
            log.info("Cannot initialize client " + clientGuid + ", agent: " + this.parentAgent.getAgentGuid() + ", SockedException at Start("+socket.getPort()+","+socket.getInetAddress().getHostAddress()+"): "+ex.getMessage(), ex);
            return false;
        } catch (IOException ex) {
            log.info("Cannot initialize client " + clientGuid + ", agent: " + this.parentAgent.getAgentGuid() + ", IOException at Start("+socket.getPort()+","+socket.getInetAddress().getHostAddress()+"): "+ex.getMessage(), ex);
            return false;
        }
    }

    /** send message to this client */
    public boolean send(DistMessage msg) {
        try {
            String line = parentAgent.getSerializer().serializeToString(msg);
            log.trace("Writing line to be sent using SOCKET client: " + clientGuid + ", LINE=" + line + ", serializer: " + parentAgent.getSerializer() + ", message: " + msg.toString());
            synchronized (outSocket) {
                outSocket.write(line + "\n");
                outSocket.flush();
                sentMessages.incrementAndGet();
                if (outSocket.checkError()) {
                    log.warn("Error in socket client: " + clientGuid);
                }
            }
            return true;
        } catch (Exception ex) {
            log.warn("ERROR WHILE SENDING DATA FOR CLIENT: " + clientGuid + ", reason: " + ex.getMessage(), ex);
            parentAgent.getAgentIssues().addIssue("SocketServerClient.send", ex);
            return false;
        }
    }
    /** method working in separated thread dedicated for receiving data */
    public void threadWork() {
        try {
            if (!working) {
                log.warn("Socket thread is not working, trying to reconnect");
                //reconnect();
                return;
            }
            if (inSocket == null) {
                log.warn("inSocket is null, trying to reconnect");
                working = false;
                //reconnect();
                return;
            }
            String readLine = inSocket.readLine();
            if (readLine != null) {
                log.trace("Reading line for client: " + clientGuid + ", LINE=" + readLine);
                DistMessage receivedMsg = (DistMessage)parentAgent.getSerializer().deserializeFromString(DistMessage.class.getName(), readLine);
                receivedMessages.incrementAndGet();
                if (receivedMsg.isSystem()) {
                    parseWelcomeMessage(receivedMsg);

                } else {
                    log.trace("Socket client got another message, client: " + clientGuid + ", LINE: " +readLine + ", message: " + receivedMsg.toString());
                    parentAgent.getAgentServices().receiveMessage(receivedMsg);
                }
            }
        }  catch (java.net.SocketTimeoutException ex) {
        }  catch (IOException ex) {
            working = false;
            log.warn(" Exception while reading from socket: "+ex.getMessage());
        } catch (Exception ex) {
            working = false;
            parentAgent.getAgentIssues().addIssue("threadWork", ex);
            log.warn(" Exception in Socket Client: "+ex.getMessage()+"; "+ex.getLocalizedMessage());
        }
    }
    /** */
    private void parseWelcomeMessage(DistMessage msg) {
        try {
            AgentWelcomeMessage welcome = (AgentWelcomeMessage)msg.getMessage();
            log.info("Socked client got WELCOME message, client: " + clientGuid + ", from agent: " + welcome.getAgentInfo().getAgentGuid() + ", from client: " + welcome.getClientInfo().getClientGuid());
            // TODO: welcome message to SocketClient - set Agent name and initial information from Welcome message
        } catch (Exception ex) {
            parentAgent.getAgentIssues().addIssue("parseWelcomeMessage", ex);
        }
    }
    /** close this client */
    protected void onClose() {
        try {
            working = false;
            log.info("Closing socket for GUID: " + clientGuid);
            if (inSocket != null)
                inSocket.close();
            if (outSocket != null)
                outSocket.close();
            if (socket != null)
                socket.close();
        }
        catch (IOException ex) {
            log.info(" Error while closing Socket connection, reason: "+ex.getMessage());
        }
    }

    @Override
    public void run() {
        log.info("Starting thread for socket client on port: " + port + ", GUID: " + clientGuid);
        try {
            Thread.sleep(200);
        } catch (InterruptedException ex) {
            log.warn(" Plugin thread stopped before started for client: " + clientGuid);
        }
        while (working) {
            threadWork();
            try {
                Thread.sleep(sleepThreadTime);
            } catch (InterruptedException ex) {
            }
            // TODO: check if socket is still valid OR there is a need to reconnect
        }

        log.info(" Plugin thread stopped (end) ");
    }

}