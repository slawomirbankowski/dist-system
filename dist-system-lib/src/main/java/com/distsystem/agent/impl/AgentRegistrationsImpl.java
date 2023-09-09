package com.distsystem.agent.impl;

import com.distsystem.agent.AgentInstance;
import com.distsystem.agent.registrations.RegistrationApplication;
import com.distsystem.agent.registrations.RegistrationElasticsearch;
import com.distsystem.agent.registrations.RegistrationJdbc;
import com.distsystem.agent.registrations.RegistrationKafka;
import com.distsystem.api.*;
import com.distsystem.api.enums.DistComponentType;
import com.distsystem.api.info.AgentRegistrationInfo;
import com.distsystem.api.info.AgentRegistrationsInfo;
import com.distsystem.base.RegistrationBase;
import com.distsystem.base.dtos.DistAgentRegisterRow;
import com.distsystem.base.dtos.DistAgentServerRow;
import com.distsystem.base.dtos.DistAgentServiceRow;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentComponent;
import com.distsystem.interfaces.AgentRegistrations;
import com.distsystem.utils.DistUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/** Implementation of manager to register this agent, servers and services in global repository using JDBC, Kafka, Elasticsearch or any other central storage.
 * Central registration could be used to gather information about other agents.
 *  */
public class AgentRegistrationsImpl extends Agentable implements AgentRegistrations, AgentComponent {

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
    /** check counter of active agents */
    private final AtomicLong checkCount = new AtomicLong();

    public AgentRegistrationsImpl(Agent parentAgent) {
        super(parentAgent);
        parentAgent.addComponent(this);
    }

    /** get type of this component */
    public DistComponentType getComponentType() {
        return DistComponentType.registrations;
    }
    @Override
    public String getGuid() {
        return getParentAgentGuid();
    }
    /** create initial registration services
     * there are services to register agent, servers, issues, configurations */
    public void createRegistrations() {
        if (parentAgent.getConfig().hasProperty(DistConfig.CACHE_APPLICATION_URL)) {
            createAndAddRegistrations(RegistrationApplication.class.getName());
        }
        if (parentAgent.getConfig().hasProperty(DistConfig.AGENT_REGISTRATION_JDBC_URL)) {
            createAndAddRegistrations(RegistrationJdbc.class.getName());
        }
        if (parentAgent.getConfig().hasProperty(DistConfig.AGENT_REGISTRATION_KAFKA_BROKERS)) {
            createAndAddRegistrations(RegistrationKafka.class.getName());
        }
        if (parentAgent.getConfig().hasProperty(DistConfig.AGENT_REGISTRATION_ELASTICSEARCH_URL)) {
            createAndAddRegistrations(RegistrationElasticsearch.class.getName());
        }
        log.info("Registered agent " + parentAgent.getAgentGuid() + " to all registration services, count: " + registrations.size());
        registerToAll();
        removeInactiveAgents();
        checkActiveAgents();
        log.info("Set up timer to refresh registration items like agents, servers, agent: " + getParentAgentGuid());
        parentAgent.getAgentTimers().setUpTimer("TIMER_REGISTRATION", DistConfig.TIMER_REGISTRATION_PERIOD, DistConfig.TIMER_REGISTRATION_PERIOD_DELAY_VALUE, x -> onTimeRegisterRefresh());
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
    /** get number of known agents */
    public int getAgentsCount() {
        return agents.size();
    }

    /** register server for ROW */
    public void registerServer(DistAgentServerRow servDto) {
        log.info("Registering server for GUID: " + servDto.serverguid + ", server type: " + servDto.servertype + ", servers: " + registeredServers.size() + ", registrations: " + registrations.size());
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

    /** create global connector and save in connectors */
    private void createAndAddRegistrations(String className) {
        synchronized (registrations) {
            createRegistrationForClass(className).stream().forEach(reg -> {
                registrations.put(reg.getRegisterGuid(), reg);
            });
        }
    }
    /** create registration base object for current instance and given class */
    private Optional<RegistrationBase> createRegistrationForClass(String className) {
        try {
            log.info("Try to create registration for class: " + className);
            RegistrationBase registr = (RegistrationBase)Class.forName(className)
                    .getConstructor(AgentInstance.class)
                    .newInstance(parentAgent);
            log.info("Created registration for class: " + className + ", guid: " + registr.getRegisterGuid() + ", connected: " + registr.isConnected());
            return Optional.of(registr);
        } catch (Exception ex) {
            log.warn("Cannot create new registration object for class: " + className + ", reason: " + ex.getMessage(), ex);
            parentAgent.getAgentIssues().addIssue("AgentRegistrationsImpl.createRegistrationForClass", ex);
            return Optional.empty();
        }
    }
    /** run by agent every X seconds */
    public boolean onTimeRegisterRefresh() {
        try {
            pingAllRegistrations();
            checkActiveAgents();
            removeInactiveAgents();
            // TODO: connect to all nearby agents, check statuses
            log.info("AGENT REGISTRATION summary for guid: " + parentAgent.getAgentGuid() + ", registrations: " + registrations.size() + ", connected agents: " + agents.size() + ", registeredServers: " + registeredServers.size());
            return true;
        } catch (Exception ex) {
            log.warn("Cannot ping registrations, check agents or remove inactive agents, reason: " + ex.getMessage(), ex);
            parentAgent.getAgentIssues().addIssue("AgentRegistrationsImpl.onTimeRegisterRefresh", ex);
            return false;
        }
    }
    /** add issue to registrations */
    public void addIssue(DistIssue issue) {
        registrations.values().stream().forEach(reg -> reg.addIssue(issue));
    }
    /** close registration services */
    public void close() {
        unregisterFromAll();
    }

    /** ping all registrations to notify that this agent is still working */
    private void pingAllRegistrations() {
        log.info("Agent - Ping registration objects, registrations: " + registrations.size());
        AgentPing pingObj = new AgentPing(register, agents.size(), parentAgent.getAgentThreads().getThreadsCount(),
                parentAgent.getAgentServices().getServicesCount(), parentAgent.getAgentConnectors().getClientsCount(), parentAgent.getAgentConnectors().getServersCount());
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
        log.info("Check connected agents for agent: " + parentAgent.getAgentGuid() + ", current count: " + agents.size() + ", registrations: " + registrations.size() + ", registeredServers: " + registeredServers.size());
        registrations.entrySet().stream().forEach(regObj -> {
            List<DistAgentRegisterRow> allAgentsInRegistration = regObj.getValue().getAgents();
            log.info("Get other agents from registrations, agent: " + parentAgent.getAgentGuid() + ", registration: " + regObj.getKey() +", allAgents: " + allAgentsInRegistration.size());
            allAgentsInRegistration.stream().forEach(agentFromRegistration -> {
                AgentObject someAgent = agents.get(agentFromRegistration.getAgentguid());
                if (someAgent == null) {
                    someAgent = new AgentObject(agentFromRegistration);
                    log.info("Adding NEW agent from registration TO current Agent: " + parentAgent.getAgentGuid() + ", connected Agent: " + agentFromRegistration.getAgentguid() + ", active: " + agentFromRegistration.getIsactive() + ", Agents count: " + agents.size());
                    agents.put(agentFromRegistration.getAgentguid(), someAgent);
                }
                someAgent.update(agentFromRegistration, regObj.getKey());
            });
        });
        checkCount.incrementAndGet();
        log.info("AFTER check connected agents for agent: " + parentAgent.getAgentGuid() + ", current count: " + agents.size() + ", registrations: " + registrations.size() + ", registeredServers: " + registeredServers.size());
    }
    public void removeInactiveAgents() {
        long inactivateBeforeSecondsAgo = getConfig().getPropertyAsLong(DistConfig.AGENT_INACTIVATE_AFTER, DistConfig.AGENT_INACTIVATE_AFTER_DEFAULT_VALUE)/1000;
        LocalDateTime inactivateBeforeDate = LocalDateTime.now().minusSeconds(inactivateBeforeSecondsAgo);
        long deleteBeforeSecondsAgo = getConfig().getPropertyAsLong(DistConfig.AGENT_DELETE_AFTER, DistConfig.AGENT_DELETE_AFTER_DEFAULT_VALUE)/1000;
        LocalDateTime deleteBeforeDate = LocalDateTime.now().minusSeconds(deleteBeforeSecondsAgo);
        log.info("Inactivate agents that have no ping for last " + (inactivateBeforeSecondsAgo) + " seconds, remove inactive agents with ping before " + deleteBeforeSecondsAgo + " seconds ago");
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
    /** register this agent to all available connectors
     * this is registering agent itself,  */
    private void registerToAll() {
        var agents = getAgents();
        register = new AgentRegister(parentAgent.getAgentGuid(), parentAgent.getAgentSecret(),
                DistUtils.getCurrentHostName(), DistUtils.getCurrentHostAddress(),
                parentAgent.getAgentApi().getPort(), parentAgent.getCreateDate(), agents, true);
        List<DistAgentServiceRow> serviceRows = parentAgent.getAgentServices().getServiceRows();
        try {
            synchronized (registrations) {
                log.info("Registering this agent to all registration objects, GUID: " + parentAgent.getAgentGuid() + " on host " + register.getHostName() + ", services: " + serviceRows.size() + " to all registrations: " + registrations.size());
                registrations.values().stream().forEach(regSrv -> {
                    regSrv.agentRegister(register);
                    serviceRows.forEach(serviceRow -> {
                        regSrv.registerService(serviceRow);
                    });
                });
            }
        } catch (Exception ex) {
            log.warn("Cannot register agent " + parentAgent.getAgentGuid() + " to registration services, reason: " + ex.getMessage(), ex);
            parentAgent.getAgentIssues().addIssue("AgentRegistrationsImpl.registerToAll", ex);
        }
    }

    /** unregister agent and all servers from all registration services */
    private void unregisterFromAll() {
        try {
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
            parentAgent.getAgentIssues().addIssue("AgentRegistrationsImpl.unregisterFromAll", ex);
        }
    }

}
