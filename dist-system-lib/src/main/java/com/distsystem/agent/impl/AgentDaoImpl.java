package com.distsystem.agent.impl;

import com.distsystem.api.*;
import com.distsystem.api.enums.DistDaoType;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.api.info.AgentDaoInfo;
import com.distsystem.api.info.AgentDaoSimpleInfo;
import com.distsystem.api.info.AgentDaosInfo;
import com.distsystem.base.ServiceBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.Dao;
import com.distsystem.dao.DaoElasticsearchBase;
import com.distsystem.dao.DaoJdbcBase;
import com.distsystem.dao.DaoKafkaBase;
import com.distsystem.interfaces.AgentDao;
import com.distsystem.utils.DistWebApiProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

/** manager for DAOs - all external data access objects to JDBC, Kafka, Redis, Elasticsearch */
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
    /** sequence for getting DAO object */
    private final AtomicLong getSequence = new AtomicLong();
    /** created counter */
    private final AtomicLong createdCount = new AtomicLong();

    /** create new DAO manager */
    public AgentDaoImpl(Agent parentAgent) {
        super(parentAgent);
        parentAgent.getAgentServices().registerService(this);
    }
    /** get type of service: cache, measure, report, flow, space, ... */
    public DistServiceType getServiceType() {
        return DistServiceType.daos;
    }
    /** process message, returns message with status */
    public DistMessage processMessage(DistMessage msg) {
        return msg.notSupported();
    }

    /** update configuration of this Service to add registrations, services, servers, ... */
    public void updateConfig(DistConfig newCfg) {
    }

    /** additional web API endpoints */
    protected DistWebApiProcessor additionalWebApiProcessor() {
        return new DistWebApiProcessor(getServiceType())
                .addHandlerGet("info", (m, req) -> req.responseOkJsonSerialize(getInfo()))
                .addHandlerGet("dao-infos", (m, req) -> req.responseOkJsonSerialize(getDaoInfos()))
                .addHandlerGet("dao-info", (m, req) -> req.responseOkJsonSerialize(daoInfo(req.getParamOne())))
                .addHandlerPost("dao-close", (m, req) -> req.responseOkJsonSerialize(daoClose(req.getParamOne())))
                .addHandlerPost("dao-reconnect", (m, req) -> req.responseOkJsonSerialize(daoReconnect(req.getParamOne())))
                .addHandlerPost("dao-test", (m, req) -> req.responseOkJsonSerialize(daoTest(req.getParamOne())))
                .addHandlerGet("keys", (m, req) -> req.responseOkJsonSerialize(daos.values().stream().map(d -> d.getGuid())))
                .addHandlerGet("producer-keys", (m, req) -> req.responseOkJsonSerialize(getDaoProducerKeys()));
    }
    /** read configuration and re-initialize this component */
    public boolean reinitialize() {
        // TODO: implement reinitialization
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
    /** */
    public String daoReconnect(String daoGuid) {
        //return getDaoOrEmpty(daoGuid).stream().map(d -> d.re()).toList();
        return "";
    }
    /** */
    public String daoTest(String daoGuid) {


        return "";
    }
    /** */
    public Set<String> getDaoProducerKeys() {
        return daoProducers.keySet();
    }
    /** */
    public List<AgentDaoInfo> getDaoInfos() {
        log.info("!!!!!!!!!!!!!!!!!!!! DAO info - current daos: " + daos.size());
        return daos.values().stream().map(d -> d.getInfo()).toList();
    }
    /** get DAO for key and class */
    public <T extends Dao> Optional<T> getOrCreateDao(Class<T> daoClass, DaoParams params) {
        try {
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
        getSequence.incrementAndGet();
        Optional<T> daoOrEmpty = getDao(params.getKey(), daoClass);
        return daoOrEmpty.or(() -> createDao(params.getKey(), daoClass, params));
    }
    /** register a function to produce DAO object for given key and parameters */
    public void registerDaoProducer(String className, Function<DaoParams, Dao> producer) {
        log.info("Registering new DAO producer for class: " + className);
        daoProducers.put(className, producer);
    }
    /** register a function to produce DAO object for given key and parameters */
    public <T> void registerDaoProducer(Class<T> cl, Function<DaoParams, Dao> producer) {
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
        var newDao = (T)producer.apply(params);
        createdCount.incrementAndGet();
        log.info("Created new DAO for key: " + key + ", class: " + daoClass.getName());
        daos.put(key, newDao);
        return Optional.of(newDao);
    }
    /** create NEW dao and put it into DAO map */
    public Optional<Dao> createDao(String key, DaoParams params) {
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
        return new DaoKafkaBase(params, parentAgent);
    }
    /** get or create JDBC DAO */
    public Dao createJdbcDao(DaoParams params) {
        return new DaoJdbcBase(params, parentAgent);
    }
    /** get or create DAO for Elasticsearch */
    private Dao createElasticsearchDao(DaoParams params) {
        // get elastic DAO
        return new DaoElasticsearchBase(params, parentAgent);
    }
    /** create all DAOs for given names with parameters */
    public void createDaos(Map<String, DaoParams> daos) {
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
