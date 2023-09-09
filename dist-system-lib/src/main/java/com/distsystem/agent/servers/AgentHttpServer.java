package com.distsystem.agent.servers;

import com.distsystem.api.AgentWelcomeMessage;
import com.distsystem.api.enums.DistClientType;
import com.distsystem.api.DistConfig;
import com.distsystem.api.DistMessage;
import com.distsystem.base.ServerBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentServer;
import com.distsystem.utils.DistUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/** HTTP server to exchange messages between Agents */
public class AgentHttpServer extends ServerBase implements AgentServer {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(AgentHttpServer.class);
    private com.sun.net.httpserver.HttpServer httpServer;
    private AgentHttpHandler httpHandler;
    private int httpPort;
    private String httpUrl;

    public AgentHttpServer(Agent parentAgent) {
        super(parentAgent);
        initialize();
    }
    public void initialize() {
        try {
            httpPort = parentAgent.getConfig().getPropertyAsInt(DistConfig.AGENT_SERVER_HTTP_PORT, DistConfig.AGENT_SOCKET_PORT_VALUE_SEQ.incrementAndGet());
            httpUrl = "http://" + DistUtils.getCurrentHostName() + ":" + httpPort + "/";
            log.info("Starting new HTTP server at port:" + httpPort + ", agent: " + parentAgent.getAgentGuid());
            httpServer = HttpServer.create(new InetSocketAddress(httpPort), 0);
            httpHandler = new AgentHttpHandler(this, parentAgent);
            httpServer.createContext("/", httpHandler);
            httpServer.setExecutor(null);
            httpServer.start();
            log.info("Started HTTP server!!!");
        } catch (Exception ex) {
            log.info("Cannot start HTTP server, reason: " + ex.getMessage());
            parentAgent.getAgentIssues().addIssue("AgentHttpServer", ex);
        }
    }
    /** get type of clients to be connected to this server */
    public DistClientType getClientType() {
        return DistClientType.http;
    }
    /** get port of this server */
    public int getPort() {
        return httpPort;
    }
    /** get port of this server */
    public String getUrl() {
        return httpUrl;
    }
    static class AgentHttpHandler implements HttpHandler {
        private AgentHttpServer server;
        private Agent parentAgent;
        public AgentHttpHandler(AgentHttpServer server, Agent parentAgent) {
            this.server = server;
            this.parentAgent = parentAgent;
        }
        @Override
        public void handle(HttpExchange t) throws IOException {
            try {
                String line = new String(t.getRequestBody().readAllBytes());
                log.info("-------------------------------------------------------->>>>>>> Receive line: " + line);
                DistMessage msg = (DistMessage)parentAgent.getSerializer().deserializeFromString(DistMessage.class.getName(), line);
                server.receivedMessages.incrementAndGet();
                log.info("Receive message in HTTP server for agent: " + parentAgent.getAgentGuid() + ", message: " + msg.toString());
                if (msg.isSystem()) {
                    // parseWelcomeMessage(msg);
                } else {
                    parentAgent.getAgentServices().receiveMessage(msg);
                }
                String response = "OK";
                t.sendResponseHeaders(200, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } catch (Exception ex) {
                parentAgent.getAgentIssues().addIssue("AgentHttpHandler.handle", ex);
            }
        }
    }
    /** */
    private void parseWelcomeMessage(DistMessage msg) {
        try {
            AgentWelcomeMessage welcome = (AgentWelcomeMessage)msg.getMessage();
            log.info("Got WELCOME message, server: " + getServerGuid() + ", from agent: " + welcome.getAgentInfo().getAgentGuid() + ", from client: " + welcome.getClientInfo().getClientGuid());
            // TODO: welcome message to SocketClient - set Agent name and initial information from Welcome message
        } catch (Exception ex) {
            parentAgent.getAgentIssues().addIssue("parseWelcomeMessage", ex);
        }
    }

    @Override
    public void close() {
        try {
            log.info("Try to close HTTP server for Agent: " + parentAgent.getAgentGuid());
            httpServer.stop(3);
        } catch (Exception ex) {
        }
    }
}
