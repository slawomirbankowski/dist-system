package com.distsystem.agent.servers;

import com.distsystem.api.DaoParams;
import com.distsystem.api.ServiceObjectParams;
import com.distsystem.api.enums.DistClientType;
import com.distsystem.api.DistConfig;
import com.distsystem.api.DistMessage;
import com.distsystem.dao.DaoKafkaBase;
import com.distsystem.base.ServerBase;
import com.distsystem.interfaces.AgentServer;
import com.distsystem.interfaces.KafkaReceiver;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.stream.Collectors;

/** Kafka to exchange messages between Agents */
public class AgentKafkaServer extends ServerBase implements AgentServer {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(AgentKafkaServer.class);

    private String brokers;
    private String topicTemplate;
    private DaoKafkaBase daoKafka;
    int numPartitions = 1;
    short replicationFactor = 1;
    private String agentDedicatedTopicName;
    private String broadcastTopicName;
    /** */
    public AgentKafkaServer(ServiceObjectParams params) {
        super(params);
        initialize();
    }

    /** count objects in this agentable object including this object */
    public long countObjectsAgentable() {
        return 7L;
    }
    public void initialize() {
        try {
            brokers = getConfigProperty(DistConfig.BROKERS, DistConfig.AGENT_CONNECTORS_SERVER_KAFKA_BROKERS_DEFAULT_VALUE); // parentAgent.getConfig().getProperty(DistConfig.AGENT_CONNECTORS_SERVER_KAFKA_BROKERS, DistConfig.AGENT_CONNECTORS_SERVER_KAFKA_BROKERS_DEFAULT_VALUE);
            topicTemplate = getConfigProperty(DistConfig.TOPIC, "dist-agent-"); //parentAgent.getConfig().getProperty(DistConfig.AGENT_CONNECTORS_SERVER_KAFKA_TOPIC, "dist-agent-");
            agentDedicatedTopicName = topicTemplate + parentAgent.getAgentShortGuid();
            String consumerGroup = topicTemplate + parentAgent.getAgentShortGuid();
            var tagTopicNames = parentAgent.getAgentTags().stream().map(tag -> ""+tag).collect(Collectors.toSet());
            broadcastTopicName = topicTemplate + "broadcast";
            log.info("Initializing Kafka server with brokers: " + brokers + ", template: " + topicTemplate+ ", consumer group: " + consumerGroup);
            var params = DaoParams.kafkaParams(brokers, numPartitions, replicationFactor);
            daoKafka = parentAgent.getAgentDao().getOrCreateDaoOrError(DaoKafkaBase.class, params);
            log.info("Initializing Kafka server, creating all topics, dedicated topic: " + agentDedicatedTopicName + ", broadcast topic: " + broadcastTopicName + ", tags topics: " + tagTopicNames);
            daoKafka.usedByComponent(this);
            daoKafka.createTopics(Set.of(agentDedicatedTopicName, broadcastTopicName));
            daoKafka.createTopics(tagTopicNames);
            daoKafka.createKafkaConsumer(agentDedicatedTopicName, consumerGroup, this::receiveMessages);
            daoKafka.createKafkaConsumer(broadcastTopicName, consumerGroup, this::receiveMessages);
            log.info("Started Kafka server using Brokers: " + brokers +", agent topic: " + agentDedicatedTopicName + ", agent: " + getParentAgentGuid());
        } catch (Exception ex) {
            log.info("Cannot start Kafka server, reason: " + ex.getMessage());
            parentAgent.getIssues().addIssue("AgentKafkaServer.initializeServer", ex);
        }
    }

    /** read configuration and re-initialize this component */
    public boolean componentReinitialize() {
        // TODO: reinitialize this component
        return true;
    }
    /** receive message from Kafka */
    public Boolean receiveMessages(KafkaReceiver receiver, ConsumerRecord<String, String> kafkaMsg) {
        try {
            DistMessage msg = (DistMessage)parentAgent.getSerializer().deserializeFromString(DistMessage.class.getName(), kafkaMsg.value());
            receivedMessages.incrementAndGet();
            log.info("Receive message in Kafka server for agent: " + parentAgent.getAgentGuid() + ", message: " + msg.toString());
            if (msg.isSystem()) {
                // parseWelcomeMessage(msg);
            } else {
                parentAgent.getServices().receiveMessage(msg);
            }
            return true;
        } catch (Exception ex) {
            parentAgent.getIssues().addIssue("AgentKafkaServer.receiveMessages", ex);
            return false;
        }
    }
    /** get type of clients to be connected to this server */
    public DistClientType getClientType() {
        return DistClientType.kafka;
    }
    /** get port of this server */
    public int getPort() {
        return 9092;
    }

    /** get URL of this server */
    public String getUrl() {
        return brokers;
    }

    protected void onClose() {
        try {
            log.info("Try to close Kafka server for Agent: " + parentAgent.getAgentGuid());
            daoKafka.close();
        } catch (Exception ex) {
            addIssueToAgent("onClose", ex);
        }
    }
}
