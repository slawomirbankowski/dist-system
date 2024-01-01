package com.distsystem.agent.services;

import com.distsystem.api.*;
import com.distsystem.api.dtos.DistAgentDaoRow;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.api.info.AgentRegisteredInfo;
import com.distsystem.api.info.AgentRegistrationInfo;
import com.distsystem.api.info.AgentRegistrationsInfo;
import com.distsystem.base.RegistrationBase;
import com.distsystem.base.ServiceBase;
import com.distsystem.api.dtos.DistAgentRegisterRow;
import com.distsystem.api.dtos.DistAgentServerRow;
import com.distsystem.api.dtos.DistAgentServiceRow;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentRegistrations;
import com.distsystem.interfaces.Registration;
import com.distsystem.utils.AdvancedMap;
import com.distsystem.utils.DistUtils;
import com.distsystem.utils.DistWebApiProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/** Implementation of service to register this agent, servers and services in global repository using JDBC, Kafka, Elasticsearch or any other central storage.
 * Central registration could be used to gather information about other agents.
 *  */
public class AgentRegistrationsImpl extends ServiceBase implements AgentRegistrations {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(AgentRegistrationsImpl.class);

    /** list of registration services to register agent, ping agent, register server, register service or unregister items */
    private final java.util.concurrent.ConcurrentHashMap<String, RegistrationBase> registrations = new java.util.concurrent.ConcurrentHashMap<>();
    /** all known agents from registration services, this contains all possible info about each Agent */
    private final java.util.concurrent.ConcurrentHashMap<String, AgentObject> agents = new java.util.concurrent.ConcurrentHashMap<>();
    /** all rows of registered and known servers from other agents that this agent should be able to connect */
    private final LinkedList<DistAgentServerRow> registeredServers = new LinkedList<>();
    /** register object that was used for registration of this object */
    private AgentRegister register;

    public AgentRegistrationsImpl(Agent parentAgent) {
        super(parentAgent);
        parentAgent.getServices().registerService(this);
    }

    /** count objects in this agentable object including this object */
    public long countObjectsService() {
        // TODO: calculate counts of objects
        return 2L;
    }
    /** get type of service: cache, measure, report, flow, space, ... */
    public DistServiceType getServiceType() {
        return DistServiceType.registrations;
    }
    /** get description of this service */
    public String getServiceDescription() {
        return "";
    }
    /** update configuration of this Service to add registrations, services, servers, ... */
    public void updateConfig(DistConfig newCfg) {
    }
    /** additional web API endpoints */
    protected DistWebApiProcessor additionalWebApiProcessor() {
        return new DistWebApiProcessor(getServiceType())
                .addHandlerGet("registration-keys", (m, req) -> req.responseOkJsonSerialize(registrations.keySet()))
                .addHandlerGet("registration-infos", (m, req) -> req.responseOkJsonSerialize(getRegistrationInfos()))
                .addHandlerGet("agents-all", (m, req) -> req.responseOkJsonSerialize(getAgents()))
                .addHandlerGet("agents-active", (m, req) -> req.responseOkJsonSerialize(getAgentsActive()))
                .addHandlerGet("agents-keys", (m, req) -> req.responseOkJsonSerialize(agents.keySet()))
                .addHandlerGet("agents-now", (m, req) -> req.responseOkJsonSerialize(getAgentsNow()))
                .addHandlerGet("agent", (m, req) -> req.responseOkJsonSerialize(getAgentInfo(req.getParamOne())))
                .addHandlerGet("servers", (m, req) -> req.responseOkJsonSerialize(registeredServers.stream().map(s -> s.copyNoPassword()).toList()))
                .addHandlerGet("register", (m, req) -> req.responseOkJsonSerialize(register.toAgentRegisterRow()))
                .addHandlerGet("info", (m, req) -> req.responseOkJsonSerialize(getInfo()));
    }
    /** read configuration and re-initialize this component */
    protected boolean onReinitialize() {
        // TODO: implement reinitialization
        // TODO: check what should be re-initialized
        registerConfigGroup(DistConfig.AGENT_REGISTRATION_OBJECT);
        return true;
    }
    /** change values in configuration bucket - to override this method */
    public AdvancedMap initializeConfigBucket(DistConfigBucket bucket) {
        AdvancedMap status = AdvancedMap.create(this);
        // TODO: insert, update, delete of bucket
        log.info("Initializing new registration with bucket for agent: " + parentAgent.getAgentGuid() + ", bucket key: " + bucket.getKey() + ", current servers: " + registeredServers.size());
        Optional<RegistrationBase> reg = createRegistration(bucket);
        return reg.stream().map(r -> r.statusMap()).findFirst().orElse(status.withStatus("NOT_CREATED"));
    }
    /** run after initialization */
    public void afterInitialization() {
        log.debug("Running after initialization method for agent: " + parentAgent.getAgentGuid() + " to all registration services, registration servers count: " + registrations.size());
        registerToAll();
        removeInactiveAgents();
        checkActiveAgents();
        log.debug("Set up timer to refresh registration items like agents, servers, agent: " + getParentAgentGuid());
        parentAgent.getTimers().cancelTimer("TIMER_REGISTRATION");
        parentAgent.getTimers().setUpTimer("TIMER_REGISTRATION", DistConfig.AGENT_CACHE_TIMER_REGISTRATION_PERIOD, DistConfig.AGENT_CACHE_TIMER_REGISTRATION_PERIOD_DELAY_VALUE, x -> onTimeRegisterRefresh());
    }

    /** get number of registration */
    public int getRegistrationsCount() {
        return registrations.size();
    }
    /** get UIDs for registration services */
    public List<String> getRegistrationKeys() {
        return registrations.values().stream()
                .map(RegistrationBase::getRegisterGuid)
                .collect(Collectors.toList());
    }
    /** get information object about all registration */
    public AgentRegistrationsInfo getInfo() {
        return new AgentRegistrationsInfo(
                getRegistrationInfos(),
                checkCount.get(),
                agents.values().stream()
                        .filter(AgentObject::isActive)
                        .map(AgentObject::getRegisteredInfo)
                        .collect(Collectors.toList()));
    }
    /** get information infos about registration objects */
    public List<AgentRegistrationInfo> getRegistrationInfos() {
        return registrations.values().stream()
                .map(RegistrationBase::getInfo)
                .collect(Collectors.toList());
    }
    /** get list of connected agents */
    public List<DistAgentRegisterRow> getAgentsNow() {
        return registrations.values().stream()
                .flatMap(x -> x.getAgents().stream())
                .collect(Collectors.toList());
    }
    /** get info about agent by GUID */
    public Optional<AgentRegisteredInfo> getAgentInfo(String guid) {
        AgentObject agent = agents.get(guid);
        if (agent != null) {
            return Optional.of(agent.getRegisteredInfo());
        } else {
            return Optional.empty();
        }
    }
    /** get number of known agents */
    public int getAgentsCount() {
        return agents.size();
    }
    /** get number of active agents */
    public long getAgentsActiveCount() {
        return agents.values().stream().filter(AgentObject::isActive).count();
    }

    /** create new registration */
    public void createRegistration(String configType, String configInstance, Map<String, String> configValues) {
        configGroup.addConfigValues("OBJECT", configType,configInstance, configValues);
    }

    /** register server for ROW */
    public void registerServer(DistAgentServerRow servDto) {
        createEvent("registerServer");
        log.debug("Registering server for GUID: " + servDto.getServerGuid() + ", server type: " + servDto.getServerType() + ", current servers: " + registeredServers.size() + ", current registrations: " + registrations.size());
        registrations.values().stream().forEach(reg -> reg.addServer(servDto));
        registeredServers.add(servDto);
    }
    /** get all servers from registration services */
    public List<DistAgentServerRow> getServers() {
        return registrations.values().stream().flatMap(reg -> reg.getServers().stream()).collect(Collectors.toList());
    }
    /** get list of agents possible to connect */
    public List<DistAgentRegisterRow> getAgents() {
        return agents.values().stream().map(AgentObject::getSimplified).collect(Collectors.toList());
    }
    /** get all active agents */
    public List<DistAgentRegisterRow> getAgentsActive() {
        return agents.values().stream().filter(AgentObject::isActive).map(AgentObject::getSimplified).collect(Collectors.toList());
    }
    /** get all registrations */
    public List<Registration> getRegistrations() {
        return registrations.values().stream().map(x -> (Registration)x).toList();
    }

    /** create registration base object for current instance and given class */
    private Optional<RegistrationBase> createRegistration(DistConfigBucket bucket) {
        createEvent("createRegistration");
        String className = DistConfig.AGENT_REGISTRATION_CLASS_MAP.get(bucket.getKey().getConfigType());
        try {
            log.debug("Try to create registration for class: " + className + ", bucket key: " + bucket.getKey());
            RegistrationBase registr = (RegistrationBase)Class.forName(className)
                    .getConstructor(ServiceObjectParams.class)
                    .newInstance(ServiceObjectParams.create(parentAgent, this, className, bucket));
            log.debug("Created registration for class: " + className + ", guid: " + registr.getRegisterGuid() + ", connected: " + registr.isConnected() + ", current registration servers: " + registrations.size());
            registrations.put(registr.getGuid(), registr);
            return Optional.of(registr);
        } catch (Exception ex) {
            log.warn("Cannot create new registration object for class: " + className + ", reason: " + ex.getMessage(), ex);
            addIssueToAgent("createRegistrationForClass", ex);
            return Optional.empty();
        }
    }

    /** run by agent every X seconds */
    public boolean onTimeRegisterRefresh() {
        try {
            checkCount.incrementAndGet();
            log.info("Timer registration check, seq: " + checkCount.get() +", working time: " + parentAgent.getAgentWorkingTime());
            createEvent("onTimeRegisterRefresh");
            pingAllRegistrations();
            checkActiveAgents();
            removeInactiveAgents();
            saveDaos();
            long activeAgentsCount = getAgent().getRegistrations().getAgentsActiveCount();
            // TODO: connect to all nearby agents, check statuses
            log.debug("AGENT REGISTRATION summary for guid: " + parentAgent.getAgentGuid() + ", registrations: " + registrations.size() + ", connected agents: " + agents.size() + ", activeAgentsCount: " + activeAgentsCount + ", registeredServers: " + registeredServers.size());
            return true;
        } catch (Exception ex) {
            log.warn("Cannot ping registrations, check agents or remove inactive agents, reason: " + ex.getMessage(), ex);
            addIssueToAgent("onTimeRegisterRefresh", ex);
            return false;
        }
    }
    /** add issue to registrations */
    public void addIssue(DistIssue issue) {
        registrations.values().stream().forEach(reg -> reg.addIssue(issue));
    }
    /** close registration services */
    protected void onClose() {
        unregisterFromAll();
    }

    /** ping all registrations to notify that this agent is still working */
    private void pingAllRegistrations() {
        createEvent("pingAllRegistrations");
        log.debug("Agent - Ping registration objects, registrations: " + registrations.size());
        AgentPing pingObj = new AgentPing(register, agents.size(), parentAgent.getThreads().getThreadsCount(),
                parentAgent.getServices().getServicesCount(), parentAgent.getConnectors().getClientsCount(), parentAgent.getConnectors().getServersCount());
        AgentObject currAgent = agents.get(parentAgent.getAgentGuid()); // update ping of current agent
        if (currAgent != null) {
            currAgent.ping(pingObj);
        }
        registrations.entrySet().stream().forEach(e -> {
            e.getValue().agentPing(pingObj);
        });
    }
    /** check all active agents from all registrations */
    private void checkActiveAgents() {
        createEvent("checkActiveAgents");
        log.debug("Check connected agents for agent: " + parentAgent.getAgentGuid() + ", current count: " + agents.size() + ", registrations: " + registrations.size() + ", registeredServers: " + registeredServers.size());
        registrations.entrySet().stream().forEach(regObj -> {
            List<DistAgentRegisterRow> allAgentsInRegistration = regObj.getValue().getAgents();
            log.debug("Get other agents from registrations, agent: " + parentAgent.getAgentGuid() + ", registration: " + regObj.getKey() +", allAgents: " + allAgentsInRegistration.size());
            allAgentsInRegistration.stream().forEach(agentFromRegistration -> {
                AgentObject someAgent = agents.get(agentFromRegistration.getAgentGuid());
                if (someAgent == null) {
                    someAgent = new AgentObject(agentFromRegistration);
                    log.debug("Adding NEW agent from registration TO current Agent: " + parentAgent.getAgentGuid() + ", connected Agent: " + agentFromRegistration.getAgentGuid() + ", active: " + agentFromRegistration.getActive() + ", Agents count: " + agents.size());
                    agents.put(agentFromRegistration.getAgentGuid(), someAgent);
                }
                someAgent.update(agentFromRegistration, regObj.getKey());
            });
        });
        checkCount.incrementAndGet();
        log.info("AFTER check connected agents for agent: " + parentAgent.getAgentGuid() + ", current count: " + agents.size() + ", registrations: " + registrations.size() + ", registeredServers: " + registeredServers.size());
    }
    /** remove all inactive agents */
    public void removeInactiveAgents() {
        createEvent("removeInactiveAgents");
        long inactivateBeforeSecondsAgo = getConfig().getPropertyAsLong(DistConfig.AGENT_CONNECTORS_INACTIVATE_AFTER, DistConfig.AGENT_CONNECTORS_INACTIVATE_AFTER_DEFAULT_VALUE)/1000;
        LocalDateTime inactivateBeforeDate = LocalDateTime.now().minusSeconds(inactivateBeforeSecondsAgo);
        long deleteBeforeSecondsAgo = getConfig().getPropertyAsLong(DistConfig.AGENT_CONNECTORS_DELETE_AFTER, DistConfig.AGENT_CONNECTORS_DELETE_AFTER_DEFAULT_VALUE)/1000;
        LocalDateTime deleteBeforeDate = LocalDateTime.now().minusSeconds(deleteBeforeSecondsAgo);
        log.debug("Inactivate agents that have no ping for last " + (inactivateBeforeSecondsAgo) + " seconds, remove inactive agents with ping before " + deleteBeforeSecondsAgo + " seconds ago");
        registrations.entrySet().stream().forEach(e -> {
            e.getValue().removeInactiveAgents(inactivateBeforeDate);
            e.getValue().deleteInactiveAgents(deleteBeforeDate);
        });
        registrations.values().stream().forEach(reg -> {
            registeredServers.stream().forEach(srv -> {
                reg.serverPing(srv);
            });
            reg.serversCheck(inactivateBeforeDate, deleteBeforeDate);
        });
    }

    /** save all DAOs */
    private void saveDaos() {
        createEvent("saveDaos");
        List<DistAgentDaoRow> daoRows = parentAgent.getAgentDao().getAllDaos().stream().map(d -> d.toRow()).collect(Collectors.toList());
        log.info("Save all DAOs in all registration objects, daos: " + daoRows.size() +", registrations: " + registrations.size());
        registrations.values().stream().forEach(reg -> {
            daoRows.forEach(daoRow -> {
                reg.addDao(daoRow);
            });
        });
    }
    /** register this agent to all available connectors
     * this is registering agent itself,  */
    private void registerToAll() {
        createEvent("registerToAll");
        var agents = getAgents();
        register = new AgentRegister(parentAgent.getAgentGuid(), parentAgent.getAgentSecret(),
                DistUtils.getCurrentHostName(), DistUtils.getCurrentHostAddress(),
                parentAgent.getApi().getPort(), parentAgent.getCreateDate(), agents, true);
        List<DistAgentServiceRow> serviceRows = parentAgent.getServices().getServiceRows();
        List<DistAgentServerRow> serverRows = parentAgent.getConnectors().getServerRows();
        try {
            synchronized (registrations) {
                log.info("Registering this agent to all registration objects, GUID: " + parentAgent.getAgentGuid() + " on host " + register.getHostName() + ", services: " + serviceRows.size() + " to all registrations: " + registrations.size());
                registrations.values().stream().forEach(regSrv -> {
                    regSrv.agentRegister(register);
                    serviceRows.forEach(serviceRow -> {
                        regSrv.registerService(serviceRow);
                    });
                    serverRows.forEach(servRow -> {
                        regSrv.addServer(servRow);

                    });
                });
            }
        } catch (Exception ex) {
            log.warn("Cannot register agent " + parentAgent.getAgentGuid() + " to registration services, reason: " + ex.getMessage(), ex);
            addIssueToAgent("registerToAll", ex);
        }
    }

    /** unregister agent and all servers from all registration services */
    private void unregisterFromAll() {
        try {
            createEvent("unregisterFromAll");
            synchronized (registrations) {
                log.info("Unregistering this agent: " +  parentAgent.getAgentGuid() + " from all registrations: " + registrations.size());
                registrations.values().stream().forEach(reg -> {
                    if (register != null) {
                        register.deactivate();
                        reg.agentUnregister(register);
                    }
                    registeredServers.stream().forEach(srv -> {
                        reg.unregisterServer(srv);
                    });
                });
                agents.clear();
            }
        } catch (Exception ex) {
            log.warn("Cannot unregister agent, reason: " + ex.getMessage(), ex);
            addIssueToAgent("unregisterFromAll", ex);
        }
    }

}
