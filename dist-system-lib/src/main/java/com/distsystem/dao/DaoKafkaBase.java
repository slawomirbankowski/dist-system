package com.distsystem.dao;

import com.distsystem.api.DaoParams;
import com.distsystem.base.DaoBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentComponent;
import com.distsystem.interfaces.KafkaReceiver;
import com.distsystem.utils.DistUtils;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/** base class for any JDBC based DAO */
public class DaoKafkaBase extends DaoBase implements AgentComponent {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(DaoKafkaBase.class);

    /** brokers of Kafka */
    private final String brokers;
    private int numPartitions = 1;
    private short replicationFactor = 1;
    private String clientId;
    /** */
    private AdminClient adminClient;
    /** all producers to write into Kafka topics */
    private KafkaProducer<String, String> producer;
    /** all consumers by topic name */
    private final Map<String, KafkaReceiverImpl> consumersByTopic = new ConcurrentHashMap<>();
    /** number of sent messages using producer */
    private final AtomicLong sentMessages = new AtomicLong();

    /** creates new DAO to JDBC database */
    public DaoKafkaBase(DaoParams params, Agent agent) {
        super(params, agent);
        this.brokers = resolve(params.getBrokers());
        this.numPartitions = params.getNumPartitions();
        this.replicationFactor = params.getReplicationFactor();
        this.clientId = parentAgent.getAgentGuid();
        onInitialize();
    }

    /** count objects in this agentable object including this object */
    public long countObjectsAgentable() {
        return 2L;
    }
    /** returns true if DAO is connected */
    public boolean isConnected() {
        return true;
    }

    /** test DAO and returns items */
    public Map<String, Object> testDao() {
        return Map.of("isConnected", isConnected(), "url", getUrl(), "className", this.getClass().getName());
    }
    /** get URL of this DAO */
    public String getUrl() {
        return brokers;
    }
    /** */
    public void onInitialize() {
        try {
            log.info("Connecting to Kafka, BROKERS=" + resolve(brokers));
            adminClient = createAdminClient();
            producer = createKafkaProducer();
            log.info("Connected to Kafka");
        } catch (Exception ex) {
            log.info("Cannot connect to Kafka at URL:" + brokers + ", reason: " + ex.getMessage(), ex);
            parentAgent.addIssue("DaoKafkaBase.onInitialize", ex);
        }
    }

    /** read configuration and re-initialize this component */
    public boolean componentReinitialize() {
        // TODO: implement reinitialization
        return true;
    }

    /** get all topics */
    public Collection<String> getDaoStructures() {
        return getTopics();
    }

    /** */
    private Properties commonKafkaProperties() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        return props;
    }
    /** */
    private void producerKafkaProperties(Properties props) {
        props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, "1");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringSerializer.class.getName());
    }
    /** */
    private void consumerKafkaProperties(Properties props) {
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringDeserializer.class.getName());
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "10");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
    }
    /** create admin client */
    private AdminClient createAdminClient() {
        var props = commonKafkaProperties();
        return AdminClient.create(props);
    }
    /** get all possible topics */
    public Set<String> getTopics() {
        return getTopics("");
    }
    /** get all topics starts with given String */
    public Set<String> getTopics(String starts) {
        try {
            return adminClient.listTopics().names().get().stream()
                    .filter(x -> x.startsWith(starts))
                    .collect(Collectors.toSet());
        } catch (Exception ex) {
            parentAgent.addIssue("DaoKafkaBase.getTopics", ex);
            return Set.of();
        }
    }

    /** get number of consumers */
    public int getConsumersCount() {
        return consumersByTopic.size();
    }
    /** get keys for consumers */
    public Set<String> getConsumersKeys() {
        return consumersByTopic.keySet();
    }
    /** search of topics */
    public Set<String> searchTopics(String contains) {
        return getTopics().stream().filter(t -> t.contains(contains)).collect(Collectors.toSet());
    }
    /** create topics for given names */
    public boolean createTopic(String topicName) {
        return createTopics(Set.of(topicName));
    }
    /** filter topics in set and returns only non-existing topics, all existing topics are removed from the set */
    public Set<String> getNonExistingTopics(Set<String> topicNames) {
        var setCopy = new HashSet<String>(topicNames);
        setCopy.removeAll(getTopics());
        return setCopy;
    }
    /** create topics for given names */
    public boolean createTopics(Set<String> topicNames) {
        return createTopics(topicNames, numPartitions, replicationFactor);
    }
    /** create topics for given names */
    public boolean createTopics(Set<String> topicNames, int np, short fr) {
        try {
            if (topicNames.isEmpty()) {
                log.info("No topics to be created");
                return true;
            } else {
                Set<String> nonExistingTopics = getNonExistingTopics(topicNames);
                log.info("Try to create topics for names: " + nonExistingTopics + ", partitions: " + np + ", replication: " + fr + ", brokers: " + brokers);
                List<NewTopic> topicsToCreate = nonExistingTopics.stream().map(tn -> new NewTopic(tn, np, fr)).collect(Collectors.toList());
                CreateTopicsResult createResult = adminClient.createTopics(topicsToCreate);
                createResult.all().get(10, TimeUnit.SECONDS);
                return true;
            }
        } catch (Exception ex) {
            log.warn("Cannot create topics with partitions: " + + np + ", replication: " + fr + ", brokers: " + brokers + ", reason: " + ex.getMessage(), ex);
            parentAgent.addIssue("DaoKafkaBase.createTopics", ex);
            return false;
        }
    }

    /** delete topics for given names */
    public boolean deleteTopics(Set<String> topicNames) {
        try {
            log.info("Try to delete topics for names: " + topicNames);
            adminClient.deleteTopics(topicNames);
            return true;
        } catch (Exception ex) {
            log.warn("Cannot delete topics, reason: " + ex.getMessage(), ex);
            parentAgent.addIssue("DaoKafkaBase.deleteTopics", ex);
            return false;
        }
    }
    /** create new Kafka producer to write messages into topics */
    private KafkaProducer<String, String> createKafkaProducer() {
        Properties props = commonKafkaProperties();
        producerKafkaProperties(props);
        log.info("Creating new Kafka producer to send messages to brokers: " + brokers + ", client: " + clientId);
        return new KafkaProducer<String, String>(props);
    }

    /** get existing Kafka Consumer or create new one */
    public KafkaReceiverImpl getOrCreateKafkaConsumer(String topicName, String consumerGroup, java.util.function.BiFunction<KafkaReceiver, ConsumerRecord<String, String>, Boolean> onReadMessage) {
        synchronized (consumersByTopic) {
            KafkaReceiverImpl rec = consumersByTopic.get(topicName);
            if (rec == null) {
                rec = createKafkaConsumer(topicName, consumerGroup, onReadMessage);
            }
            return rec;
        }
    }
    /** create new Kafka Consumer and replace current one */
    public KafkaReceiverImpl createKafkaConsumer(String topicName, String consumerGroup, java.util.function.BiFunction<KafkaReceiver, ConsumerRecord<String, String>, Boolean> onReadMessage) {
        log.info("Create NEW Kafka  consumer for topic: " + topicName + ", consumer: " + consumerGroup + ", brokers: " + brokers);
        Properties props = commonKafkaProperties();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroup);
        consumerKafkaProperties(props);
        var consumer = new KafkaConsumer<String, String>(props);
        consumer.subscribe(Set.of(topicName));
        KafkaReceiverImpl receiver = new KafkaReceiverImpl(this, topicName, consumerGroup, consumer, onReadMessage);
        Thread thread = new Thread(receiver);
        receiver.setThread(thread);
        thread.setDaemon(true);
        thread.start();
        parentAgent.getThreads().registerThread(this, thread, "kafka-receiver-" + topicName);
        KafkaReceiverImpl oldReceiver = consumersByTopic.put(topicName, receiver);
        if (oldReceiver != null) {
            oldReceiver.close();
        }
        return receiver;
    }
    /** stop consumer for given topic name */
    public boolean stopConsumer(String topicName) {
        KafkaReceiverImpl consumer = consumersByTopic.get(topicName);
        if (consumer != null) {
            log.info("Closing Kafka consumer for topic: " + topicName + ", brokers: " + brokers);
            consumer.close();
            return true;
        } else {
            return false;
        }
    }
    /** set one message using current Kafka producer */
    public Future<RecordMetadata> send(String topicName, String key, String value) {
        try {
            if (!closed) {
                Future<RecordMetadata> sentInfo =  producer.send(new ProducerRecord<String, String>(topicName, key, value));
                producer.flush();
                sentMessages.incrementAndGet();
                return sentInfo;
            } else {
                log.warn("Cannot send message to closed Kafka DAO: "+ getGuid() +", topic: " + topicName);
                return null;
            }
        } catch (Exception ex) {
            log.warn("Cannot send message to topic: " + topicName +", reason: " + ex.getMessage(), ex);
            return null;
        }
    }
    public boolean closeDao() {
        log.info("Closing Kafka DAO for brokers: " + brokers + ", consumers: " + consumersByTopic.size());
        try {
            closed = true;
            adminClient.close();
            producer.close();
            consumersByTopic.values().stream().forEach(kc -> kc.close());
            return true;
        } catch (Exception ex) {
            log.warn("Cannot close Kafka DAO, brokers: " + brokers + ", consumers: " + consumersByTopic.size() +", reason: " + ex.getMessage(), ex);
            return false;
        }
    }
    /** close current Kafka producer, admin client, consumers */
    protected void onClose() {
        closeDao();
    }

}

/** separated Thread to receive data from Kafka topic */
class KafkaReceiverImpl implements Runnable, KafkaReceiver {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(KafkaReceiverImpl.class);
    /** global unique ID of this receiver  */
    private String kafkaReceiverGuid = DistUtils.generateCustomGuid("KAFKA_RECEIVER_");

    /** created date and time */
    private LocalDateTime createDate = LocalDateTime.now();
    /** parent DAO for Kafka */
    private DaoKafkaBase dao;
    /** name of topic to receive data from */
    private final String topicName;
    /** name of consumer group to get data from that topic */
    private final String consumerGroup;
    /** consumer to read messages from Kafka */
    private final KafkaConsumer<String, String> consumer;
    /** */
    private java.util.function.BiFunction<KafkaReceiver, ConsumerRecord<String, String>, Boolean> onReadMessage;
    /** thread */
    private Thread thread;
    /** if this receiver is still working  */
    private boolean working = true;
    private final AtomicLong totalIterations = new AtomicLong();

    /** total number of messages read */
    private final AtomicLong totalRead = new AtomicLong();
    /** total number of messages confirmed by parent service */
    private final AtomicLong totalConfirmed = new AtomicLong();

    /** */
    public KafkaReceiverImpl(DaoKafkaBase dao, String topicName, String consumerGroup, KafkaConsumer<String, String> consumer, java.util.function.BiFunction<KafkaReceiver, ConsumerRecord<String, String>, Boolean> onReadMessage) {
        this.dao = dao;
        this.topicName = topicName;
        this.consumerGroup = consumerGroup;
        this.consumer = consumer;
        this.onReadMessage = onReadMessage;
    }
    /** */
    public void setThread(Thread thread) {
        this.thread = thread;
    }
    public DaoKafkaBase getDao() {
        return dao;
    }
    public String getTopicName() {
        return topicName;
    }
    public KafkaConsumer<String, String> getConsumer() {
        return consumer;
    }
    public  java.util.function.BiFunction<KafkaReceiver, ConsumerRecord<String, String>, Boolean> getOnReadMessage() {
        return onReadMessage;
    }
    /** */
    public Thread getThread() {
        return thread;
    }
    /** */
    public boolean isWorking() {
        return working;
    }
    /** get consumer group to read messages from Kafka */
    public String getConsumerGroup() {
        return consumerGroup;
    }
    /** get date and time of creation */
    public LocalDateTime getCreateDate() {
        return createDate;
    }
    /** get total read */
    public long getTotalRead() {
        return totalRead.get();
    }
    /** get total confirmed */
    public long getTotalConfirmed() {
        return totalConfirmed.get();
    }
    @Override
    public void run() {
        while (working) {
            try {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(2));
                records.iterator().forEachRemaining(rec -> {
                    totalRead.incrementAndGet();
                    var confirmed = onReadMessage.apply(this, rec);
                    if (confirmed) {
                        totalConfirmed.incrementAndGet();
                    }
                });
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                working = false;
            } catch (Exception ex) {
                log.warn("Exception while running receiver thread for Kafka topic: " + topicName +  ", reason: " + ex.getMessage());
                dao.getAgent().addIssue("KafkaReceiver.run", ex);
            }
            totalIterations.incrementAndGet();
        }
    }
    /** close this receiver - set working as false */
    public void close() {
        working = false;
        try {
            consumer.close();
            log.info("Kafka consumer closed for topic: " + topicName);
        } catch (Exception ex) {
            log.warn("Cannot close consumer, reason: " + ex.getMessage());
            dao.getAgent().addIssue("KafkaReceiver.close", ex);
        }
    }

}