package com.distsystem.agent.registrations;

import com.distsystem.agent.AgentInstance;
import com.distsystem.api.*;
import com.distsystem.base.RegistrationBase;
import com.distsystem.base.dtos.DistAgentRegisterRow;
import com.distsystem.base.dtos.DistAgentServerRow;
import com.distsystem.base.dtos.DistAgentServiceRow;
import com.distsystem.dao.DaoKafkaBase;
import com.distsystem.interfaces.KafkaReceiver;
import com.distsystem.utils.JsonUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/** *
 * Registration service using Kafka - there are several topics to register agents, servers, services, issues, pings.
 */
public class RegistrationKafka extends RegistrationBase {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(RegistrationKafka.class);
    /** DAO for Kafka to read/write messages from/into topics */
    private DaoKafkaBase kafkaDao;
    /** URLs of Kafka brokers */
    private String kafkaBrokers;

    /** all agents from Kafka */
    private final Map<String, DistAgentRegisterRow> agents = new ConcurrentHashMap<>();
    /** all servers from Kafka */
    private final Map<String, DistAgentServerRow> servers = new ConcurrentHashMap<>();

    private String topicPrefix;
    private String registrationTopicName;
    private String serverTopicName;
    private String serviceTopicName;
    private String issueTopicName;

    public RegistrationKafka(AgentInstance parentAgent) {
        super(parentAgent);
    }
    /** run for initialization in classes */
    @Override
    protected void onInitialize() {
        kafkaBrokers = parentAgent.getConfig().getProperty(DistConfig.AGENT_REGISTRATION_KAFKA_BROKERS);
        topicPrefix = parentAgent.getConfig().getProperty(DistConfig.AGENT_REGISTRATION_KAFKA_TOPIC, "dist-agent-");
        log.info("Creating Kafka registration object with brokers: " + kafkaBrokers + ", topic prefix: " + topicPrefix);
        registrationTopicName = topicPrefix + "register";
        serverTopicName = topicPrefix + "server";
        serviceTopicName = topicPrefix + "service";
        issueTopicName = topicPrefix + "issue";
        try {
            kafkaDao = parentAgent.getAgentDao().getOrCreateDaoOrError(DaoKafkaBase.class, DaoParams.kafkaParams(kafkaBrokers, 1, (short)1));
            kafkaDao.usedByComponent(this);
            log.info("Creating Kafka needed topics using DAO: " + kafkaDao.getGuid() + ", consumers: " + kafkaDao.getConsumersCount());
            kafkaDao.createTopics(Set.of(registrationTopicName, serverTopicName, serviceTopicName, issueTopicName));
            log.info("Creating Kafka consumers for registration");
            kafkaDao.getOrCreateKafkaConsumer(registrationTopicName, parentAgent.getAgentGuid(), this::readMessageRegistration);
            kafkaDao.getOrCreateKafkaConsumer(serverTopicName, parentAgent.getAgentGuid(), this::readMessageServer);
            log.info("Registered to Kafka: " + kafkaBrokers + ", guid: " + kafkaDao.getGuid() + ", consumers: " + kafkaDao.getConsumersCount());
        } catch (Exception ex) {
            log.warn("Cannot connect registration to Kafka brokers: " + ex.getMessage(), ex);
        }
    }
    /** read registration messages from Kafka topic */
    public Boolean readMessageRegistration(KafkaReceiver receiver, ConsumerRecord<String, String> row) {
        log.info("READ REGISTRATION FROM KAFKA !!!!!!!!! "+ row.value() + ", agents: " + agents.size());
        DistAgentRegisterRow agentRow = JsonUtils.deserialize(row.value(), DistAgentRegisterRow.class);
        if (agentRow != null) {
            log.info("Adding new agent from Kafka registration, GUID: " + agentRow.agentguid + ", host: " + agentRow.hostname + ", active: " + agentRow.isactive + ", active: " + agentRow.isactive);

            agents.put(agentRow.getAgentguid(), agentRow);
            return true;
        } else {
            return false;
        }
    }

    /** read servers message from Kafka topic */
    public Boolean readMessageServer(KafkaReceiver receiver, ConsumerRecord<String, String> row) {
        log.debug("READ SERVER FROM KAFKA !!!!!!!!!" + row.value() +", servers: " + servers.size());
        DistAgentServerRow serverRow = JsonUtils.deserialize(row.value(), DistAgentServerRow.class);
        if (serverRow != null) {
            log.info("Adding new server from registration, GUID: " + serverRow.serverguid + ", type: " + serverRow.servertype + ", servers: " + servers.size());
            servers.put(serverRow.serverguid, serverRow);
            return true;
        } else {
            return false;
        }
    }

    /** get custom parameters for this registration */
    public Map<String, Object> getRegistrationCustomParameters() {
        return Map.of("daoGuid", kafkaDao.getGuid(),
            "agents", agents.size(),
                "servers", servers.size(),
                "daoConsumers", kafkaDao.getConsumersCount(),
                "topics", kafkaDao.getTopics(topicPrefix),
                "daoConsumerKeys", kafkaDao.getConsumersKeys(),
                "daoConnected", kafkaDao.isConnected());
    }
    @Override
    protected boolean onIsConnected() {
        return false;
    }
    @Override
    protected AgentConfirmation onAgentRegister(AgentRegister register) {
        log.info("Registering AGENT AT Kafka registration, GUID: " + register.getAgentGuid() + ", host: " + register.getHostName());

        kafkaDao.send(registrationTopicName, register.getAgentGuid(),  JsonUtils.serialize(register.toAgentRegisterRow()));
        return new AgentConfirmation(register.getAgentGuid(), true, false, 0, List.of());
    }
    protected AgentConfirmation onAgentUnregister(AgentRegister register) {
        register.updatePingDate();
        kafkaDao.send(registrationTopicName, register.getAgentGuid(),  JsonUtils.serialize(register.toAgentRegisterRow()));
        return new AgentConfirmation(register.getAgentGuid(), true, false, 0, List.of());
    }
    @Override
    protected AgentPingResponse onAgentPing(AgentPing ping) {
        kafkaDao.send(registrationTopicName, ping.getAgentGuid(),  JsonUtils.serialize(ping.getRegister().toAgentRegisterRow()));
        return new AgentPingResponse(ping.getAgentGuid());
    }
    /** remove active agents without last ping date for more than X minutes */
    public boolean removeInactiveAgents(LocalDateTime beforeDate) {
        agents.values().stream().filter(a -> a.isactive == 1 && a.getLastpingdate().isBefore(beforeDate)).collect(Collectors.toList());
        return true;
    }
    /** remove inactive agents with last ping date for more than X minutes */
    public boolean deleteInactiveAgents(LocalDateTime beforeDate) {
        //

        return true;
    }
    /** get normalized URL for this registration */
    public String getUrl() {
        return kafkaBrokers;
    }
    /** add issue for registration */
    public void addIssue(DistIssue issue) {
        kafkaDao.send(issueTopicName, issue.getGuid(),  JsonUtils.serialize(issue.toRow()));
    }
    /** register server for communication */
    public void addServer(DistAgentServerRow serv) {
        log.info("Add new server to Kafka registration, server: " + serv.serverguid + ", type: " + serv.servertype + ", topic: " + serverTopicName);
        kafkaDao.send(serverTopicName, serv.serverguid,  JsonUtils.serialize(serv));
    }
    /** unregister server for communication */
    public void unregisterServer(DistAgentServerRow serv) {
        serv.deactivate();
        kafkaDao.send(serverTopicName, serv.serverguid,  JsonUtils.serialize(serv));
    }
    /** get all communication servers */
    public List<DistAgentServerRow> getServers() {
        return servers.values().stream().filter(s -> s.isactive==1).collect(Collectors.toList());
    }
    /** ping given server by GUID */
    public boolean serverPing(DistAgentServerRow serv) {
        kafkaDao.send(serverTopicName, serv.serverguid,  JsonUtils.serialize(serv));
        return true;
    }
    /** set active servers with last ping date before given date as inactive */
    public boolean serversCheck(LocalDateTime inactivateBeforeDate, LocalDateTime deleteBeforeDate) {
        // TODO: implement checking servers
        return false;
    }

    /** register service */
    public void registerService(DistAgentServiceRow service) {

    }

    /** get agents from registration services */
    public List<DistAgentRegisterRow> getAgents() {
        return new ArrayList<>(agents.values());
    }

    /** close current connector */
    @Override
    protected void onClose() {
        kafkaDao.stopConsumer(registrationTopicName);
        kafkaDao.stopConsumer(serverTopicName);
        kafkaDao.stopConsumer(serviceTopicName);
        kafkaDao.stopConsumer(issueTopicName);

    }
}
