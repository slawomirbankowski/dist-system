package com.distsystem.agent.registrations;

import com.distsystem.api.*;
import com.distsystem.api.dtos.*;
import com.distsystem.base.RegistrationBase;
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

    public RegistrationKafka(ServiceObjectParams params) {
        super(params);
    }


    /** count objects in this agentable object including this object */
    public long countObjectsAgentable() {
        return 9L;
    }
    /** run for initialization in classes */
    @Override
    protected void onInitialize() {
        kafkaBrokers = getConfigProperty(DistConfig.BROKERS, "localhost:9091"); // parentAgent.getConfig().getProperty(DistConfig.AGENT_REGISTRATION_OBJECT_KAFKA_BROKERS);
        topicPrefix = getConfigProperty(DistConfig.TOPIC, "dist-agent-"); //parentAgent.getConfig().getProperty(DistConfig.AGENT_REGISTRATION_OBJECT_KAFKA_TOPIC, "dist-agent-");
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
    /** read configuration and re-initialize this component */
    public boolean componentReinitialize() {
        // TODO: implement reinitialization
        return true;
    }

    /** read registration messages from Kafka topic */
    public Boolean readMessageRegistration(KafkaReceiver receiver, ConsumerRecord<String, String> row) {
        log.info("READ REGISTRATION FROM KAFKA !!!!!!!!! "+ row.value() + ", agents: " + agents.size());
        DistAgentRegisterRow agentRow = JsonUtils.deserialize(row.value(), DistAgentRegisterRow.class);
        if (agentRow != null) {
            log.info("Adding new agent from Kafka registration, GUID: " + agentRow.getAgentGuid() + ", host: " + agentRow.getHostName() + ", active: " + agentRow.getActive());

            agents.put(agentRow.getAgentGuid(), agentRow);
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
            log.info("Adding new server from registration, GUID: " + serverRow.getServerGuid() + ", type: " + serverRow.getServerType() + ", servers: " + servers.size());
            servers.put(serverRow.getServerGuid(), serverRow);
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
        agents.values().stream().filter(a -> a.getActive() == 1 && a.getLastPingDate().isBefore(beforeDate)).collect(Collectors.toList());
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
        log.info("Add new server to Kafka registration, server: " + serv.getServerGuid() + ", type: " + serv.getServerType() + ", topic: " + serverTopicName);
        kafkaDao.send(serverTopicName, serv.getServerGuid(),  JsonUtils.serialize(serv));
    }
    /** unregister server for communication */
    public void unregisterServer(DistAgentServerRow serv) {
        serv.deactivate();
        kafkaDao.send(serverTopicName, serv.getServerGuid(),  JsonUtils.serialize(serv));
    }
    /** get all communication servers */
    public List<DistAgentServerRow> getServers() {
        return servers.values().stream().filter(s -> s.getIsActive()==1).collect(Collectors.toList());
    }
    /** ping given server by GUID */
    public boolean serverPing(DistAgentServerRow serv) {
        kafkaDao.send(serverTopicName, serv.getServerGuid(),  JsonUtils.serialize(serv));
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

    /** get all shared storages */
    public List<DistAgentStorageRow> getStorages() {
        return List.of();
    }
    /** get all shared storage names */
    public List<String> getStorageNames() {
        return List.of();
    }
    /** get storage by name */
    public Optional<DistAgentStorageRow> getStorageByName(String storageName) {
        return Optional.empty();
    }
    /** add storage */
    public boolean addStorage(DistAgentStorageRow storage) {
        return false;
    }

    /** get all shared reports */
    public List<DistAgentReportRow> getReports() {
        return List.of();
    }
    /** get all shared report names */
    public List<String> getReportNames() {
        return List.of();
    }
    /** get report by name */
    public Optional<DistAgentReportRow> getReportByName(String reportName) {
        return Optional.empty();
    }
    /** add report */
    public boolean addReport(DistAgentReportRow report) {
        return false;
    }
    /** add report run */
    public boolean addReportRun(DistAgentReportRunRow reportRun) {
        return false;
    }

    /** get all monitors */
    public List<DistAgentMonitorRow> getMonitors() {
        return List.of();
    }
    /** get all monitor names */
    public List<String> getMonitorNames() {
        return List.of();
    }
    /** get monitor by name */
    public Optional<DistAgentMonitorRow> getMonitorByName(String reportName) {
        return Optional.empty();
    }
    /** add monitor */
    public boolean addMonitor(DistAgentMonitorRow monitor) {
        return false;
    }
    /** add monitor check */
    public boolean addMonitorCheck(DistAgentMonitorCheckRow monitorCheck) {
        return false;
    }

    /** get all notifications */
    public List<DistAgentNotificationRow> getNotifications() {
        return List.of();
    }
    /** add notification */
    public boolean addNotification(DistAgentNotificationRow notif) {
        return false;
    }

    /** get all schedules */
    public List<DistAgentScheduleRow> getSchedules() {
        return List.of();
    }
    /** add schedule */
    public boolean addSchedule(DistAgentScheduleRow schedule) {
        return false;
    }
    /** add schedule */
    public boolean addScheduleExecution(DistAgentScheduleRow schedule) {
        return false;
    }

    /** get all spaces */
    public List<DistAgentSpaceRow> getSpaces() {
        return List.of();
    }
    /** get all space names */
    public List<String> getSpaceNames() {
        return List.of();
    }
    /** get space by name */
    public Optional<DistAgentSpaceRow> getSpaceByName(String spaceName) {
        return Optional.empty();
    }
    /** add space */
    public boolean addSpace(DistAgentSpaceRow space) {
        return false;
    }
    /** remove space */
    public boolean removeSpace(String spaceName) {
        return false;
    }

    /** get all measures */
    public List<DistAgentMeasureRow> getMeasures() {
        return List.of();
    }
    /** get all measure names */
    public List<String> getMeasureNames() {
        return List.of();
    }
    /** add measure */
    public boolean addMeasure(DistAgentMeasureRow measure) {
        return false;
    }

    /** get all queries */
    public List<DistAgentQueryRow> getQueries() {
        return List.of();
    }
    /** get all query names */
    public List<String> getQueryNames() {
        return List.of();
    }
    /** get query by name */
    public Optional<DistAgentQueryRow> getQueryByName(String queryName) {
        return Optional.empty();
    }
    /** add or edit query */
    public boolean addQuery(DistAgentQueryRow measure) {
        return false;
    }

    /** add DAO row */
    public boolean addDao(DistAgentDaoRow dao) {
        return false;
    }

    /** get all global resources for given type */
    public List<DistAgentResourceRow> getResourcesForType(String resourceType) {
        return List.of();
    }
    /** get all global resource names for given type */
    public List<String> getResourceNamesForType(String resourceType) {
        return List.of();
    }
    /** get resource by name */
    public Optional<DistAgentResourceRow> getResourceByName(String queryName) {
        return Optional.empty();
    }
    /** add new resources */
    public boolean addResources(List<DistAgentResourceRow> resources) {
        return false;
    }

    /** get all settings */
    public List<DistAgentSettingRow> getSettings() {
        return List.of();
    }
    /** search for settings */
    public List<DistAgentSettingRow> searchSettings(String findStr) {
        return List.of();
    }
    /** add settings */
    public boolean addSettings(List<DistAgentSettingRow> settings) {
        return false;
    }

    /** get resource by name */
    public List<String> getScriptNames() {
        return List.of();
    }
    /** get script by name */
    public Optional<DistAgentScriptRow> getScriptForName(String scriptName) {
        return Optional.empty();
    }
    /** add script */
    public boolean addScript(DistAgentScriptRow script) {
        return false;
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
