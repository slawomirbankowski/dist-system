package com.distsystem.base;

import com.distsystem.agent.AgentInstance;
import com.distsystem.agent.impl.Agentable;
import com.distsystem.api.*;
import com.distsystem.api.enums.DistComponentType;
import com.distsystem.api.info.AgentRegistrationInfo;
import com.distsystem.base.dtos.DistAgentRegisterRow;
import com.distsystem.base.dtos.DistAgentServerRow;
import com.distsystem.base.dtos.DistAgentServiceRow;
import com.distsystem.interfaces.AgentComponent;
import com.distsystem.utils.CacheHitRatio;
import com.distsystem.utils.DistUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** base class to connect to registration service - global storage that is managing agents
 * connector should be able to register agent, ping it, check health
 * */
public abstract class RegistrationBase extends Agentable implements AgentComponent {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(RegistrationBase.class);
    /** global unique ID */
    private final String registerGuid = DistUtils.generateConnectorGuid(this.getClass().getSimpleName());

    /** confirmation of registration of this agent to connector */
    protected AgentConfirmation registerConfirmation;
    /** flat to indicate if connector is initialized */
    private boolean initialized = false;
    /** is registration closed */
    private boolean closed = false;
    /** status of last connection OR false if there were no connections yet */
    private boolean lastConnected = false;
    /** connection ratio */
    private final CacheHitRatio connectRatio = new CacheHitRatio();

    /** constructor to save parent agent */
    public RegistrationBase(AgentInstance parentAgent) {
        super(parentAgent);
        parentAgent.addComponent(this);
        initialize();
    }

    /** get type of this component */
    public DistComponentType getComponentType() {
        return DistComponentType.registration;
    }
    @Override
    public String getGuid() {
        return getParentAgentGuid();
    }
    /** get global ID of this connector */
    public String getRegisterGuid() {
        return registerGuid;
    }

    /** initialize */
    private void initialize() {
        onInitialize();
        initialized = true;
    }
    /** run for initialization in classes */
    protected abstract void onInitialize();
    /** returns status of initialized */
    public boolean isInitialized() {
        return initialized;
    }

    /** if connector is connected */
    public boolean isConnected() {
        // TODO: calculate connection OK ratio
        boolean connected = onIsConnected();
        lastConnected = connected;
        if (connected) {
            connectRatio.hit();
        } else {
            connectRatio.miss();
        }
        return connected;
    }
    /** last status of connection */
    public boolean isLastConnected() {
        return lastConnected;
    }
    /** if connector is connected */
    protected abstract boolean onIsConnected();
    /** register this agent to connector */
    public AgentConfirmation agentRegister(AgentRegister register) {
        log.info("Registering agent at registration: " + getUrl() + ", registerGuid: " + registerGuid + ", agent: " + register.getAgentGuid());
        AgentConfirmation cfm =  onAgentRegister(register);
        registerConfirmation = cfm;
        return cfm;
    }

    /** add issue for registration */
    public abstract void addIssue(DistIssue issue);
    /** register server for communication */
    public abstract void addServer(DistAgentServerRow serv);
    /** unregister server for communication */
    public abstract void unregisterServer(DistAgentServerRow serv);
    /** agent registration to be implemented in specific connector*/
    protected abstract AgentConfirmation onAgentRegister(AgentRegister register);
    /** ping from this agent to connector */
    public AgentPingResponse agentPing(AgentPing ping) {
        AgentPingResponse pingResp = onAgentPing(ping);
        // TODO: register latest ping response
        return pingResp;
    }
    public AgentConfirmation agentUnregister(AgentRegister register) {
        log.info("Unregistering agent at registration: " + getUrl() + ", registerGuid: " + registerGuid + ", agent: " + register.getAgentGuid());
        register.deactivate();
        AgentConfirmation cfm =  onAgentUnregister(register);
        closed = true;
        return cfm;
    }
    /** to override for agent unregistering */
    protected abstract AgentConfirmation onAgentUnregister(AgentRegister register);

    /** ping from this agent to connector */
    protected abstract AgentPingResponse onAgentPing(AgentPing ping);
    /** inactivate active agents with last ping date for more than X minutes */
    public abstract boolean removeInactiveAgents(LocalDateTime beforeDate);
    /** remove inactive agents with last ping date for more than X minutes */
    public abstract boolean deleteInactiveAgents(LocalDateTime beforeDate);

    /** register service */
    public abstract void registerService(DistAgentServiceRow service);

    /** get normalized URL for this registration */
    public abstract String getUrl();
    /** get custom parameters for this registration */
    public Map<String, Object> getRegistrationCustomParameters() {
        return Map.of();
    }
    /** get information about registration object */
    public AgentRegistrationInfo getInfo() {
        // String registerGuid, String registrationType, LocalDateTime createdDate, boolean initialized, boolean closed, boolean lastConnected, String url, AgentConfirmation confirmation
        return new AgentRegistrationInfo(registerGuid, getClass().getSimpleName(), getCreateDate(), initialized, closed, lastConnected, getUrl(), registerConfirmation, getRegistrationCustomParameters());
    }
    /** get all agents */
    public abstract List<DistAgentRegisterRow> getAgents();
    /** get list of active agents */
    public List<DistAgentRegisterRow> getAgentsActive() {
        return getAgents().stream().filter(a -> a.isactive==1).collect(Collectors.toList());
    }
    /** get all communication servers */
    public abstract List<DistAgentServerRow> getServers();
    /** ping given server by GUID */
    public abstract boolean serverPing(DistAgentServerRow serv);
    /** set active servers with last ping date before given date as inactive */
    public abstract boolean serversCheck(LocalDateTime inactivateBeforeDate, LocalDateTime deleteBeforeDate);

    /** close current connector */
    public void close() {
        onClose();
    }
    /** close current connector */
    protected abstract void onClose();

}
