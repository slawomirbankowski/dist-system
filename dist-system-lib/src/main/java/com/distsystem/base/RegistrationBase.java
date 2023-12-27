package com.distsystem.base;

import com.distsystem.api.*;
import com.distsystem.api.dtos.*;
import com.distsystem.api.enums.DistComponentType;
import com.distsystem.api.info.AgentRegistrationInfo;
import com.distsystem.interfaces.AgentComponent;
import com.distsystem.interfaces.Registration;
import com.distsystem.utils.CacheHitRatio;
import com.distsystem.utils.DistUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** base class to connect to registration service - global storage that is managing agents
 * connector should be able to register agent, ping it, check health
 * */
public abstract class RegistrationBase extends ServiceObjectBase implements AgentComponent, Registration {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(RegistrationBase.class);
    /** global unique ID */
    private final String registerGuid = DistUtils.generateConnectorGuid(this.getClass().getSimpleName());

    /** confirmation of registration of this agent to connector */
    protected AgentConfirmation registerConfirmation;
    /** status of last connection OR false if there were no connections yet */
    private boolean lastConnected = false;
    /** connection ratio */
    private final CacheHitRatio connectRatio = new CacheHitRatio();

    /** constructor to save parent agent */
    public RegistrationBase(ServiceObjectParams params) {
        super(params);
        parentAgent.addComponent(this);
        initialize();
        createEvent("RegistrationBase");
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
        createEvent("initialize");
    }
    /** run for initialization in classes */
    protected abstract void onInitialize();

    /** if connector is connected */
    public boolean isConnected() {
        createEvent("isConnected");
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
    public DistStatusMap statusMap() {
        return DistStatusMap.create(this).notImplemented();
    }
    /** last status of connection */
    public boolean isLastConnected() {
        return lastConnected;
    }
    /** if connector is connected */
    protected abstract boolean onIsConnected();
    /** register this agent to connector */
    public AgentConfirmation agentRegister(AgentRegister register) {
        createEvent("agentRegister");
        log.info("Registering agent at registration: " + getUrl() + ", registerGuid: " + registerGuid + ", agent: " + register.getAgentGuid());
        AgentConfirmation cfm =  onAgentRegister(register);
        registerConfirmation = cfm;
        return cfm;
    }

    /** agent registration to be implemented in specific connector*/
    protected abstract AgentConfirmation onAgentRegister(AgentRegister register);
    /** ping from this agent to connector */
    @Override
    public AgentPingResponse agentPing(AgentPing ping) {
        createEvent("agentPing");
        AgentPingResponse pingResp = onAgentPing(ping);
        // TODO: register latest ping response
        return pingResp;
    }
    @Override
    public AgentConfirmation agentUnregister(AgentRegister register) {
        createEvent("agentUnregister");
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

    /** get custom parameters for this registration */
    @Override
    public Map<String, Object> getRegistrationCustomParameters() {
        return Map.of();
    }
    /** get information about registration object */
    @Override
    public AgentRegistrationInfo getInfo() {
        // String registerGuid, String registrationType, LocalDateTime createdDate, boolean initialized, boolean closed, boolean lastConnected, String url, AgentConfirmation confirmation
        return new AgentRegistrationInfo(registerGuid, getClass().getSimpleName(), getCreateDate(), initialized, closed, lastConnected, getUrl(), registerConfirmation, getRegistrationCustomParameters());
    }

    /** get list of active agents */
    @Override
    public List<DistAgentRegisterRow> getAgentsActive() {
        return getAgents().stream().filter(a -> a.getActive()==1).collect(Collectors.toList());
    }


    /** close current connector */
    protected abstract void onClose();

}
