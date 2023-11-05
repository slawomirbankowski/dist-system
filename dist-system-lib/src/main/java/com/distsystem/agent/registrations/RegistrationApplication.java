package com.distsystem.agent.registrations;

import com.distsystem.agent.AgentInstance;
import com.distsystem.api.*;
import com.distsystem.base.RegistrationBase;
import com.distsystem.base.dtos.DistAgentRegisterRow;
import com.distsystem.base.dtos.DistAgentServerRow;
import com.distsystem.base.dtos.DistAgentServiceRow;
import com.distsystem.interfaces.HttpCallable;
import com.distsystem.utils.HttpConnectionHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

/** connector to global dist-cache application - central point with registering/unregistering agents  */
public class RegistrationApplication extends RegistrationBase {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(RegistrationApplication.class);

    /** */
    private String urlString;
    /** HTTP connection helper */
    private HttpCallable applicationConn = null;

    public RegistrationApplication(AgentInstance parentAgent) {
        super(parentAgent);
    }
    /** run for initialization in classes */
    @Override
    public void onInitialize() {
        urlString = parentAgent.getConfig().getProperty(DistConfig.AGENT_CACHE_APPLICATION_URL);
        try {
            log.info("Connecting to dist-cache application, URL: " + urlString);
            applicationConn = HttpConnectionHelper.createHttpClient(urlString);
        } catch (Exception ex) {
            parentAgent.getAgentIssues().addIssue("AgentTimersImpl.onInitialize", ex);
            log.warn("Cannot connect to dist-cache application, reason: " + ex.getMessage(), ex);
        }
    }

    /** read configuration and re-initialize this component */
    public boolean componentReinitialize() {
        // TODO: implement reinitialization

        if (applicationConn != null) {

        }
        onInitialize();

        return true;
    }
    @Override
    protected boolean onIsConnected() {
        return false;
    }
    @Override
    protected AgentConfirmation onAgentRegister(AgentRegister register) {
        try {
            log.info("Try to register agent as dist-cache application on URL: " + urlString + ", agent: " + register.getAgentGuid());
            ObjectMapper mapper = JsonMapper.builder()
                    .findAndAddModules()
                    .build();
            String registerBody = mapper.writeValueAsString(register);
            applicationConn = HttpConnectionHelper.createHttpClient(urlString);
            log.info("Try to register agent with endpoint /agent and body: " + registerBody);
            var response = applicationConn.callPut("/v1/agent", registerBody);
            // TODO: save response from application
            log.info("Got registration response from APP: " + response.getInfo());
            return null;
        } catch (Exception ex) {
            parentAgent.getAgentIssues().addIssue("RegistrationApplication.onAgentRegister", ex);
            log.warn("Cannot connect to dist-cache application, reason: " + ex.getMessage(), ex);
            return null;
        }
    }
    protected AgentConfirmation onAgentUnregister(AgentRegister register) {

        return new AgentConfirmation(register.getAgentGuid(), true, false, 0, List.of());
    }
    @Override
    protected AgentPingResponse onAgentPing(AgentPing ping) {
        // TODO: implement ping to connector from this agent

        return null;
    }
    /** remove active agents without last ping date for more than X minutes */
    public boolean removeInactiveAgents(LocalDateTime beforeDate) {
        return true;
    }
    /** remove inactive agents with last ping date for more than X minutes */
    public boolean deleteInactiveAgents(LocalDateTime beforeDate) {
        return true;
    }

    /** get normalized URL for this registration */
    public String getUrl() {
        return urlString;
    }

    /** add issue for registration */
    public void addIssue(DistIssue issue) {
    }
    /** register server for communication */
    public void addServer(DistAgentServerRow serv) {
    }
    /** unregister server for communication */
    public void unregisterServer(DistAgentServerRow serv) {
    }
    /** get all communication servers */
    public  List<DistAgentServerRow> getServers() {
        return new LinkedList<>();
    }
    /** ping given server by GUID */
    public boolean serverPing(DistAgentServerRow serv) {
        return false;
    }
    /** set active servers with last ping date before given date as inactive */
    public boolean serversCheck(LocalDateTime inactivateBeforeDate, LocalDateTime deleteBeforeDate) {
        return false;
    }

    /** register service */
    public void registerService(DistAgentServiceRow service) {

    }

    /** get agents from registration services */
    public List<DistAgentRegisterRow> getAgents() {
        return new LinkedList<>();
    }

    /** close current connector */
    @Override
    protected void onClose() {
        // TODO: implement closing this connector
    }

}
