package com.distsystem;

import com.distsystem.agent.AgentInstance;
import com.distsystem.api.*;
import com.distsystem.api.enums.DistEnvironmentType;
import com.distsystem.api.enums.DistSerializerTypes;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.Cache;
import com.distsystem.interfaces.DistSerializer;
import com.distsystem.interfaces.Resolver;
import com.distsystem.serializers.ComplexSerializer;
import com.distsystem.utils.JsonUtils;
import com.distsystem.utils.DistUtils;
import com.distsystem.utils.CustomSerializer;
import com.distsystem.utils.ResolverManager;
import com.distsystem.utils.resolvers.MapResolver;
import com.distsystem.utils.resolvers.MethodResolver;
import com.distsystem.utils.resolvers.PropertyResolver;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * factory class to create Agent with desired configuration
 * Agent is part of distributes system that has many services to be used:
 * - Cache - local cache object contains storages that keeps object for fast read and connects to other distributed cache through agent system
 * - Auth - authentication in distributed system
 * - Reports
 * - Semaphores
 * ...and much more
 *
 * */
public class DistFactory {

    /** local logger */
    private static final Logger log = LoggerFactory.getLogger(DistFactory.class);

    /** all created agents so far
     * key - Agent GUID
     * value - Agent object
     * */
    private static final Map<String, Agent> createdAgents = new HashMap<>();
    /** default agent for this JVM, just for easier use */
    private static Optional<Agent> defaultAgent = Optional.empty();

    /** get existing agent instance OR create one if it is not existing */
    public static synchronized Agent getDefaultAgent(DistConfig cfg) {
        return defaultAgent.orElseGet(() -> createDefaultAgent(cfg));
    }
    /** create default agent */
    public static synchronized Agent createDefaultAgent(DistConfig cfg) {
        if (defaultAgent.isPresent()) {
            return defaultAgent.get();
        }
        synchronized (defaultAgent) {
            Agent agent = DistFactory.buildConfigFactory(cfg).createAgentInstance();
            defaultAgent = Optional.of(agent);
            return agent;
        }
    }

    /** get Agent by GUID */
    public static Agent getAgent(String guid) {
        return createdAgents.get(guid);
    }
    /** register agent in this JVM */
    public static void registerAgent(Agent agent) {
        log.info("Register new agent in all local agents, guid: " + agent.getAgentGuid());
        createdAgents.put(agent.getAgentGuid(), agent);
    }
    /** set default agent to new value */
    public static void setDefaultAgent(Agent agent) {
        defaultAgent = Optional.of(agent);
    }

    /** create new instance of cache with given configuration */
    public static synchronized Cache createCacheInstance(DistConfig cfg, HashMap<String, Function<AgentEvent, String>> callbacks) {
        return DistFactory.buildConfigFactory(cfg)
                .withCallbacks(callbacks)
                .createCacheInstance();
    }
    /** * create new instance of cache with properties */
    public static Cache createCacheInstance(DistConfig cacheCfg) {
        return DistFactory
                .buildConfigFactory(cacheCfg)
                .createCacheInstance();
    }
    /** create new instance of cache with properties */
    public static Cache createCacheInstance(Properties cacheProps) {
        return DistFactory.buildPropertiesFactory(cacheProps)
                .createCacheInstance();
    }

    /** create new empty config for distributed cache*/
    public static DistConfig buildEmptyConfig() {
        return DistConfig.buildEmptyConfig();
    }

    /** build empty factory to fill with properties, callbacks with methods like:
     *  withProperty, withName, withPort, withStorageHashMap, withStorageElasticsearch, ...
     * cache instance could be created from factory */
    public static DistFactory buildEmptyFactory() {
        return new DistFactory();
    }

    /** build default configuration with default name, HashMap as storage */
    public static DistFactory buildDefaultFactory() {
        return DistFactory
                .buildEmptyFactory()
                .withUniverseNameDefault()
                .withAgentNameGenerated()
                .withCommonProperties()
                .withSerializerDefault()
                .withWebApiDefaultPort()
                .withServerSocketDefaultPort()
                .withServerDatagramPortDefaultValue()
                .withServerHttpPortDefault()
                .withCacheStorageHashMap()
                .withTimerStorageClean(30000)
                .withTimerRegistrationPeriod(30000)
                .withTimerServerPeriod(30000)
                .withMaxIssues(DistConfig.AGENT_CACHE_ISSUES_MAX_COUNT_VALUE)
                .withMaxEvents(DistConfig.AGENT_CACHE_EVENTS_MAX_COUNT_VALUE)
                .withCacheMaxObjectsAndItems(DistConfig.AGENT_CACHE_MAX_LOCAL_OBJECTS_VALUE, DistConfig.AGENT_CACHE_MAX_LOCAL_ITEMS_VALUE)
                .withEnvironmentVariables();
    }

    /** build factory based on properties */
    public static DistFactory buildPropertiesFactory(Map initialFactoryProperties) {
        return DistFactory
                .buildEmptyFactory()
                .withMap(initialFactoryProperties);
    }
    /** build factory from given configuration */
    public static DistFactory buildConfigFactory(DistConfig cacheCfg) {
        return buildPropertiesFactory(cacheCfg.getProperties());
    }

    /** unique identifier for factory */
    private final String distFactoryGuid = DistUtils.generateCustomTimeGuid("factory");
    /** cache properties for factory */
    private final Properties props = new Properties();
    /** callbacks for events - methods (values) to call when there is event of given type (keys) */
    private final HashMap<String, Function<AgentEvent, String>> callbacks = new HashMap<>();
    /** all serializers assigned to class name
     * key = full name of class to be serialized
     * value = serializer to do the work */
    private final HashMap<String, DistSerializer> serializers = new HashMap<>();
    /** cache policy */
    private CachePolicy policy = CachePolicyBuilder.empty().create();
    /** resolver for configuration values */
    private final ResolverManager resolver = new ResolverManager()
            .addResolver(new PropertyResolver(props))
            .addResolver(new MapResolver(System.getenv()));
    /** all DAOs parameters by name to create DAO in Agent */
    private final Map<String, DaoParams> daos = new HashMap<>();

    /** factory is just creating managers */
    private DistFactory() {
    }
    /** extract configuration from current factory */
    public DistConfig extractConfig() {
        return DistConfig.buildConfig(props);
    }

    /** create instance of cache from current factory using properties and callbacks
     * this is creating Agent and Agent is creating Cache by get method
     * */
    public Cache createCacheInstance() {
        return createAgentInstance().getCache();
    }
    /** create instance of agent from current factory using properties and callbacks
     * This is just creating Agent itself
     * */
    public Agent createAgentInstance() {
        Runtime rt = Runtime.getRuntime();
        long memoryBefore = rt.maxMemory()-rt.freeMemory();
        log.info("New Agent to be created...., factory GUID: " + distFactoryGuid+ ", free: " + rt.freeMemory() + ", max: " + rt.maxMemory() + ", total: " +rt.totalMemory());
        long startTime = System.currentTimeMillis();
        DistConfig config = new DistConfig(props, resolver);
        AgentInstance agent = new AgentInstance(config, callbacks, serializers, policy, daos);
        DistFactory.registerAgent(agent);
        agent.initializeAgent();
        long totalTime = System.currentTimeMillis() - startTime;
        long memoryAfter = rt.maxMemory()-rt.freeMemory();
        long memoryUsed = memoryAfter-memoryBefore;
        log.info("New Agent CREATED AND INITIALIZED!!! guid: " + agent.getAgentGuid() +", services: " + agent.getServices().getServicesCount() + ", totalTime: " + totalTime + "ms, free: "  + rt.freeMemory() + ", max: " + rt.maxMemory() + ", total: " +rt.totalMemory() + ", used: " + memoryUsed);
        log.info("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        return agent;
    }
    /** set type and name of the environment */
    public DistFactory withEnvironment(DistEnvironmentType envType, String envName) {
        log.debug("Set environment for DistFactory, factory GUID: " + distFactoryGuid+ ", envType:" + envType.name());
        props.setProperty(DistConfig.AGENT_ENVIRONMENT_TYPE, envType.name());
        props.setProperty(DistConfig.AGENT_ENVIRONMENT_NAME, envName);
        return this;
    }
    /** set type of the environment */
    public DistFactory withEnvironment(DistEnvironmentType envType) {
        return withEnvironment(envType, envType.name());
    }

    /** add all ENV variables to agent configuration */
    public DistFactory withEnvironmentVariables() {
        log.debug("Adding ENV variables for DistFactory, factory GUID: " + distFactoryGuid+ ", ENV size:" + System.getenv().size());
        for (Map.Entry<String, String> e : System.getenv().entrySet()) {
            props.setProperty(e.getKey(), e.getValue());
        }
        return this;
    }
    /** add new command line arguments to Dist properties */
    public DistFactory withCommandLineArguments(String[] args) {
        log.debug("Adding CMD line arguments for DistFactory, guid: " + distFactoryGuid + ", arguments: " + args.length);
        String currentProperty = null;
        List<String> currentValues = new LinkedList<>();
        for (int i=0; i<args.length; i++) {
            String currArg = args[i];
            if (currArg.startsWith("--")) {
                if (currentProperty != null && !currentValues.isEmpty()) {
                    props.put(currentProperty, String.join(",", currentValues));
                }
                currentProperty = currArg.substring(2);
                currentValues.clear();
            } else {
                currentValues.add(currArg);
            }
        }
        return this;
    }
    /** add friendly name for this distributed system - it would be any name that would be visible in logs, via REST endpoints */
    public DistFactory withUniverseName(String name) {
        log.debug("Set universe name to DistFactory, guid: " + distFactoryGuid + ", name: " + name);
        props.setProperty(DistConfig.AGENT_UNIVERSE, name);
        return this;
    }
    /** add default name for Distributed system*/
    public DistFactory withUniverseNameDefault() {
        return withUniverseName(DistConfig.AGENT_UNIVERSE_NAME_DEFAULT);
    }
    /** add friendly name for this agent - it would be any name that would be visible in logs, via REST endpoints
     * name should be unique, but it is not a must */
    public DistFactory withAgentName(String name) {
        log.debug("Set agent name to DistFactory, guid: " + distFactoryGuid + ", name: " + name);
        props.setProperty(DistConfig.AGENT_NAME, name);
        return this;
    }
    /** generate agent name from host*/
    public DistFactory withAgentNameGenerated() {
        return withAgentName("A_" + DistUtils.getCurrentHostName() + "_" + DistUtils.generateShortGuid());
    }
    /** */
    public DistFactory withAgentParentApplication(String applicationName) {
        log.debug("Set application name to DistFactory, guid: " + distFactoryGuid + ", name: " + applicationName);
        props.setProperty(DistConfig.AGENT_PARENT_APPLICATION, applicationName);
        return this;
    }
    /** script with all agent properties */
    public DistFactory withScript(String multiLineSettingsScript) {
        log.debug("Set Script name to DistFactory, guid: " + distFactoryGuid + ", size: " + multiLineSettingsScript.length());
        Arrays.stream(multiLineSettingsScript.split("\\n")).forEach(line -> {
            DistUtils.splitBySeparationEqual(line, ";", '=', true).forEach(nameValue -> {
                props.setProperty(nameValue[0], nameValue[1]);
            });
        });
        return this;
    }

    /** add common properties for this cache/machine/agent/address/path */
    public DistFactory withCommonProperties() {
        log.debug("Add common properties to DistFactory, guid: " + distFactoryGuid + ", host: " + DistUtils.getCurrentHostName());
        props.setProperty(DistConfig.AGENT_GLOBAL_GUID, DistUtils.getGuid());
        props.setProperty(DistConfig.AGENT_HOST_NAME, DistUtils.getCurrentHostName());
        props.setProperty(DistConfig.AGENT_HOST_ADDRESS, DistUtils.getCurrentHostAddress());
        props.setProperty(DistConfig.AGENT_LOCATION_PATH, DistUtils.getCurrentLocationPath());
        return this;
    }

    /** add simple property */
    public DistFactory withProperty(String name, String value) {
        props.setProperty(name, value);
        log.debug("Add single property to DistFactory, guid: " + distFactoryGuid + ", name: " + name);
        return this;
    }
    public DistFactory withProperties(Properties initialFactoryProperties) {
        log.debug("Add many properties to DistFactory, guid: " + distFactoryGuid + ", size: " + initialFactoryProperties.size());
        for (Map.Entry<Object, Object> e: initialFactoryProperties.entrySet()) {
            props.setProperty(e.getKey().toString(), e.getValue().toString());
        }
        return this;
    }
    /** with map of properties */
    public DistFactory withMap(Map<String, String> initialFactoryProperties) {
        log.debug("Add Map with properties to DistFactory, guid: " + distFactoryGuid + ", size: " + initialFactoryProperties.size());
        for (Map.Entry<String, String> e: initialFactoryProperties.entrySet()) {
            props.setProperty(e.getKey(), e.getValue());
        }
        return this;
    }
    /** add JSON with properties */
    public DistFactory withJson(String jsonDefinition) {
        log.debug("Add JSON with properties map to DistFactory, guid: " + distFactoryGuid + ", size: " + jsonDefinition.length());
        Map<String, String> map = JsonUtils.deserialize(jsonDefinition, new TypeReference<Map<String, String>>() {});
        return withMap(map);
    }
    /** add properties from file with properties format */
    public DistFactory withPropertiesFile(String propertiesFile) {
        try {
            log.debug("Add file with properties map to DistFactory, guid: " + distFactoryGuid + ", fileName: " + propertiesFile);
            Properties propFromFile = new Properties();
            propFromFile.load(new java.io.FileReader(propertiesFile));
            props.putAll(propFromFile);
        } catch (FileNotFoundException ex) {
            log.warn("Cannot find properties file for name: " + propertiesFile);
        } catch (IOException ex) {
            log.warn("Cannot read properties file: " + propertiesFile);
        }
        return this;
    }
    /** load properties from URL in properties format */
    public DistFactory withPropertiesUrl(String urlWithProperties) {
        try {
            log.debug("Add URL with properties map to DistFactory, guid: " + distFactoryGuid + ", URL: " + urlWithProperties);
            Properties propFromUrl = new Properties();
            URL conn = new URL(urlWithProperties);
            propFromUrl.load(conn.openConnection().getInputStream());
            props.putAll(propFromUrl);
        } catch (IOException ex) {
            log.warn("Cannot read properties from URL: " + urlWithProperties);
        }
        return this;
    }

    /** with resolver to resolve values form String  */
    public DistFactory withResolver(Resolver r) {
        log.debug("Add resolver to DistFactory, guid: " + distFactoryGuid + ", resolver: " + r.getClass().getName());
        resolver.addResolver(r);
        return this;
    }
    public DistFactory withResolverFromMap(Map<String, String> map) {
        log.debug("Add resolver from Map to DistFactory, guid: " + distFactoryGuid + ", map size: " + map.size());
        resolver.addResolver(new MapResolver(map));
        return this;
    }
    public DistFactory withResolverFromMethod(Function<String, Optional<String>> method) {
        log.debug("Add resolver from Method to DistFactory, guid: " + distFactoryGuid + ", method class " + method.getClass().getName());
        resolver.addResolver(new MethodResolver(method));
        return this;
    }
    public DistFactory withResolverFromKeyValue(Map<String, String> map) {
        log.debug("Add resolver from KeyValue to DistFactory, guid: " + distFactoryGuid + ", map size: " + map.size());
        resolver.addResolver(new MapResolver(map));
        return withResolverFromMap(Map.of());
    }
    /** set tags to Agent for better identification */
    public DistFactory withAgentTags(Set<String> tags) {
        log.debug("Set tags name to DistFactory, guid: " + distFactoryGuid + ", tags: " + tags.size());
        props.setProperty(DistConfig.AGENT_TAGS, String.join(";", tags));
        return this;
    }

    /** define port for Web Api */
    public DistFactory withWebApiPort(int port) {
        log.debug("Set Web API port to DistFactory, guid: " + distFactoryGuid + ", port: " + port);
        props.setProperty(DistConfig.AGENT_API_PORT, ""+port);
        return this;
    }
    /** define port for Web Api */
    public DistFactory withWebApiPort(String port) {
        log.debug("Set Web API port to DistFactory, guid: " + distFactoryGuid + ", port: " + port);
        props.setProperty(DistConfig.AGENT_API_PORT, port);
        return this;
    }
    /** set Web API port to default value */
    public DistFactory withWebApiDefaultPort() {
        return withWebApiPort(DistConfig.AGENT_API_PORT_DEFAULT_VALUE);
    }

    /** set default serializers */
    public DistFactory withSerializerDefault() {
        log.debug("Set default serializer to DistFactory, guid: " + distFactoryGuid);
        props.setProperty(DistConfig.AGENT_SERIALIZER_DEFINITION, DistConfig.AGENT_SERIALIZER_DEFINITION_SERIALIZABLE_VALUE);
        serializers.putAll(ComplexSerializer.parseSerializers(DistConfig.AGENT_SERIALIZER_DEFINITION_SERIALIZABLE_VALUE));
        return this;
    }
    /** definition of serializer from String */
    public DistFactory withSerializer(String serializerDefinition) {
        log.debug("Set serializer to DistFactory, guid: " + distFactoryGuid + ", definition: " + serializerDefinition);
        serializers.putAll(ComplexSerializer.parseSerializers(serializerDefinition));
        props.setProperty(DistConfig.AGENT_SERIALIZER_DEFINITION, serializerDefinition);
        return this;
    }
    public DistFactory withSerializer(DistSerializerTypes serializer, String... className) {
        // TODO: add better way to define serializers - from classes and definitions and types???

        //serializers.put()
        //ComplexSerializer.createComplexSerializer()
        //serializers.putAll(ComplexSerializer.parseSerializers(serializerDefinition));
        //props.setProperty(DistConfig.SERIALIZER_DEFINITION, serializerDefinition);
        return this;
    }

    /** add serializer to Dist system */
    public DistFactory withSerializer(String className, DistSerializer ser) {
        serializers.put(className, ser);
        return this;
    }
    /** add custom serializations for this system
     * @param className name of class to be serialized or deserialized
     * @param serializeFunction function to serialize Object to String
     * @param deserializeFunction function to deserialize Class name with String to Object  */
    public DistFactory withSerializerCustom(String className, Function<Object, String> serializeFunction, BiFunction<String, String, Object> deserializeFunction) {
        serializers.put(className, new CustomSerializer(serializeFunction, deserializeFunction));
        return this;
    }

    /** add DAO for given name with JDBC url, driver, user and password */
    public DistFactory withDaoJdbc(String name, String url, String driver, String user, String pass) {
        log.debug("Set JDBC DAO to DistFactory, guid: " + distFactoryGuid + ", name: " + name + ", url: " + url);
        daos.put(name, DaoParams.jdbcParams(url, driver, user, pass));
        return this;
    }
    public DistFactory withDaoElastic(String name, String url, String user, String pass) {
        log.debug("Set Elasticsearch DAO to DistFactory, guid: " + distFactoryGuid + ", name: " + name + ", url: " + url);
        daos.put(name, DaoParams.elasticsearchParams(url, user, pass));
        return this;
    }
    public DistFactory withDaoKafka(String name, String brokers, String topicName, int numPartitions, short replicationFactor) {
        log.debug("Set Kafka DAO to DistFactory, guid: " + distFactoryGuid + ", name: " + name + ", brokers: " + brokers);
        daos.put(name, DaoParams.kafkaParams(brokers, numPartitions, replicationFactor));
        return this;
    }

    /** add registration method as JDBC */
    public DistFactory withRegistrationJdbc(String url, String driver, String user, String pass) {
        log.debug("Set JDBC Registration to DistFactory, guid: " + distFactoryGuid + ", url: " + url);
        props.setProperty(DistConfig.AGENT_REGISTRATION_OBJECT_JDBC_URL, url);
        props.setProperty(DistConfig.AGENT_REGISTRATION_OBJECT_JDBC_DRIVER, driver);
        props.setProperty(DistConfig.AGENT_REGISTRATION_OBJECT_JDBC_USER, user);
        props.setProperty(DistConfig.AGENT_REGISTRATION_OBJECT_JDBC_PASS, pass);
        return this;
    }

    /** add registration method as JDBC */
    public DistFactory withRegistrationJdbcFromEnv() {
        log.debug("Set JDBC Registration from ENV to DistFactory, guid: " + distFactoryGuid);
        props.setProperty(DistConfig.AGENT_REGISTRATION_OBJECT_JDBC_URL, "${JDBC_URL}");
        props.setProperty(DistConfig.AGENT_REGISTRATION_OBJECT_JDBC_DRIVER, "${JDBC_DRIVER}");
        props.setProperty(DistConfig.AGENT_REGISTRATION_OBJECT_JDBC_USER, "${JDBC_USER}");
        props.setProperty(DistConfig.AGENT_REGISTRATION_OBJECT_JDBC_PASS, "${JDBC_PASS}");
        return this;
    }
    /** add URL for Dist standalone application */
    public DistFactory withRegisterApplication(String cacheAppUrl) {
        log.debug("Set APP Registration to DistFactory, guid: " + distFactoryGuid + ", url: " + cacheAppUrl);
        props.setProperty(DistConfig.AGENT_CACHE_APPLICATION_URL, cacheAppUrl);
        return this;
    }
    /** add URL for Dist application */
    public DistFactory withRegisterApplicationDefaultUrl() {
        props.setProperty(DistConfig.AGENT_CACHE_APPLICATION_URL, DistConfig.AGENT_CACHE_APPLICATION_URL_DEFAULT_VALUE);
        return this;
    }
    /** add registration with Elasticsearch */
    public DistFactory withRegistrationElasticsearch(String url, String user, String pass) {
        log.debug("Set Elasticsearch Registration to DistFactory, guid: " + distFactoryGuid + ", url: " + url);
        props.setProperty(DistConfig.AGENT_REGISTRATION_OBJECT_ELASTICSEARCH_URL, url);
        props.setProperty(DistConfig.AGENT_REGISTRATION_OBJECT_ELASTICSEARCH_USER, user);
        props.setProperty(DistConfig.AGENT_REGISTRATION_OBJECT_ELASTICSEARCH_PASS, pass);
        return this;
    }
    public DistFactory withRegistrationElasticsearchFromEnvironment() {
        log.debug("Set Elasticsearch Registration from ENV to DistFactory, guid: " + distFactoryGuid);
        props.setProperty(DistConfig.AGENT_REGISTRATION_OBJECT_ELASTICSEARCH_URL, "${ELASTICSEARCH_URL}");
        props.setProperty(DistConfig.AGENT_REGISTRATION_OBJECT_ELASTICSEARCH_USER, "${ELASTICSEARCH_USER}");
        props.setProperty(DistConfig.AGENT_REGISTRATION_OBJECT_ELASTICSEARCH_PASS, "${ELASTICSEARCH_PASS}");
        return this;
    }
    /** add registration through Kafka topic */
    public DistFactory withRegistrationKafka(String brokers, String topicName, int numPartitions, short replicationFactor) {
        log.debug("Set Kafka Registration to DistFactory, guid: " + distFactoryGuid + ", brokers: " + brokers);
        props.setProperty(DistConfig.AGENT_REGISTRATION_OBJECT_KAFKA_BROKERS, brokers);
        props.setProperty(DistConfig.AGENT_REGISTRATION_OBJECT_KAFKA_TOPIC, topicName);
        props.setProperty(DistConfig.AGENT_REGISTRATION_OBJECT_KAFKA_PARTITIONS, ""+numPartitions);
        props.setProperty(DistConfig.AGENT_REGISTRATION_OBJECT_KAFKA_REPLICATION, ""+replicationFactor);
        return this;
    }
    /** add registration through Kafka topic */
    public DistFactory withRegistrationKafka(String brokers, String topicName, String numPartitions, String replicationFactor) {
        log.debug("Set Kafka Registration to DistFactory, guid: " + distFactoryGuid + ", brokers: " + brokers);
        props.setProperty(DistConfig.AGENT_REGISTRATION_OBJECT_KAFKA_BROKERS, brokers);
        props.setProperty(DistConfig.AGENT_REGISTRATION_OBJECT_KAFKA_TOPIC, topicName);
        props.setProperty(DistConfig.AGENT_REGISTRATION_OBJECT_KAFKA_PARTITIONS, numPartitions);
        props.setProperty(DistConfig.AGENT_REGISTRATION_OBJECT_KAFKA_REPLICATION, replicationFactor);
        return this;
    }
    public DistFactory withRegistrationKafka(String brokers, String topicName) {
        return withRegistrationKafka(brokers, topicName,
                DistConfig.AGENT_REGISTRATION_OBJECT_KAFKA_PARTITIONS_DEFAULT_VALUE,
                DistConfig.AGENT_REGISTRATION_OBJECT_KAFKA_REPLICATION_DEFAULT_VALUE);
    }

    public DistFactory withRegistrationKafkaFromEnvironment() {
        return withRegistrationKafka("${KAFKA_BROKERS}", "${KAFKA_TOPIC}",
                DistConfig.AGENT_REGISTRATION_OBJECT_KAFKA_PARTITIONS_DEFAULT_VALUE,
                DistConfig.AGENT_REGISTRATION_OBJECT_KAFKA_REPLICATION_DEFAULT_VALUE);
    }
    public DistFactory withRegistrationKafka(String brokers) {
        return withRegistrationKafka(brokers, DistConfig.AGENT_REGISTRATION_OBJECT_KAFKA_TOPIC_DEFAULT_VALUE);
    }
    public DistFactory withRegistrationMongodb(String host, int port) {
        log.debug("Set MongoDB Registration to DistFactory, guid: " + distFactoryGuid + ", host: " + host);
        props.setProperty(DistConfig.AGENT_REGISTRATION_OBJECT_MONGODB_HOST, host);
        props.setProperty(DistConfig.AGENT_REGISTRATION_OBJECT_MONGODB_PORT, ""+port);
        return this;
    }
    public DistFactory withRegistrationMongodb(String host, String port, String user, String pass) {
        log.debug("Set MongoDB Registration to DistFactory, guid: " + distFactoryGuid + ", host: " + host);
        props.setProperty(DistConfig.AGENT_REGISTRATION_OBJECT_MONGODB_HOST, host);
        props.setProperty(DistConfig.AGENT_REGISTRATION_OBJECT_MONGODB_PORT, port);
        props.setProperty(DistConfig.AGENT_REGISTRATION_OBJECT_MONGODB_USER, user);
        props.setProperty(DistConfig.AGENT_REGISTRATION_OBJECT_MONGODB_PASS, pass);
        return this;
    }
    public DistFactory withRegistrationMongodb(String host, String port) {
        log.debug("Set MongoDB Registration to DistFactory, guid: " + distFactoryGuid + ", host: " + host);
        props.setProperty(DistConfig.AGENT_REGISTRATION_OBJECT_MONGODB_HOST, host);
        props.setProperty(DistConfig.AGENT_REGISTRATION_OBJECT_MONGODB_PORT, port);
        return this;
    }
    public DistFactory withRegistrationMongodbFromEnvironment() {
        return withRegistrationMongodb("${MONGODB_HOST}", "${MONGODB_PORT}");
    }

    /** add times to inactivate other agents that have no ping for more than time declared,
     * remove all agents without ping for more than time declared
     * */
    public DistFactory withRegisterCleanAfter(long inactivateWithoutPingMs, long deleteWithoutPingMs) {
        props.setProperty(DistConfig.AGENT_CONNECTORS_INACTIVATE_AFTER, ""+inactivateWithoutPingMs);
        props.setProperty(DistConfig.AGENT_CONNECTORS_DELETE_AFTER, ""+deleteWithoutPingMs);
        return this;
    }
    public DistFactory withRegisterCleanAfter(String inactivateWithoutPingMs, String deleteWithoutPingMs) {
        props.setProperty(DistConfig.AGENT_CONNECTORS_INACTIVATE_AFTER, inactivateWithoutPingMs);
        props.setProperty(DistConfig.AGENT_CONNECTORS_DELETE_AFTER, deleteWithoutPingMs);
        return this;
    }
    public DistFactory withRegisterCleanAfterDefault() {
        props.setProperty(DistConfig.AGENT_CONNECTORS_INACTIVATE_AFTER, ""+DistConfig.AGENT_CONNECTORS_INACTIVATE_AFTER_DEFAULT_VALUE);
        props.setProperty(DistConfig.AGENT_CONNECTORS_DELETE_AFTER, ""+DistConfig.AGENT_CONNECTORS_DELETE_AFTER_DEFAULT_VALUE);
        return this;
    }
    /** add auth storage as JDBC */
    public DistFactory withAuthStorageJdbc(String url, String driver, String user, String pass) {
        props.setProperty(DistConfig.AGENT_AUTH_STORAGE_JDBC_URL, url);
        props.setProperty(DistConfig.AGENT_AUTH_STORAGE_JDBC_DRIVER, driver);
        props.setProperty(DistConfig.AGENT_AUTH_STORAGE_JDBC_USER, user);
        props.setProperty(DistConfig.AGENT_AUTH_STORAGE_JDBC_PASS, pass);
        return this;
    }
    /** add auth storage as Kafka topic */
    public DistFactory withAuthStorageKafka(String brokers, String topic, int partitions, int replication) {
        props.setProperty(DistConfig.AGENT_AUTH_STORAGE_KAFKA_BROKERS, brokers);
        props.setProperty(DistConfig.AGENT_AUTH_STORAGE_KAFKA_TOPIC, topic);
        props.setProperty(DistConfig.AGENT_AUTH_STORAGE_KAFKA_PARTITIONS, ""+partitions);
        props.setProperty(DistConfig.AGENT_AUTH_STORAGE_KAFKA_REPLICATION, ""+replication);
        return this;
    }
    public DistFactory withAuthStorageKafka(String brokers, String topic, String partitions, String replication) {
        props.setProperty(DistConfig.AGENT_AUTH_STORAGE_KAFKA_BROKERS, brokers);
        props.setProperty(DistConfig.AGENT_AUTH_STORAGE_KAFKA_TOPIC, topic);
        props.setProperty(DistConfig.AGENT_AUTH_STORAGE_KAFKA_PARTITIONS, partitions);
        props.setProperty(DistConfig.AGENT_AUTH_STORAGE_KAFKA_REPLICATION, replication);
        return this;
    }
    /** define port for Socket communication */
    public DistFactory withServerSocketPort(int port) {
        props.setProperty(DistConfig.AGENT_CONNECTORS_SERVER_SOCKET_PORT, ""+port);
        return this;
    }
    public DistFactory withServerSocketPortConfig(String cfgName) {
        props.setProperty(DistConfig.AGENT_CONNECTORS_SERVER_SOCKET_PORT, "${"+ cfgName + "}");
        return this;
    }
    /** define port to define value on which agent will be listening - this is Socket server */
    public DistFactory withServerSocketDefaultPort() {
        return withServerSocketPort(DistConfig.AGENT_CONNECTORS_SERVER_SOCKET_PORT_DEFAULT_VALUE);
    }
    /** define port for HTTP communication between agents */
    public DistFactory withServerHttpPort(int port) {
        props.setProperty(DistConfig.AGENT_CONNECTORS_SERVER_HTTP_PORT, ""+port);
        return this;
    }
    public DistFactory withServerHttpPortDefault() {
        props.setProperty(DistConfig.AGENT_CONNECTORS_SERVER_HTTP_PORT, ""+DistConfig.AGENT_CONNECTORS_SERVER_HTTP_PORT_DEFAULT_VALUE);
        return this;
    }
    /** define port for DATAGRAM/ UDP communication between agents */
    public DistFactory withServerDatagramPort(int port) {
        props.setProperty(DistConfig.AGENT_CONNECTORS_SERVER_DATAGRAM_PORT, ""+port);
        return this;
    }
    /** define default port for Datagram connection */
    public DistFactory withServerDatagramPortDefaultValue() {
        return withServerDatagramPort(DistConfig.AGENT_SERVER_DATAGRAM_PORT_DEFAULT_VALUE);
    }
    /** define port for Kafka communication */
    public DistFactory withServerKafka(String brokers, String topicName) {
        props.setProperty(DistConfig.AGENT_CONNECTORS_SERVER_KAFKA_BROKERS, brokers);
        props.setProperty(DistConfig.AGENT_CONNECTORS_SERVER_KAFKA_TOPIC, topicName);
        return this;
    }
    /** get comma-separated list of defined cache storages */
    private String withCacheExistingStorageList() {
        String existingStorages = props.getProperty(DistConfig.AGENT_CACHE_STORAGES);
        if (existingStorages == null) {
            existingStorages = "";
        }
        return existingStorages;
    }
    /** add storage with HashMap */
    public DistFactory withCacheStorageHashMap() {
        props.setProperty(DistConfig.AGENT_CACHE_STORAGES, withCacheExistingStorageList() + "," + DistConfig.AGENT_CACHE_STORAGE_VALUE_HASHMAP);
        return this;
    }
    /**  */
    public DistFactory withCacheStoragePriorityQueue() {
        props.setProperty(DistConfig.AGENT_CACHE_STORAGES, withCacheExistingStorageList() + "," + DistConfig.AGENT_CACHE_STORAGE_VALUE_PRIORITYQUEUE);
        return this;
    }
    public DistFactory withCacheStorageWeakHashMap() {
        props.setProperty(DistConfig.AGENT_CACHE_STORAGES, withCacheExistingStorageList() + "," + DistConfig.AGENT_CACHE_STORAGE_VALUE_WEAKHASHMAP);
        return this;
    }
    public DistFactory withCacheStorageElasticsearch(String url, String user, String pass) {
        props.setProperty(DistConfig.AGENT_CACHE_STORAGES, withCacheExistingStorageList() + "," + DistConfig.AGENT_CACHE_STORAGE_VALUE_ELASTICSEARCH);
        props.setProperty(DistConfig.AGENT_CACHE_STORAGE_ELASTICSEARCH_URL, url);
        props.setProperty(DistConfig.AGENT_CACHE_STORAGE_ELASTICSEARCH_USER, user);
        props.setProperty(DistConfig.AGENT_CACHE_STORAGE_ELASTICSEARCH_PASS, pass);
        return this;
    }

    public DistFactory withCacheStorageElasticsearchFromEnv() {
        props.setProperty(DistConfig.AGENT_CACHE_STORAGES, withCacheExistingStorageList() + "," + DistConfig.AGENT_CACHE_STORAGE_VALUE_ELASTICSEARCH);
        props.setProperty(DistConfig.AGENT_CACHE_STORAGE_ELASTICSEARCH_URL, "${ELASTICSEARCH_URL}");
        props.setProperty(DistConfig.AGENT_CACHE_STORAGE_ELASTICSEARCH_USER, "${ELASTICSEARCH_USER}");
        props.setProperty(DistConfig.AGENT_CACHE_STORAGE_ELASTICSEARCH_PASS, "${ELASTICSEARCH_PASS}");
        return this;
    }
    /** add JDBC as external storage */
    public DistFactory withCacheStorageJdbc(String url, String driver, String user) {
        props.setProperty(DistConfig.AGENT_CACHE_STORAGES, withCacheExistingStorageList() + "," + DistConfig.AGENT_CACHE_STORAGE_VALUE_JDBC);
        props.setProperty(DistConfig.AGENT_CACHE_STORAGE_JDBC_URL, url);
        props.setProperty(DistConfig.AGENT_CACHE_STORAGE_JDBC_DRIVER, driver);
        props.setProperty(DistConfig.AGENT_CACHE_STORAGE_JDBC_USER, user);
        props.setProperty(DistConfig.AGENT_CACHE_STORAGE_JDBC_DIALECT, driver);
        return this;
    }
    /** add JDBC as external storage */
    public DistFactory withCacheStorageJdbc(String url, String driver, String user, String pass) {
        props.setProperty(DistConfig.AGENT_CACHE_STORAGES, withCacheExistingStorageList() + "," + DistConfig.AGENT_CACHE_STORAGE_VALUE_JDBC);
        props.setProperty(DistConfig.AGENT_CACHE_STORAGE_JDBC_URL, url);
        props.setProperty(DistConfig.AGENT_CACHE_STORAGE_JDBC_DRIVER, driver);
        props.setProperty(DistConfig.AGENT_CACHE_STORAGE_JDBC_USER, user);
        props.setProperty(DistConfig.AGENT_CACHE_STORAGE_JDBC_PASS, pass);
        props.setProperty(DistConfig.AGENT_CACHE_STORAGE_JDBC_DIALECT, driver);
        return this;
    }
    /** add JDBC as external storage */
    public DistFactory withCacheStorageJdbcFromEnv() {
        props.setProperty(DistConfig.AGENT_CACHE_STORAGES, withCacheExistingStorageList() + "," + DistConfig.AGENT_CACHE_STORAGE_VALUE_JDBC);
        props.setProperty(DistConfig.AGENT_CACHE_STORAGE_JDBC_URL, "${JDBC_URL}");
        props.setProperty(DistConfig.AGENT_CACHE_STORAGE_JDBC_DRIVER, "${JDBC_DRIVER}");
        props.setProperty(DistConfig.AGENT_CACHE_STORAGE_JDBC_USER, "${JDBC_USER}");
        props.setProperty(DistConfig.AGENT_CACHE_STORAGE_JDBC_PASS, "${JDBC_PASS}");
        props.setProperty(DistConfig.AGENT_CACHE_STORAGE_JDBC_DIALECT, "${JDBC_DIALECT}");
        return this;
    }

    /** add LocalDisk with custom path as cache storage for large objects */
    public DistFactory withCacheStorageLocalDisk(String basePath) {
        props.setProperty(DistConfig.AGENT_CACHE_STORAGES, withCacheExistingStorageList() + "," + DistConfig.AGENT_CACHE_STORAGE_VALUE_LOCAL_DISK);
        props.setProperty(DistConfig.AGENT_CACHE_STORAGE_LOCALDISK_PREFIXPATH, basePath);
        return this;
    }
    /** add cache storage as Redis */
    public DistFactory withCacheStorageRedis(String url, int port) {
        props.setProperty(DistConfig.AGENT_CACHE_STORAGES, withCacheExistingStorageList() + "," + DistConfig.AGENT_CACHE_STORAGE_VALUE_REDIS);
        props.setProperty(DistConfig.AGENT_CACHE_STORAGE_REDIS_URL, url);
        props.setProperty(DistConfig.AGENT_CACHE_STORAGE_REDIS_PORT, ""+port);
        return this;
    }
    /** add cache storage as Redis */
    public DistFactory withCacheStorageRedis(String url, String port) {
        props.setProperty(DistConfig.AGENT_CACHE_STORAGES, withCacheExistingStorageList() + "," + DistConfig.AGENT_CACHE_STORAGE_VALUE_REDIS);
        props.setProperty(DistConfig.AGENT_CACHE_STORAGE_REDIS_URL, url);
        props.setProperty(DistConfig.AGENT_CACHE_STORAGE_REDIS_PORT, port);
        return this;
    }
    /** add cache storage as Mongodb */
    public DistFactory withCacheStorageMongo(String host, int port) {
        props.setProperty(DistConfig.AGENT_CACHE_STORAGES, withCacheExistingStorageList() + "," + DistConfig.AGENT_CACHE_STORAGE_VALUE_MONGO);
        props.setProperty(DistConfig.AGENT_CACHE_STORAGE_MONGODB_HOST, host);
        props.setProperty(DistConfig.AGENT_CACHE_STORAGE_MONGODB_PORT, ""+port);
        return this;
    }
    /** add cache storage as Kafka */
    public DistFactory withCacheStorageKafka(String brokers, String topicName) {
        String existingProps = ""+props.getProperty(DistConfig.AGENT_CACHE_STORAGES);
        props.setProperty(DistConfig.AGENT_CACHE_STORAGES, existingProps + "," + DistConfig.AGENT_CACHE_STORAGE_VALUE_KAFKA);
        props.setProperty(DistConfig.AGENT_CACHE_STORAGE_KAFKA_BROKERS, brokers);
        props.setProperty(DistConfig.AGENT_CACHE_STORAGE_KAFKA_TOPIC, topicName);
        return this;
    }
    /** add cache storage as Kafka */
    public DistFactory withCacheStorageKafka(String brokers) {
        return withCacheStorageKafka(brokers, DistConfig.AGENT_CACHE_STORAGE_KAFKA_BROKERS_DEFAULT_VALUE);
    }


    /** CACHE setting - object TTL - time to live */
    public DistFactory withCacheObjectTimeToLive(long timeToLiveMs) {
        props.setProperty(DistConfig.AGENT_CACHE_TTL, ""+timeToLiveMs);
        return this;
    }
    public DistFactory withCacheMaxObjectsAndItems(int maxObjects, int maxItems) {
        props.setProperty(DistConfig.AGENT_CACHE_MAX_LOCAL_OBJECTS, ""+maxObjects);
        props.setProperty(DistConfig.AGENT_CACHE_MAX_LOCAL_ITEMS, ""+maxItems);
        return this;
    }
    public DistFactory withMaxIssues(long maxIssues) {
        props.setProperty(DistConfig.AGENT_CACHE_ISSUES_MAX_COUNT, ""+maxIssues);
        return this;
    }
    /** set maximum number of events kept in cache queue */
    public DistFactory withMaxEvents(long maxEvents) {
        props.setProperty(DistConfig.AGENT_CACHE_EVENTS_MAX_COUNT, ""+maxEvents);
        return this;
    }
    /** add callback */
    public DistFactory withCallback(String eventType, Function<AgentEvent, String> callback) {
        callbacks.put(eventType, callback);
        return this;
    }
    public DistFactory withCallbacks(Map<String, Function<AgentEvent, String>> callbackMethods) {
        callbackMethods.entrySet().stream().forEach(cb -> {
            callbacks.put(cb.getKey(), cb.getValue());
        });
        return this;
    }
    /** define internal timer delay and period time in milliseconds */
    public DistFactory withTimerStorageClean(long periodMs) {
        props.setProperty(DistConfig.AGENT_CACHE_TIMER_CLEAN_STORAGE_PERIOD, ""+periodMs);
        return this;
    }
    public DistFactory withTimerStorageCleanDefault() {
        props.setProperty(DistConfig.AGENT_CACHE_TIMER_CLEAN_STORAGE_PERIOD, ""+DistConfig.AGENT_CACHE_TIMER_CLEAN_STORAGE_PERIOD_DELAY_VALUE);
        return this;
    }
    /** define internal timer delay for check  */
    public DistFactory withTimerRegistrationPeriod(long periodMs) {
        props.setProperty(DistConfig.AGENT_CACHE_TIMER_REGISTRATION_PERIOD, ""+periodMs);
        return this;
    }
    public DistFactory withTimerRegistrationPeriodDefault() {
        props.setProperty(DistConfig.AGENT_CACHE_TIMER_REGISTRATION_PERIOD, ""+DistConfig.AGENT_CACHE_TIMER_REGISTRATION_PERIOD_DELAY_VALUE);
        return this;
    }
    /** define internal timer delay for check servers and clients */
    public DistFactory withTimerServerPeriod(long periodMs) {
        props.setProperty(DistConfig.AGENT_CACHE_TIMER_SERVER_CLIENT_PERIOD, ""+periodMs);
        return this;
    }
    public DistFactory withTimerServerPeriodDefault() {
        props.setProperty(DistConfig.AGENT_CACHE_TIMER_SERVER_CLIENT_PERIOD, ""+DistConfig.TIMER_SERVER_CLIENT_PERIOD_DELAY_VALUE);
        return this;
    }
    /** add JDBC as configuration reader */
    public DistFactory withConfigReaderJdbc(String url, String driver, String user, String pass) {
        props.setProperty(DistConfig.AGENT_CONFIGREADER_OBJECT_JDBC_URL, url);
        props.setProperty(DistConfig.AGENT_CONFIGREADER_OBJECT_JDBC_DRIVER, driver);
        props.setProperty(DistConfig.AGENT_CONFIGREADER_OBJECT_JDBC_USER, user);
        props.setProperty(DistConfig.AGENT_CONFIGREADER_OBJECT_JDBC_PASS, pass);
        return this;
    }
    /** add HTTP as configuration reader */
    public DistFactory withConfigReaderHttp(String url, String headers) {
        props.setProperty(DistConfig.AGENT_CONFIGREADER_OBJECT_HTTP_URL, url);
        props.setProperty(DistConfig.AGENT_CONFIGREADER_OBJECT_HTTP_HEADERS, headers);
        return this;
    }

    /** set cache policy, this is overwriting existing policy */
    public DistFactory withCachePolicy(CachePolicy policy) {
        this.policy = policy;
        return this;
    }

}
