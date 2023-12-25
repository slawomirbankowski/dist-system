package com.distsystem.agent.services;

import com.distsystem.api.*;
import com.distsystem.api.enums.DistDaoType;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.api.info.AgentDaoInfo;
import com.distsystem.api.info.AgentDaoSimpleInfo;
import com.distsystem.api.info.AgentDaosInfo;
import com.distsystem.base.ServiceBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentComponent;
import com.distsystem.interfaces.Dao;
import com.distsystem.dao.DaoElasticsearchBase;
import com.distsystem.dao.DaoJdbcBase;
import com.distsystem.dao.DaoKafkaBase;
import com.distsystem.interfaces.AgentDao;
import com.distsystem.utils.DistWebApiProcessor;
import com.distsystem.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/** service for DAOs - all external data access objects to JDBC, Kafka, Redis, Elasticsearch, MongoDB, Cassandra, ... */
public class AgentDaoImpl extends ServiceBase implements AgentDao {

    /** local logger for this class */
    protected static final Logger log = LoggerFactory.getLogger(AgentDaoImpl.class);

    /** DAOs for Kafka, JDBC, Elasticsearch, Redis, MongoDB, and all other DAOs */
    private final java.util.concurrent.ConcurrentHashMap<String, Dao> daos = new java.util.concurrent.ConcurrentHashMap<>();
    /** all producers for DAO objects */
    private final Map<String, Function<DaoParams, Dao>> daoProducers = new HashMap<>(Map.of(
            DaoKafkaBase.class.getName(), this::createKafkaDao,
            DaoJdbcBase.class.getName(), this::createJdbcDao,
            DaoElasticsearchBase.class.getName(), this::createElasticsearchDao
    ));

    /** create new DAO manager */
    public AgentDaoImpl(Agent parentAgent) {
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
        return DistServiceType.daos;
    }
    /** get description of this service */
    public String getServiceDescription() {
        return "Data Access Object service to manage external connections and clients.";
    }

    /** update configuration of this Service to add registrations, services, servers, ... */
    public void updateConfig(DistConfig newCfg) {
    }
    /** change values in configuration bucket */
    public void initializeConfigBucket(DistConfigBucket bucket) {
        // INITIALIZE DAOs
    }
    /** run after initialization */
    public void afterInitialization() {

    }
    /** additional web API endpoints */
    protected DistWebApiProcessor additionalWebApiProcessor() {
        return new DistWebApiProcessor(getServiceType())
                .addHandlerGet("info", (m, req) -> req.responseOkJsonSerialize(getInfo()))
                .addHandlerGet("dao-infos", (m, req) -> req.responseOkJsonSerialize(getDaoInfos()))
                .addHandlerGet("dao-info", (m, req) -> req.responseOkJsonSerialize(daoInfo(req.getParamOne())))
                .addHandlerPost("dao-create", (m, req) -> req.responseOkJsonSerialize(daoCreateFromJson(req.getContentAsString())))
                .addHandlerGet("dao-simple-info", (m, req) -> req.responseOkJsonSerialize(daoSimpleInfo(req.getParamOne())))
                .addHandlerPost("dao-close", (m, req) -> req.responseOkJsonSerialize(daoClose(req.getParamOne())))
                .addHandlerPost("dao-reconnect", (m, req) -> req.responseOkJsonSerialize(daoReconnect(req.getParamOne())))
                .addHandlerPost("dao-test", (m, req) -> req.responseOkJsonSerialize(daoTest(req.getParamOne())))
                .addHandlerGet("keys", (m, req) -> req.responseOkJsonSerialize(daos.values().stream().map(d -> d.getGuid()).toList()))
                .addHandlerGet("producer-keys", (m, req) -> req.responseOkJsonSerialize(getDaoProducerKeys()));
    }
    /** read configuration and re-initialize this component */
    protected boolean onReinitialize() {
        // TODO: implement reinitialization for DAO

        return true;
    }

    /** get DAO by guid or empty */
    public Optional<Dao> getDaoOrEmpty(String daoGuid) {
        Dao d = daos.get(daoGuid);
        if (d != null) {
            return Optional.of(d);
        } else {
            return Optional.empty();
        }
    }
    /** close DAO */
    public List<AgentDaoSimpleInfo> daoClose(String daoGuid) {
        touch();
        log.info("Try to close DAO by GUID: " + daoGuid);
        createEvent("daoClose", "DAO_CLOSE", daoGuid);
        return getDaoOrEmpty(daoGuid).stream().map(d -> {
            d.closeDao();
            return d.getSimpleInfo();
        }).toList();
    }
    /** info for DAO */
    public List<AgentDaoInfo> daoInfo(String daoGuid) {
        return getDaoOrEmpty(daoGuid).stream().map(d -> d.getInfo()).toList();
    }
    /** simple info for DAO */
    public List<AgentDaoSimpleInfo> daoSimpleInfo(String daoGuid) {
        return getDaoOrEmpty(daoGuid).stream().map(d -> d.getSimpleInfo()).toList();
    }
    /** re-initialize DAO, reconnect */
    public Map<String, Object> daoCreateFromJson(String bodyJson) {
        try {
            touch();
            createEvent("daoCreateFromJson");
            log.info("Try to create DAO with parameters for agent: " + parentAgent.getAgentGuid() + ", current DAOs: " + daos.size());
            Map<String, Object> props = JsonUtils.deserializeToMapOfObjects(bodyJson);
            DaoParams params = DaoParams.fromMap(props);
            Optional<Dao> dao = createDao(params.getKey(), params);
            return dao.stream().map(x -> x.testDao()).findFirst().orElse(Map.of("exists", false));
        } catch (Exception ex) {
            log.warn("Cannot create DAO for parameters, agent: " + parentAgent.getAgentGuid() + ", reason: " + ex.getMessage(), ex);
            addIssueToAgent("daoCreateFromJson", ex);
            return Map.of("status", "Exception", "reason", ex.getMessage());
        }
    }
    /** re-initialize DAO, reconnect */
    public Map<String, Object> daoReconnect(String daoGuid) {
        //return getDaoOrEmpty(daoGuid).stream().map(d -> d.re()).toList();
        createEvent("daoReconnect", "", daoGuid);
        touch();
        Dao d = daos.get(daoGuid);
        if (d != null) {
            return d.reinitializeDao();
        } else {
            return Map.of("exists", false);
        }
    }


    /** test connectivity of one DAO */
    public Map<String, Object> daoTest(String daoGuid) {
        touch("daoTest");
        Dao d = daos.get(daoGuid);
        createEvent("daoTest", "DAO_TEST", daoGuid);
        if (d != null) {
            return d.testDao();
        } else {
            return Map.of("exists", false);
        }
    }
    /** get all keys for DAO producers */
    public Set<String> getDaoProducerKeys() {
        return daoProducers.keySet();
    }
    /** get DAO info objects for all initialized DAOs*/
    public List<AgentDaoInfo> getDaoInfos() {
        log.info("!!!!!!!!!!!!!!!!!!!! DAO info - current daos: " + daos.size());
        return daos.values().stream().map(d -> d.getInfo()).toList();
    }
    /** get all DAOs for all types */
    public List<Dao> getAllDaos() {
        return daos.values().stream().toList();
    }
    /** get DAO for key and class */
    public <T extends Dao> Optional<T> getOrCreateDao(Class<T> daoClass, DaoParams params) {
        try {
            touch("getOrCreateDao");
            log.info("Try to get or create DAO for key=" + params.getKey() + ", DAO type: " + params.getDaoType().name());
            return getOrCreateDaoObject(daoClass, params);
        } catch (Exception ex) {
            log.warn("Could not create DAO for class:" + daoClass.getName() + ", reason: " + ex.getMessage());
            return Optional.empty();
        }
    }
    /** get DAO for key and class
     * If DAO cannot be created than throw Exception  */
    public <T extends Dao> T getOrCreateDaoOrError(Class<T> daoClass, DaoParams params) throws IllegalArgumentException {
        Optional<T> dao = getOrCreateDaoObject(daoClass, params);
        if (dao.isPresent()) {
            return dao.get();
        } else {
            throw new IllegalArgumentException("Cannot create DAO");
        }
    }
    /** get DAO for key and class */
    private <T extends Dao> Optional<T> getOrCreateDaoObject(Class<T> daoClass, DaoParams params) {
        touch("getOrCreateDaoObject");
        readCount.incrementAndGet();
        Optional<T> daoOrEmpty = getDao(params.getKey(), daoClass);
        return daoOrEmpty.or(() -> createDao(params.getKey(), daoClass, params));
    }
    /** register a function to produce DAO object for given key and parameters */
    public void registerDaoProducer(String className, Function<DaoParams, Dao> producer) {
        touch("registerDaoProducer");
        log.info("Registering new DAO producer for class: " + className);
        daoProducers.put(className, producer);
    }
    /** register a function to produce DAO object for given key and parameters */
    public <T> void registerDaoProducer(Class<T> cl, Function<DaoParams, Dao> producer) {
        touch("registerDaoProducer");
        log.info("Registering new DAO producer for class: " + cl.getName());
        daoProducers.put(cl.getName(), producer);
    }
    /** get number of DAOs already defined */
    public int getDaosCount() {
        return daos.size();
    }
    /** get all DAOs for given type  */
    public List<Dao> getDaosByType(DistDaoType daoType) {
        return daos.values().stream()
                .filter(x -> x.getParams().getDaoType() == daoType)
                .collect(Collectors.toList());
    }
    /** get or create DAO */
    public <T extends Dao> Optional<T> getDao(String key, Class<T> daoClass) {
        T dao = (T)daos.get(key);
        if (dao == null) {
            return Optional.empty();
        }
        return Optional.of(dao);
    }
    /** create NEW dao and put it into DAO map */
    public <T extends Dao> Optional<T> createDao(String key, Class<T> daoClass, DaoParams params) {
        var producer = daoProducers.get(daoClass.getName());
        if (producer == null) {
            return Optional.empty();
        };
        createEvent("createDao", "DAO_CREATE", key);
        var newDao = (T)producer.apply(params);
        if (AgentComponent.class.isInstance(newDao)) {
            addComponent((AgentComponent)newDao);
        }
        createdCount.incrementAndGet();
        log.info("Created new DAO for key: " + key + ", class: " + daoClass.getName());
        daos.put(key, newDao);
        return Optional.of(newDao);
    }
    /** create NEW dao and put it into DAO map */
    public Optional<Dao> createDao(String key, DaoParams params) {
        touch("createDao");
        var producer = daoProducers.get(params.getDaoType().getClassName());
        if (producer == null) {
            return Optional.empty();
        };
        var newDao = producer.apply(params);
        log.info("Created new DAO for key: " + key + ", class: " + params.getDaoType().getClassName());
        daos.put(key, newDao);
        return Optional.of(newDao);
    }

    /** get or create Kafka DAO */
    public Dao createKafkaDao(DaoParams params) {
        touch("createKafkaDao");
        return new DaoKafkaBase(params, parentAgent);
    }
    /** get or create JDBC DAO */
    public Dao createJdbcDao(DaoParams params) {
        touch("createJdbcDao");
        return new DaoJdbcBase(params, parentAgent);
    }
    /** get or create DAO for Elasticsearch */
    private Dao createElasticsearchDao(DaoParams params) {
        touch("createElasticsearchDao");
        // get elastic DAO
        return new DaoElasticsearchBase(params, parentAgent);
    }
    /** create all DAOs for given names with parameters */
    public void createDaos(Map<String, DaoParams> daos) {
        touch("createDaos");
        daos.entrySet().stream().forEach(dp -> {
            createDao(dp.getKey(), dp.getValue());
        });
    }
    /** get info about DAOs */
    public AgentDaosInfo getInfo() {
        return new AgentDaosInfo(daos.values().stream().map(Dao::getInfo).collect(Collectors.toList()), daoProducers.keySet());
    }
    /** close all DAOs, clear the map with DAOs  */
    protected void onClose() {
        synchronized (daos) {
            log.info("Closing DAOs for agent: " + parentAgent.getAgentGuid() +", count: " + daos.size());
            daos.values().stream().forEach(Dao::closeDao);
            daos.clear();
        }
    }

}
