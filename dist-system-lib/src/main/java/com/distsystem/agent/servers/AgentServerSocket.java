package com.distsystem.agent.servers;

import com.distsystem.agent.clients.SocketServerClient;
import com.distsystem.api.enums.DistClientType;
import com.distsystem.api.DistConfig;
import com.distsystem.base.ServerBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentServer;
import com.distsystem.utils.DistUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.LinkedList;

/** Socket server to listen on one port */
public class AgentServerSocket extends ServerBase implements AgentServer, Runnable {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(AgentServerSocket.class);

    /** socket server for connections from other agents */
    private java.net.ServerSocket serverSocket = null;
    /** all threads initialized */
    private LinkedList<Thread> threads = new LinkedList<>();
    /** all threads initialized */
    private LinkedList<SocketServerClient> clients = new LinkedList<>();
    /** all clients but organized by agent GUID when agent is sending introduction message */
    private HashMap<String,SocketServerClient> clientsByAgentGuid = new HashMap<>();
    private int workingPort;

    /** creates new server for communication based on socket */
    public AgentServerSocket(Agent parentAgent) {
        super(parentAgent);
        initialize();
    }

    /** get type of clients to be connected to this server */
    public DistClientType getClientType() {
        return DistClientType.socket;
    }
    /** get port of this server */
    public int getPort() {
        return workingPort;
    }
    /** get port of this server */
    public String getUrl() {
        return "socket://" + DistUtils.getCurrentHostName() + ":" + workingPort + "/";
    }
    public void initialize() {
        try {
            workingPort = parentAgent.getConfig().getPropertyAsInt(DistConfig.AGENT_CONNECTORS_SERVER_SOCKET_PORT, DistConfig.AGENT_CONNECTORS_SOCKET_PORT_VALUE_SEQ.incrementAndGet());;
            // open socket port
            log.info("Starting socket for incoming connections from other clients on port " + workingPort + ", server UID: " + serverGuid + ", agent: " + parentAgent.getAgentGuid());
            // create SocketServer and thread for accepting sockets
            serverSocket = new java.net.ServerSocket(workingPort);
            serverSocket.setSoTimeout(2000);
            log.info("Starting socket server on port: " + workingPort);
            Thread mainThread = new Thread(this);
            mainThread.setDaemon(true);
            mainThread.start();
            parentAgent.getAgentThreads().registerThread(this, mainThread, "socket-server-" + workingPort);
            threads.add(mainThread);
        } catch (Exception ex) {
            log.warn("Cannot run socket server on port: " + workingPort + ", reason: " + ex.getMessage());
        }
    }
    /** read configuration and re-initialize this component */
    public boolean componentReinitialize() {
        // TODO: reinitialize this component
        return true;
    }
    /** run in separated thread to accept new Sockets */
    public void run() {
        log.info("......... Accepting connections on port " + workingPort);
        while (!closed) {
            try {
                Socket socket = serverSocket.accept();
                if (socket != null) {
                    log.info("......... SERVER - New socket connected on port " + workingPort + ", creating client");
                    SocketServerClient client = new SocketServerClient(parentAgent, socket);
                    clients.add(client);
                    parentAgent.getAgentConnectors().registerLocalClient(client);
                }
            } catch (SocketTimeoutException ex) {
            } catch (Exception ex) {
                log.error("!!!!! Unknown exception on Socket server working on port " + workingPort);
            }
            DistUtils.sleep(2000);
        }
    }


    protected void onClose() {
        // close socket server and all clients
        closed = true;
        threads.stream().forEach(th -> {
            try {
                log.info("Closing main thread of socket server");
                // TODO: proper thread close
                th.join(4000);
            } catch (Exception ex) {
                //
                log.info("Cannot close thread: " + th.getName() + ", reason: " + ex.getMessage());
            }
        });
        clients.stream().forEach(c -> {
            c.close();
        });
        try {
            log.info("Closing socket server at port " + workingPort);
            serverSocket.close();
        } catch (Exception ex) {
            log.warn("Cannot close socket server, reason: " + ex.getMessage());
        }
    }
}
