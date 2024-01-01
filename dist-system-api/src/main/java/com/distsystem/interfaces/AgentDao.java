package com.distsystem.interfaces;

import com.distsystem.api.DaoParams;
import com.distsystem.api.enums.DistDaoType;
import com.distsystem.api.info.AgentDaoInfo;
import com.distsystem.api.info.AgentDaoSimpleInfo;
import com.distsystem.utils.AdvancedMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/** interface for DAO manager to keep DAO objects for JDBC, Kafka, Elasticsearch, Redis, MongoDB and others
 * */
public interface AgentDao extends DistService {

    /** get DAO for key and class */
    <T extends Dao> Optional<T> getOrCreateDao(Class<T> daoClass, DaoParams params);
    /** get DAO for key and class
     * If DAO cannot be created than throw Exception  */
    <T extends Dao> T getOrCreateDaoOrError(Class<T> daoClass, DaoParams params) throws IllegalArgumentException;
    /** create NEW dao and put it into DAO map */
    <T extends Dao> Optional<T> createDao(String key, Class<T> daoClass, DaoParams params);
    /** create all DAOS for given names with parameters */
    void createDaos(Map<String, DaoParams> daos);

    /** get DAO by guid or empty */
    Optional<Dao> getDaoOrEmpty(String daoGuid);
    /** close DAO */
    List<AgentDaoSimpleInfo> daoClose(String daoGuid);
    /** info for DAO */
    List<AgentDaoInfo> daoInfo(String daoGuid);
    /** simple info for DAO */
    List<AgentDaoSimpleInfo> daoSimpleInfo(String daoGuid);
    /** re-initialize DAO, reconnect */
    AdvancedMap daoReconnect(String daoGuid);
    /** get all keys for DAO producers */
    Set<String> getDaoProducerKeys();
    /** test connectivity of one DAO */
    Map<String, Object> daoTest(String daoGuid);
    /** get DAO info objects for all initialized DAOs*/
    List<AgentDaoInfo> getDaoInfos();
    /** get all DAOs for all types */
    List<Dao> getAllDaos();
    /** register a function to produce DAO object for given key and parameters */
    void registerDaoProducer(String className, Function<DaoParams, Dao> producer);
    /** register a function to produce DAO object for given key and parameters */
    <T> void registerDaoProducer(Class<T> cl, Function<DaoParams, Dao> producer);
    /** get number of DAOs already defined */
    int getDaosCount();
    /** get all DAOs for given type  */
    List<Dao> getDaosByType(DistDaoType daoType);

}
