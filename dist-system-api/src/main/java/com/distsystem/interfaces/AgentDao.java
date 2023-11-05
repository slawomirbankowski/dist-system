package com.distsystem.interfaces;

import com.distsystem.api.DaoParams;
import com.distsystem.api.enums.DistDaoType;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/** interface for DAO manager to keep DAO objects for JDBC, Kafka, Elasticsearch, Redis, MongoDB and others
 * */
public interface AgentDao extends DistService {

    /** get DAO for key and class */
    <T extends Dao> Optional<T> getOrCreateDao(Class<T> daoClass, DaoParams params);
    /** get DAO for key and class
     * If DAO cannot be created than throw Exception  */
    <T extends Dao> T getOrCreateDaoOrError(Class<T> daoClass, DaoParams params) throws IllegalArgumentException;
    /** create all DAOS for given names with parameters */
    void createDaos(Map<String, DaoParams> daos);
    /** register a function to produce DAO object for given key and parameters */
    void registerDaoProducer(String className, Function<DaoParams, Dao> producer);
    /** register a function to produce DAO object for given key and parameters */
    <T> void registerDaoProducer(Class<T> cl, Function<DaoParams, Dao> producer);
    /** get number of DAOs already defined */
    int getDaosCount();
    /** get all DAOs for given type  */
    List<Dao> getDaosByType(DistDaoType daoType);

}
