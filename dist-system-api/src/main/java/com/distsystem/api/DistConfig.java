package com.distsystem.api;

import com.distsystem.api.info.DistConfigGroupInfo;
import com.distsystem.interfaces.DistService;
import com.distsystem.utils.DistUtils;
import com.distsystem.utils.JsonUtils;
import com.distsystem.utils.ResolverManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/** Configuration for distributed system - this is keeping parameters in Properties format and Value resolver to resolve values of properties.
 * Configuration has also value change listeners - so each module can subscribe to changes of values for given properties.
 *  */
public class DistConfig {
    private static final Logger log = LoggerFactory.getLogger(DistConfig.class);

    /** build empty DistConfig with no values */
    public static DistConfig buildEmptyConfig() {
        return new DistConfig(new Properties());
    }
    /** build DistConfig using initial properties*/
    public static DistConfig buildConfig(Properties initialProperties) {
        return new DistConfig(initialProperties);
    }
    /** build DistConfig using Map */
    public static DistConfig buildConfigFromMap(Map<String, String> map) {
        Properties pr = new Properties();
        pr.putAll(map);
        return new DistConfig(pr);
    }

    /** global unique ID of this configuration */
    private final String configGuid = DistUtils.generateConfigGuid();
    /** all properties to be used for DistSystem initialization */
    private final Properties props;
    /** SEQ ID of change for configuration parameters */
    private final AtomicLong changeSeq = new AtomicLong();
    /** resolver for String values in properties */
    private final ResolverManager resolver;
    /** all configuration groups AGENT_REGISTRATION, CACHE_STORAGE */
    private final Map<String, DistConfigGroup> configGroups = new HashMap<>();

    /** creates new config with initial properties */
    public DistConfig(Properties p) {
        this.props = p;
        this.resolver = new ResolverManager();
        props.setProperty(AGENT_CONFIG_GUID, configGuid);
    }
    /** creates new config with initial properties and resolver */
    public DistConfig(Properties p, ResolverManager resolver) {
        this.props = p;
        this.resolver = resolver;
        props.setProperty(AGENT_CONFIG_GUID, configGuid);
    }

    /** count objects in this agentable object including this object */
    public long countObjects() {
        return 4L + props.size()*2L + configGroups.size()*2L;
    }
    /** register new config group and parse configuration values into buckets */
    public DistConfigGroup registerConfigGroup(String groupName, DistService parentService) {
        synchronized (configGroups) {
            DistConfigGroup gr = configGroups.get(groupName);
            if (gr == null) {
                log.info("Try to register new configuration group for name: " + groupName + ", groups: " + configGroups.size() + ", parent service: " + parentService.getServiceType().name());
                gr = new DistConfigGroup(this, groupName, parentService);
                configGroups.put(groupName, gr);
            }
            parentService.afterInitialization();
            return gr;
        }
    }
    /** unregister config group for name */
    public void unregisterConfigGroup(String groupName) {
        synchronized (configGroups) {
            log.info("Try to unregister configuration group for name: " + groupName + ", groups: " + configGroups.size());
            configGroups.remove(groupName);
        }
    }
    /** get resolver manager to resolve names, values, properties */
    public ResolverManager getResolverManager() {
        return resolver;
    }
    /** get all configuration groups */
    public List<DistConfigGroup> getConfigGroups() {
        return configGroups.values().stream().toList();
    }
    /** get all configuration group infos */
    public List<DistConfigGroupInfo> getConfigGroupInfos() {
        return configGroups.values().stream().map(DistConfigGroup::getInfo).toList();
    }
    /** add listener for given value change */
    public DistConfig addListener(String fullConfigName, Function<String, Boolean> onCfgValueChange) {

        return this;
    }
    /** get count of properties */
    public int getPropertiesCount() {
        return props.size();
    }
    /** get current properties */
    public Map getProperties() {
        return Collections.unmodifiableMap(props);
    }

    /** get current properties */
    public Map<String, String> getPropertiesStartsWith(String configNameStart) {
        Map<String, String> p = new HashMap<>();
        props.entrySet().stream().filter(x -> x.getKey().toString().startsWith(configNameStart)).forEach(c -> {
            p.put(c.getKey().toString(), c.getValue().toString());
        });
        return p;
    }
    /** get HashMap with properties for cache */
    public HashMap<String, String> getHashMap(boolean includePassword) {
        HashMap<String, String> hm = new HashMap<>();
        for (Map.Entry<Object, Object> e: props.entrySet()) {
            if (includePassword || !e.getKey().toString().contains("PASS")) {
                hm.put(e.getKey().toString(), e.getValue().toString());
            }
        }
        return hm;
    }
    /** get unique ID of this config for cache */
    public String getConfigGuid() {
        return configGuid;
    }
    /** get cache property for given name */
    public String getProperty(String name) {
        return resolver.resolve(props.getProperty(name));
    }
    /** configuration contains property for given name */
    public boolean hasProperty(String name) {
        return props.containsKey(name);
    }
    /** get property */
    public String getProperty(String name, String defaultValue) {
        String value = getProperty(name);
        if (value != null) {
            return value;
        }
        value = getProperty(name.replace("_PRIMARY", ""));
        if (value != null) {
            return value;
        }
        value = getProperty(name + "_PRIMARY");
        if (value != null) {
            return value;
        }
        return defaultValue;
    }

    /** commit properties after read */
    public void mergeWithConfig(DistConfig newCfg) {
        // TODO: implement merge new configuration with current configuration




    }

    /** get value of given configuration property as long value */
    public long getPropertyAsLong(String name, long defaultValue) {
        return DistUtils.parseLong(getProperty(name), defaultValue);
    }
    /**  */
    public int getPropertyAsInt(String name, int defaultValue) {
        return DistUtils.parseInt(getProperty(name), defaultValue);
    }
    /** */
    public double getPropertyAsDouble(String name, double defaultValue) {
        return DistUtils.parseDouble(getProperty(name), defaultValue);
    }
    /** save to File as JSON format */
    public boolean saveToJsonFile(String fileName) {
        try {
            Files.writeString(Path.of(""), toJsonString(), StandardOpenOption.CREATE);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
    /** save to file as Properties format */
    public boolean saveToPropertiesFile(String fileName) {
        try {
            props.store(new FileWriter(fileName), "");
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
    /** save properties from this configuration to JSON as String */
    public String toJsonString() {
        return JsonUtils.serialize(props);
    }
    /** save properties from this configuration to Properties as String */
    public String toPropertiesString() {
        try {
            ByteArrayOutputStream outStr = new ByteArrayOutputStream();
            props.store(outStr, "");
            return new String(outStr.toByteArray());
        } catch (IOException ex) {
            return null;
        }
    }

    public static String JDBC = "JDBC";
    public static String APPLICATION = "APPLICATION";
    public static String ELASTICSEARCH = "ELASTICSEARCH";
    public static String REDIS = "REDIS";
    public static String KAFKA = "KAFKA";
    public static String MONGODB = "MONGODB";
    public static String HTTP = "HTTP";
    public static String SOCKET = "SOCKET";
    public static String DATAGRAM = "DATAGRAM";
    public static String CASSANDRA = "CASSANDRA";

    public static String DEFAULT = "DEFAULT";
    public static String URL = "URL";
    public static String DRIVER = "DRIVER";
    public static String USER = "USER";
    public static String PASS = "PASS";
    public static String TABLE = "TABLE";
    public static String DIALECT = "DIALECT";
    public static String INIT_CONNECTIONS = "INIT_CONNECTIONS";
    public static String MAX_ACTIVE_CONNECTIONS = "MAX_ACTIVE_CONNECTIONS";
    public static String BROKERS = "BROKERS";
    public static String HOST = "HOST";
    public static String PORT = "PORT";
    public static String TIMEOUT = "TIMEOUT";
    public static String TOPIC = "TOPIC";
    public static String PARTITIONS = "PARTITIONS";
    public static String REPLICATION = "REPLICATION";
    public static String PERIOD = "PERIOD";
    public static String DATABASE = "DATABASE";
    public static String COLLECTION = "COLLECTION";
    public static String INDEX = "INDEX";
    public static String HEADERS = "HEADERS";

    public static String PRIMARY = "PRIMARY";
    public static String SECONDARY = "SECONDARY";
    public static String TERTIARY = "TERTIARY";


    public static String AGENT_GLOBAL_GUID = "AGENT_GLOBAL_GUID";
    public static String AGENT_CONFIG_GUID = "AGENT_CONFIG_GUID";

    /** name of Agent distributed universe */
    public static String AGENT_UNIVERSE = "AGENT_UNIVERSE";
    public static String AGENT_UNIVERSE_NAME_DEFAULT = "DistSystem";

    /** name of group - all caches connecting together should be having the same group
     * name of group could be like GlobalAppCache */
    public static String AGENT_GROUP = "AGENT_GROUP";
    public static String AGENT_GROUP_VALUE_DEFAULT = "Agents";

    /** name of Agent - friendly value for easy log reading */
    public static String AGENT_NAME = "AGENT_NAME";
    public static String AGENT_NAME_VALUE_DEFAULT = "Agent${}";

    /** type and name of the environment */
    public static String AGENT_ENVIRONMENT_TYPE = "AGENT_ENVIRONMENT_TYPE";
    public static String AGENT_ENVIRONMENT_TYPE_VALUE_DEFAULT = "development";
    public static String AGENT_ENVIRONMENT_NAME = "DIST_ENVIRONMENT_NAME";
    public static String AGENT_ENVIRONMENT_NAME_VALUE_DEFAULT = "development";

    public static String AGENT_HOST_NAME = "AGENT_HOST_NAME";
    public static String AGENT_HOST_ADDRESS = "AGENT_HOST_ADDRESS";
    public static String AGENT_LOCATION_PATH = "AGENT_LOCATION_PATH";
    public static String AGENT_PARENT_APPLICATION = "AGENT_PARENT_APPLICATION";

    /** port for HTTP REST Web API to contact directly to Agent */
    public static String AGENT_API_PORT = "AGENT_API_PORT";
    public static int AGENT_API_PORT_DEFAULT_VALUE = 9999;

    /** tags used to identify agent */
    public static String AGENT_TAGS = "AGENT_TAGS";
    public static String AGENT_REGISTRATION = "AGENT_REGISTRATION";


    public static Map<String, String> AGENT_REGISTRATION_CLASS_MAP = Map.of(
            APPLICATION, "com.distsystem.agent.registrations.RegistrationApplication",
            JDBC, "com.distsystem.agent.registrations.RegistrationJdbc",
            KAFKA, "com.distsystem.agent.registrations.RegistrationKafka",
            ELASTICSEARCH, "com.distsystem.agent.registrations.RegistrationElasticsearch",
            MONGODB, "com.distsystem.agent.registrations.RegistrationMongodb"
    );

    /** JDBC connection for agent registration */
    public static String AGENT_REGISTRATION_OBJECT_JDBC_URL = "AGENT_REGISTRATION_OBJECT_JDBC_URL";
    public static String AGENT_REGISTRATION_OBJECT_JDBC_DRIVER = "AGENT_REGISTRATION_OBJECT_JDBC_DRIVER";
    public static String AGENT_REGISTRATION_OBJECT_JDBC_USER = "AGENT_REGISTRATION_OBJECT_JDBC_USER";
    public static String AGENT_REGISTRATION_OBJECT_JDBC_PASS = "AGENT_REGISTRATION_OBJECT_JDBC_PASS";
    public static String AGENT_REGISTRATION_OBJECT_JDBC_DIALECT = "AGENT_REGISTRATION_OBJECT_JDBC_DIALECT";
    public static String AGENT_REGISTRATION_OBJECT_JDBC_INIT = "AGENT_REGISTRATION_OBJECT_JDBC_INIT";
    public static String AGENT_REGISTRATION_OBJECT_JDBC_MAXCONNECTIONS = "AGENT_REGISTRATION_OBJECT_JDBC_MAXCONNECTIONS";

    /** elasticsearch registration parameters */
    public static String AGENT_REGISTRATION_OBJECT_ELASTICSEARCH_URL = "AGENT_REGISTRATION_OBJECT_ELASTICSEARCH_URL";
    public static String AGENT_REGISTRATION_OBJECT_ELASTICSEARCH_USER = "AGENT_REGISTRATION_OBJECT_ELASTICSEARCH_USER";
    public static String AGENT_REGISTRATION_OBJECT_ELASTICSEARCH_PASS = "AGENT_REGISTRATION_OBJECT_ELASTICSEARCH_PASS";

    /** */
    public static String AGENT_REGISTRATION_OBJECT_REDIS_HOST = "AGENT_REGISTRATION_OBJECT_REDIS_HOST";
    public static String AGENT_REGISTRATION_OBJECT_REDIS_PORT = "AGENT_REGISTRATION_OBJECT_REDIS_PORT";

    /** */
    public static String AGENT_REGISTRATION_OBJECT_KAFKA_BROKERS = "AGENT_REGISTRATION_OBJECT_KAFKA_BROKERS";
    public static String AGENT_REGISTRATION_OBJECT_KAFKA_TOPIC = "AGENT_REGISTRATION_OBJECT_KAFKA_TOPIC";
    public static String AGENT_REGISTRATION_OBJECT_KAFKA_TOPIC_DEFAULT_VALUE = "dist-agent-registration";
    public static String AGENT_REGISTRATION_OBJECT_KAFKA_PARTITIONS = "AGENT_REGISTRATION_OBJECT_KAFKA_PARTITIONS";
    public static int AGENT_REGISTRATION_OBJECT_KAFKA_PARTITIONS_DEFAULT_VALUE = 3;
    public static String AGENT_REGISTRATION_OBJECT_KAFKA_REPLICATION = "AGENT_REGISTRATION_OBJECT_KAFKA_REPLICATION";
    public static short AGENT_REGISTRATION_OBJECT_KAFKA_REPLICATION_DEFAULT_VALUE = 3;

    public static String AGENT_REGISTRATION_OBJECT_MONGODB_HOST = "AGENT_REGISTRATION_OBJECT_MONGODB_HOST";
    public static String AGENT_REGISTRATION_OBJECT_MONGODB_PORT = "AGENT_REGISTRATION_OBJECT_MONGODB_PORT";

    public static Map<String, String> AGENT_AUTH_CLASS_MAP = Map.of(
            JDBC, "com.distsystem.agent.auth.AgentAuthJdbc",
            KAFKA, "com.distsystem.agent.auth.AgentAuthKafka"
    );
    public static String AGENT_AUTH_OBJECT_JDBC_URL = "AGENT_CACHE_APPLICATION_URL";

    public static Map<String, String> AGENT_AUTH_STORAGE_CLASS_MAP = Map.of(
            JDBC, "com.distsystem.agent.auth.AgentAuthStorageJdbc",
            KAFKA, "com.distsystem.agent.auth.AgentAuthStorageKafka"
    );
    public static String AGENT_AUTH_STORAGE_JDBC_URL = "AGENT_AUTH_STORAGE_JDBC_URL";
    public static String AGENT_AUTH_STORAGE_JDBC_DRIVER = "AGENT_AUTH_STORAGE_JDBC_DRIVER";
    public static String AGENT_AUTH_STORAGE_JDBC_USER = "AGENT_AUTH_STORAGE_JDBC_USER";
    public static String AGENT_AUTH_STORAGE_JDBC_PASS = "AGENT_AUTH_STORAGE_JDBC_PASS";
    public static String AGENT_AUTH_STORAGE_JDBC_DIALECT = "AGENT_AUTH_STORAGE_JDBC_DIALECT";

    public static String AGENT_AUTH_STORAGE_KAFKA_BROKERS = "AGENT_AUTH_STORAGE_KAFKA_BROKERS";
    public static String AGENT_AUTH_STORAGE_KAFKA_TOPIC = "AGENT_AUTH_STORAGE_KAFKA_TOPIC";
    public static String AGENT_AUTH_STORAGE_KAFKA_PARTITIONS = "AGENT_AUTH_STORAGE_KAFKA_PARTITIONS";
    public static String AGENT_AUTH_STORAGE_KAFKA_REPLICATION = "AGENT_AUTH_STORAGE_KAFKA_REPLICATION";


    public static Map<String, String> AGENT_CONNECTORS_CLASS_MAP = Map.of(
            SOCKET, "com.distsystem.agent.servers.AgentServerSocket",
            HTTP, "com.distsystem.agent.servers.AgentHttpServer",
            DATAGRAM, "com.distsystem.agent.servers.AgentDatagramServer",
            KAFKA, "com.distsystem.agent.servers.AgentKafkaServer"
    );
    /**port of SockerServer to exchange messages between Agents */
    public static String AGENT_CONNECTORS_SERVER_SOCKET_PORT = "AGENT_CONNECTORS_SERVER_SOCKET_PORT";
    /** */
    public static int AGENT_CONNECTORS_SERVER_SOCKET_PORT_DEFAULT_VALUE = 9901;
    /** sequencer for default agent port */
    public static final AtomicInteger AGENT_CONNECTORS_SOCKET_PORT_VALUE_SEQ = new AtomicInteger(9901);
    /** timeout value for Socket reading in milliseconds, this is used for Socket.setSoTimeout(...) */
    public static String AGENT_CONNECTORS_SERVER_SOCKET_CLIENT_TIMEOUT = "AGENT_CONNECTORS_SERVER_SOCKET_CLIENT_TIMEOUT";
    public static int AGENT_CONNECTORS_SERVER_SOCKET_CLIENT_TIMEOUT_DEFAULT_VALUE = 2000;

    /** port of HTTP Server to exchange messages between Agents */
    public static String AGENT_CONNECTORS_SERVER_HTTP_PORT = "AGENT_CONNECTORS_SERVER_HTTP_PORT";
    public static int AGENT_CONNECTORS_SERVER_HTTP_PORT_DEFAULT_VALUE = 9912;

    public static String AGENT_CONNECTORS_SERVER_DATAGRAM_PORT = "AGENT_CONNECTORS_SERVER_DATAGRAM_PORT";
    public static int AGENT_SERVER_DATAGRAM_PORT_DEFAULT_VALUE = 9933;
    public static String AGENT_CONNECTORS_SERVER_DATAGRAM_TIMEOUT = "AGENT_CONNECTORS_SERVER_DATAGRAM_TIMEOUT";

    /** Server for agent communication based on Kafka */
    public static String AGENT_CONNECTORS_SERVER_KAFKA_BROKERS = "AGENT_CONNECTORS_SERVER_KAFKA_BROKERS";
    public static String AGENT_CONNECTORS_SERVER_KAFKA_BROKERS_DEFAULT_VALUE = "localhost:9092";
    public static String AGENT_CONNECTORS_SERVER_KAFKA_TOPIC = "AGENT_CONNECTORS_SERVER_KAFKA_TOPIC";
    public static String AGENT_CONNECTORS_SERVER_KAFKA_TOPIC_DEFAULT_VALUE = "dist-agent-";

    /** times to deactivate old agents that are not pinging registration services
     * After AGENT_INACTIVATE_AFTER milliseconds without ping all agents would be deactivated.
     * After AGENT_DELETE_AFTER  milliseconds without ping all agents would be deleted forever
     * */
    public static final String AGENT_CONNECTORS_INACTIVATE_AFTER = "AGENT_CONNECTORS_INACTIVATE_AFTER";
    public static final long AGENT_CONNECTORS_INACTIVATE_AFTER_DEFAULT_VALUE = CacheMode.TIME_TEN_MINUTES;
    public static final String AGENT_CONNECTORS_DELETE_AFTER = "AGENT_CONNECTORS_DELETE_AFTER";
    public static final long AGENT_CONNECTORS_DELETE_AFTER_DEFAULT_VALUE = CacheMode.TIME_ONE_DAY;


    /** URL of cache standalone application to synchronize all distributed cache managers
     * Cache Standalone App is registering and unregistering all cache agents with managers
     * that are working in cluster */
    public static String AGENT_CACHE_APPLICATION_URL = "AGENT_CACHE_APPLICATION_URL";
    public static String AGENT_CACHE_APPLICATION_URL_DEFAULT_VALUE = "http://localhost:8085/api";

    /** period of timer to clear storages - value in milliseconds */
    public static String AGENT_CACHE_TIMER_CLEAN_STORAGE_PERIOD = "AGENT_CACHE_TIMER_CLEAN_STORAGE_PERIOD";
    public static long AGENT_CACHE_TIMER_CLEAN_STORAGE_PERIOD_DELAY_VALUE = CacheMode.TIME_ONE_MINUTE;

    /** timer to refresh statistics
     * Every TIMER_STAT_REFRESH_PERIOD milliseconds cache statistics would be refreshed and saved
     * */
    public static String AGENT_CACHE_TIMER_STAT_REFRESH_PERIOD = "AGENT_CACHE_TIMER_STAT_REFRESH_PERIOD";
    public static long AGENT_CACHE_TIMER_STAT_REFRESH_PERIOD_DELAY_VALUE = CacheMode.TIME_ONE_MINUTE;

    /** timer to check registration objects like agents, servers, new configurations
     * Every TIMER_REGISTRATION_PERIOD milliseconds Agent would check registration objects, ping, check old agents, refresh list of agents
     * */
    public static String AGENT_CACHE_TIMER_REGISTRATION_PERIOD = "AGENT_CACHE_TIMER_REGISTRATION_PERIOD";
    public static long AGENT_CACHE_TIMER_REGISTRATION_PERIOD_DELAY_VALUE = CacheMode.TIME_ONE_MINUTE;

    /** default value of time-to-live objects in cache*/
    public static String AGENT_CACHE_TTL = "AGENT_CACHE_TTL";
    public static long AGENT_CACHE_TTL_VALUE_DEFAULT = CacheMode.TIME_ONE_HOUR;

    /** timer to check connections, servers, clients
     * Every TIMER_SERVER_CLIENT_PERIOD milliseconds connections would be refreshed, clients would be checked,
     * old connections would be removed, connections to servers or agents would be created */
    public static String AGENT_CACHE_TIMER_SERVER_CLIENT_PERIOD = "AGENT_CACHE_TIMER_SERVER_CLIENT_PERIOD";
    public static long TIMER_SERVER_CLIENT_PERIOD_DELAY_VALUE = CacheMode.TIME_ONE_MINUTE;

    /** serializer full definition to serialize and deserialize objects */
    public static String AGENT_SERIALIZER_DEFINITION = "AGENT_SERIALIZER_DEFINITION";
    public static String AGENT_SERIALIZER_DEFINITION_SERIALIZABLE_VALUE = "java.lang.String=StringSerializer,default=ObjectStreamSerializer";

    /** maximum number of local objects */
    public static String AGENT_CACHE_MAX_LOCAL_OBJECTS = "AGENT_CACHE_MAX_LOCAL_OBJECTS";
    public static int AGENT_CACHE_MAX_LOCAL_OBJECTS_VALUE = 1000;

    /** maximum number of issues stored in cache */
    public static String AGENT_CACHE_ISSUES_MAX_COUNT = "AGENT_CACHE_ISSUES_MAX_COUNT";
    public static long AGENT_CACHE_ISSUES_MAX_COUNT_VALUE = 1000;

    /** maximum number of issues stored in cache */
    public static String AGENT_CACHE_EVENTS_MAX_COUNT = "AGENT_CACHE_EVENTS_MAX_COUNT";
    public static long AGENT_CACHE_EVENTS_MAX_COUNT_VALUE = 100;

    public static String AGENT_CACHE_POLICY = "AGENT_CACHE_POLICY";

    /** maximum number of local items - each object could be a list with many objects
     * this could be taken from collection size = number of items */
    public static String AGENT_CACHE_MAX_LOCAL_ITEMS = "AGENT_CACHE_MAX_LOCAL_ITEMS";
    public static int AGENT_CACHE_MAX_LOCAL_ITEMS_VALUE = 100000;

    /** Encoder class name to encode key of cache for different reasons:
     * Encoded key might hide passwords and secret values
     * Encoded key might be simpler and shorter
     * Encoded key might be removing some unwanted characters that would be problematic in some cache storages */
    public static String AGENT_CACHE_KEY_ENCODER = "AGENT_CACHE_KEY_ENCODER";
    public static String AGENT_CACHE_KEY_ENCODER_VALUE_NONE = "com.distsystem.encoders.KeyEncoderNone";
    public static String AGENT_CACHE_KEY_ENCODER_VALUE_SECRET = "com.distsystem.encoders.KeyEncoderStarting";
    public static String AGENT_CACHE_KEY_ENCODER_VALUE_FULL = "com.distsystem.encoders.KeyEncoderFull";

    /** default number of milliseconds as time to live for given cache object */
    public static String AGENT_CACHE_DEFAULT_TTL_TIME = "AGENT_CACHE_DEFAULT_TTL_TIME";
    /** list of semicolon separated storages initialized for cache
     * RedisCacheStorage;MongodbStorage;InternalHashMapCacheStorage;JdbcStorage
     *  */
    public static String AGENT_CACHE_STORAGES = "AGENT_CACHE_STORAGES";
    /** names of storages and class names for these storages in package distsystem.storage */
    public static String AGENT_CACHE_STORAGE_VALUE_HASHMAP = "InternalHashMapCacheStorage";
    public static String AGENT_CACHE_STORAGE_VALUE_WEAKHASHMAP = "InternalWeakHashMapCacheStorage";
    public static String AGENT_CACHE_STORAGE_VALUE_PRIORITYQUEUE = "InternalWithTtlAndPriorityCacheStorage";
    public static String AGENT_CACHE_STORAGE_VALUE_REDIS = "RedisCacheStorage";
    public static String AGENT_CACHE_STORAGE_VALUE_MONGO = "MongodbCacheStorage";
    public static String AGENT_CACHE_STORAGE_VALUE_CASSANDRA = "CassandraCacheStorage";
    public static String AGENT_CACHE_STORAGE_VALUE_KAFKA = "KafkaCacheStorage";
    public static String AGENT_CACHE_STORAGE_VALUE_LOCAL_DISK = "LocalDiskCacheStorage";
    public static String AGENT_CACHE_STORAGE_VALUE_ELASTICSEARCH = "ElasticsearchCacheCacheStorage";
    public static String AGENT_CACHE_STORAGE_VALUE_JDBC = "JdbcCacheStorage";

    /** CACHE STORAGE - settings for JDBC storage */
    public static String AGENT_CACHE_STORAGE_JDBC_URL = "AGENT_CACHE_STORAGE_JDBC_URL";
    public static String AGENT_CACHE_STORAGE_JDBC_DRIVER = "AGENT_CACHE_STORAGE_JDBC_DRIVER";
    public static String AGENT_CACHE_STORAGE_JDBC_USER = "AGENT_CACHE_STORAGE_JDBC_USER";
    public static String AGENT_CACHE_STORAGE_JDBC_PASS = "AGENT_CACHE_STORAGE_JDBC_PASS";
    public static String AGENT_CACHE_STORAGE_JDBC_DIALECT = "AGENT_CACHE_STORAGE_JDBC_DIALECT";
    public static String AGENT_CACHE_STORAGE_JDBC_INITCONNECTIONS = "AGENT_CACHE_STORAGE_JDBC_INITCONNECTIONS";
    public static String AGENT_CACHE_STORAGE_JDBC_MAXCONNECTIONS = "AGENT_CACHE_STORAGE_JDBC_MAXCONNECTIONS";

    /** CACHE STORAGE - settings for MongoDB storage */
    public static String AGENT_CACHE_STORAGE_MONGODB_HOST = "AGENT_CACHE_STORAGE_MONGODB_HOST";
    public static String AGENT_CACHE_STORAGE_MONGODB_PORT = "AGENT_CACHE_STORAGE_MONGODB_PORT";
    public static String AGENT_CACHE_STORAGE_MONGODB_DATABASE = "AGENT_CACHE_STORAGE_MONGODB_DATABASE";
    public static String AGENT_CACHE_STORAGE_MONGODB_COLLECTION = "AGENT_CACHE_STORAGE_MONGODB_COLLECTION";

    /** CACHE STORAGE - settings for Redis storage */
    public static String AGENT_CACHE_STORAGE_REDIS_HOST = "AGENT_CACHE_STORAGE_REDIS_HOST";
    public static String AGENT_CACHE_STORAGE_REDIS_PORT = "AGENT_CACHE_STORAGE_REDIS_PORT";
    public static String AGENT_CACHE_STORAGE_REDIS_URL = "AGENT_CACHE_STORAGE_REDIS_URL";

    /** CACHE STORAGE - settings for Elasticsearch storage */
    public static String AGENT_CACHE_STORAGE_ELASTICSEARCH_URL = "AGENT_CACHE_STORAGE_ELASTICSEARCH_URL";
    public static String AGENT_CACHE_STORAGE_ELASTICSEARCH_USER = "AGENT_CACHE_STORAGE_ELASTICSEARCH_USER";
    public static String AGENT_CACHE_STORAGE_ELASTICSEARCH_PASS = "AGENT_CACHE_STORAGE_ELASTICSEARCH_PASS";
    public static String AGENT_CACHE_STORAGE_ELASTICSEARCH_INDEX = "AGENT_CACHE_STORAGE_ELASTICSEARCH_INDEX";
    public static String AGENT_CACHE_STORAGE_ELASTICSEARCH_INDEX_DEFAULT_VALUE = "dist-cache-items";

    /** CACHE STORAGE - settings for Cassandra storage */
    public static String AGENT_CACHE_STORAGE_CASSANDRA_HOST = "AGENT_CACHE_STORAGE_CASSANDRA_HOST";
    public static String AGENT_CACHE_STORAGE_CASSANDRA_PORT = "AGENT_CACHE_STORAGE_CASSANDRA_PORT";

    /** CACHE STORAGE - Kafka brokers */
    public static String AGENT_CACHE_STORAGE_KAFKA_BROKERS = "AGENT_CACHE_STORAGE_KAFKA_BROKERS";
    public static String AGENT_CACHE_STORAGE_KAFKA_TOPIC = "AGENT_CACHE_STORAGE_KAFKA_TOPIC";
    public static String AGENT_CACHE_STORAGE_KAFKA_BROKERS_DEFAULT_VALUE = "dist-cache-items";

    /** CACHE STORAGE - LocalDisk prefix for storage folder/ directory */
    public static String AGENT_CACHE_STORAGE_LOCALDISK_PREFIXPATH = "CACHE_STORAGE_LOCAL_DISK_PREFIX_PATH";

    public static String AGENT_SEMAPHORE_OBJECT_JDBC_URL = "AGENT_SEMAPHORE_OBJECT_JDBC_URL";
    public static String AGENT_SEMAPHORE_OBJECT_JDBC_DRIVER = "AGENT_SEMAPHORE_OBJECT_JDBC_DRIVER";
    public static String AGENT_SEMAPHORE_OBJECT_JDBC_USER = "AGENT_SEMAPHORE_OBJECT_JDBC_USER";
    public static String AGENT_SEMAPHORE_OBJECT_JDBC_PASS = "AGENT_SEMAPHORE_OBJECT_JDBC_PASS";

    public static String AGENT_CONFIGREADER_TIMER = "AGENT_CONFIGREADER_TIMER";
    public static String AGENT_CONFIGREADER_OBJECT_HTTP_URL = "AGENT_CONFIGREADER_OBJECT_HTTP_URL";
    public static String AGENT_CONFIGREADER_OBJECT_HTTP_HEADERS = "AGENT_CONFIGREADER_OBJECT_HTTP_HEADERS";

    public static Map<String, String> AGENT_CONFIGREADER_MAP = Map.of(
            JDBC, "com.distsystem.agent.configreaders.ConfigReaderHttp",
            KAFKA, "com.distsystem.agent.configreaders.ConfigReaderKafka"
    );
    public static String AGENT_CONFIGREADER_OBJECT_JDBC_URL = "AGENT_CONFIGREADER_OBJECT_JDBC_URL";
    public static String AGENT_CONFIGREADER_OBJECT_JDBC_DRIVER = "AGENT_CONFIGREADER_OBJECT_JDBC_DRIVER";
    public static String AGENT_CONFIGREADER_OBJECT_JDBC_USER = "AGENT_CONFIGREADER_OBJECT_JDBC_USER";
    public static String AGENT_CONFIGREADER_OBJECT_JDBC_PASS = "AGENT_CONFIGREADER_OBJECT_JDBC_PASS";
    public static String AGENT_CONFIGREADER_OBJECT_JDBC_TABLE = "AGENT_CONFIGREADER_OBJECT_JDBC_TABLE";
    public static String AGENT_CONFIGREADER_OBJECT_JDBC_DIALECT = "AGENT_CONFIGREADER_OBJECT_JDBC_DIALECT";

    public static String AGENT_AUTH_OBJECT = "AGENT_AUTH_OBJECT";
    public static String AGENT_AUTH_STORAGE = "AGENT_AUTH_STORAGE";
    public static String AGENT_AUTH_TOKEN = "AGENT_AUTH_TOKEN";
    public static String AGENT_AUTH_IDENTITY = "AGENT_AUTH_IDENTITY";
    public static String AGENT_CONFIGREADER_OBJECT = "AGENT_CONFIGREADER_OBJECT";
    public static String AGENT_REGISTRATION_OBJECT = "AGENT_REGISTRATION_OBJECT";
    public static String AGENT_CONNECTORS_SERVER = "AGENT_CONNECTORS_SERVER";
    public static String AGENT_CACHE_STORAGE = "AGENT_CACHE_STORAGE";
    public static String AGENT_SEMAPHORE_OBJECT = "AGENT_SEMAPHORE_OBJECT";
    public static String AGENT_STORAGE_OBJECT = "AGENT_STORAGE_OBJECT";

    public static Map<String, String> AGENT_SEMAPHORE_CLASS_MAP = Map.of(
            JDBC, "com.distsystem.agent.impl.semaphores.SemaphoreJdbc"
    );

}
