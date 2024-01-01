package com.distsystem.agent.clients;

import com.distsystem.api.*;
import com.distsystem.api.enums.DistClientType;
import com.distsystem.api.enums.DistMessageType;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.base.AgentClientBase;
import com.distsystem.api.dtos.DistAgentServerRow;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentClient;
import com.distsystem.interfaces.HttpCallable;
import com.distsystem.utils.HttpConnectionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** HTTP client with client communications */
public class HttpClient extends AgentClientBase implements AgentClient {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(HttpClient.class);
    private String url;
    private HttpCallable httpConnectionHelper;

    /** creates new HTTP client  */
    public HttpClient(Agent parentAgent, DistAgentServerRow srv) {
        super(parentAgent, srv);
        this.url = srv.getServerUrl();
        connectedAgentGuid = srv.getAgentGuid();
        initialize();
    }

    /** count objects in this agentable object including this object */
    public long countObjectsAgentable() {
        return 2L;
    }
    /** get type of client - socket, http, datagram, ... */
    public DistClientType getClientType() {
        return DistClientType.http;
    }
    /** get unified URL of this client */
    public String getUrl() {
        return url;
    }
    /** initialize client - connecting or reconnecting */
    public boolean initialize() {
        try {
            httpConnectionHelper = HttpConnectionHelper.createHttpClient(url);
            log.info("Creates new HTTP client for server: " + serverRow.simpleInfo());
            log.info("Initializing HTTP client for agent: " + parentAgent.getAgentGuid() + ", URL: " + url + ", client UID: " + clientGuid);
            AgentWelcomeMessage welcome = new AgentWelcomeMessage(parentAgent.getAgentSimpleInfo(), getClientInfo());
            DistMessage welcomeMsg = DistMessage.createMessage(DistMessageType.system, parentAgent.getAgentGuid(), DistServiceType.agent, connectedAgentGuid, DistServiceType.agent, "welcome",  welcome);
            send(welcomeMsg);
            return true;
        } catch (Exception ex) {
            parentAgent.getIssues().addIssue("HttpClient.initialize", ex);
            log.warn("Cannot initialize client " + clientGuid + ", agent: " + this.parentAgent.getAgentGuid() + ", Exception at Start: "+ex.getMessage(), ex);
            return false;
        }
    }
    /** read configuration and re-initialize this component */
    public boolean componentReinitialize() {
        // TODO: reinitialize this component - probably nothing to be done here
        return true;
    }
    /** send message to this client */
    public boolean send(DistMessage msg) {
        try {
            String line = parentAgent.getSerializer().serializeToString(msg);
            log.trace("Writing line to be sent using HTTP client: " + clientGuid + ", LINE=" + line + ", serializer: " + parentAgent.getSerializer().getClass().getName() + ", message: " + msg.toString());
            var res = httpConnectionHelper.callPostText("", line);
            return res.isOk();
        } catch (Exception ex) {
            log.warn("ERROR WHILE SENDING DATA FOR CLIENT: " + clientGuid + ", reason: " + ex.getMessage(), ex);
            parentAgent.getIssues().addIssue("HttpClient.send", ex);
            return false;
        }
    }

    /** close this client */
    protected void onClose() {
        log.info("Closing HTTP client for GUID: " + clientGuid);
        try {
            AgentWelcomeMessage welcome = new AgentWelcomeMessage(parentAgent.getAgentSimpleInfo(), getClientInfo());
            DistMessage closeMsg = DistMessage.createMessage(DistMessageType.system, parentAgent.getAgentGuid(), DistServiceType.agent, connectedAgentGuid, DistServiceType.agent, "close",  welcome);
            send(closeMsg);
            working = false;
        } catch (Exception ex) {
            parentAgent.getIssues().addIssue("HttpClient.close", ex);
            log.info(" Error while closing HTTP client connection, reason: "+ex.getMessage());

        }
    }

}