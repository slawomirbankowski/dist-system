package com.distsystem.agent.impl;

import com.distsystem.api.DaoParams;
import com.distsystem.api.enums.DistComponentType;
import com.distsystem.api.enums.DistDaoType;
import com.distsystem.api.info.AgentDaosInfo;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentComponent;
import com.distsystem.interfaces.Dao;
import com.distsystem.dao.DaoElasticsearchBase;
import com.distsystem.dao.DaoJdbcBase;
import com.distsystem.dao.DaoKafkaBase;
import com.distsystem.interfaces.AgentDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

/** manager for DAOs - all external data access objects to JDBC, Kafka, Redis, Elasticsearch */
public class AgentDaoImpl extends Agentable implements AgentDao, AgentComponent {

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

    /** create new DAO manager */
    public AgentDaoImpl(Agent parentAgent) {
        super(parentAgent);
        parentAgent.addComponent(this);
    }


    /** get type of this component */
    public DistComponentType getComponentType() {
        return DistComponentType.daos;
    }
    @Override
    public String getGuid() {
        return getParentAgentGuid();
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
            throw new IllegalArgumentException("Cannot create ");
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
    public void close() {
        synchronized (daos) {
            log.info("Closing DAOs for agent: " + parentAgent.getAgentGuid() +", count: " + daos.size());
            daos.values().stream().forEach(Dao::close);
            daos.clear();
        }
    }

}
