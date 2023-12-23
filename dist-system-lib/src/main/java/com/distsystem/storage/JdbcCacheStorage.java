package com.distsystem.storage;

import com.distsystem.api.*;
import com.distsystem.api.enums.CacheStorageType;
import com.distsystem.api.info.CacheObjectInfo;
import com.distsystem.base.CacheStorageBase;
import com.distsystem.dao.DaoJdbcBase;
import com.distsystem.interfaces.Cache;
import com.distsystem.jdbc.JdbcDialect;

import java.util.*;
import java.util.stream.Collectors;

/** cache with JDBC connection to any compliant database
 * it would create special table with cache items and index to fast access
 * */
public class JdbcCacheStorage extends CacheStorageBase {

    /** DAO with DBCP to database */
    private final DaoJdbcBase dao;
    /** JDBC dialect with SQL queries for different database management systems */
    private final JdbcDialect dialect;
    private String jdbcUrl;

    /** initialize JDBC storage */
    public JdbcCacheStorage(Cache cache) {
        super(cache);
        jdbcUrl = cache.getConfig().getProperty(DistConfig.AGENT_CACHE_STORAGE_JDBC_URL);
        var jdbcDriver = cache.getConfig().getProperty(DistConfig.AGENT_CACHE_STORAGE_JDBC_DRIVER);
        var jdbcUser = cache.getConfig().getProperty(DistConfig.AGENT_CACHE_STORAGE_JDBC_USER, "");
        var jdbcPass = cache.getConfig().getProperty(DistConfig.AGENT_CACHE_STORAGE_JDBC_PASS, "");
        var jdbcDialect = cache.getConfig().getProperty(DistConfig.AGENT_CACHE_STORAGE_JDBC_DIALECT, "");
        // get dialect by driver class and dialect name
        dialect = JdbcDialect.getDialect(jdbcDriver, jdbcDialect);
        log.info(" ========================= Initializing JdbcStorage with URL: " + jdbcUrl + ", dialect: " + dialect);
        var initConnections = cache.getConfig().getPropertyAsInt(DistConfig.AGENT_REGISTRATION_OBJECT_JDBC_INIT, 2);
        var maxActiveConnections = cache.getConfig().getPropertyAsInt(DistConfig.AGENT_REGISTRATION_OBJECT_JDBC_MAXCONNECTIONS, 10);
        DaoParams params = DaoParams.jdbcParams(jdbcUrl, jdbcDriver, jdbcUser, jdbcPass, initConnections, maxActiveConnections);
        dao = cache.getAgent().getAgentDao().getOrCreateDaoOrError(DaoJdbcBase.class, params);
        dao.usedByComponent(this);
        initializeConnectionAndCreateTables();
    }

    /** read configuration and re-initialize this component */
    public boolean componentReinitialize() {
        // nothing to be done here
        return true;
    }
    /** create table with cache items if not exists */
    private void initializeConnectionAndCreateTables() {
        dao.checkAndCreateModel(DaoModel.cacheItem);
    }

    /** JDBC is external storage */
    public  boolean isInternal() { return false; }
    /** returns true if storage is global,
     * it means that one global shared storage is available for all cache instances*/
    public boolean isGlobal() { return true; }

    /** get additional info parameters for this storage */
    public Map<String, Object> getStorageAdditionalInfo() {
        return Map.of("jdbcUrl", jdbcUrl,
                "activeConnections", dao.getActiveConnections(),
                "idleConnections", dao.getIdleConnections(),
                " dialectName", dialect.dialectName);
    }
    /** returns true if JDBC database is connected and there is cache table */
    public boolean isOperable() {
        try {
            return dao.isConnected() && dao.executeSelectQuery(dialect.selectCacheTables()).size() == 1;
        } catch (Exception ex) {
            return false;
        }
    }
    /** get type of this storage */
    public CacheStorageType getStorageType() {
        return CacheStorageType.jdbc;
    }

    /** check if object has given key, optional with specific type */
    public boolean contains(String key) {
        return dao.executeSelectForNameModelExists(DaoModel.authAccount, key);
    }

    /** get CacheObject item from JDBC */
    public Optional<CacheObject> getObject(String key) {
        //CacheObject.fromSerialized();
        return dao.executeSelectQueryOptional(dialect.selectCacheItemsByKey(), new Object[] { key }, x -> CacheObjectSerialized.fromMapToCacheObject(x, distSerializer));
    }

    /** set object */
    public  Optional<CacheObject> setObject(CacheObject o) {
        log.debug(" CACHE JDBC SET OBJECT");
        CacheObjectSerialized cos = o.serializedFullCacheObject(distSerializer);
        var createDate = new java.util.Date();
        var endDate = new java.util.Date(createDate.getTime()+cos.getTimeToLiveMs());

        dao.executeUpdateQuery(dialect.insertUpdateCacheItem(), new Object[] {
                cos.getKey(), cos.getObjectInCache(), cos.getObjectClassName(), // cachekey, cachevalue, objectclassname,
                createDate, getCacheUid(), createDate, //  inserteddate, cacheguid, lastusedate,
                endDate, cos.getCreatedTimeMs(), cos.getObjectSeq(), cos.getObjSize(), cos.getAcquireTimeMs(), //  enddate, createdtimems, objectseq, objsize, acquiretimems,
                cos.getMode().ordinal(), cos.getPriority(), cos.getGroupsList()}); // cachemode, cachepriority, groupslist
        return Optional.empty();
    }

    /** remove objects in cache storage by keys */
    public void removeObjectsByKeys(Collection<String> keys) {
        var sqlParams = String.join(",", keys.stream().map(x -> "?").collect(Collectors.toList()));
        dao.executeUpdateQuery("delete from DistAgentCacheItem where cachekey in (" + sqlParams + ")", keys.toArray());
    }
    /** remove object in cache storage by key */
    public void removeObjectByKey(String key) {
        dao.executeUpdateQuery("delete from DistAgentCacheItem where cachekey=?", new Object[] { key });
    }
    /** get number of items in cache */
    public int getItemsCount() {
        return dao.executeSelectQuerySingle("select sum(objsize) as cnt from DistAgentCacheItem").getIntOrZero("cnt");
    }
    /** get number of objects in this cache */
    public int getObjectsCount() {
        return dao.executeSelectQuerySingle("select count(*) as cnt from DistAgentCacheItem").getIntOrZero("cnt");
    }
    /** get keys for all cache items */
    public Set<String> getKeys(String containsStr) {
        var rows = dao.executeSelectQuery("select cacheKey as key from DistAgentCacheItem where cachekey like ?", new Object[] { "%" + containsStr + "%" });
        log.info("Got cache keysfor JDBC storage: " + rows.size() + ", contains: " + containsStr);
        return rows.stream().map(x -> ""+x.get("key")).collect(Collectors.toSet());
    }
    /** get info values */
    public List<CacheObjectInfo> getInfos(String containsStr) {
        return getValues(containsStr).stream().map(CacheObject::getInfo).collect(Collectors.toList());
    }
    /** get values of cache objects that contains given String in key */
    public List<CacheObject> getValues(String containsStr) {
        log.info("GET VALUES -=======> " + dialect.selectFindCacheItems());
        return dao.executeSelectQueryFromOptionMap(dialect.selectFindCacheItems(),
                new Object[] { "%" + containsStr + "%" },
                rowMap -> CacheObjectSerialized.fromMapToCacheObject(rowMap, distSerializer));
    }
    /** clear caches for given group */
    public int clearCacheForGroup(String groupName) {
        return dao.executeUpdateQuery("delete from DistAgentCacheItem where groupslist like ?", new Object[] { "%," + groupName + ",%" });
    }
    /** clear cache contains given partial key */
    public int clearCacheContains(String str) {
        return dao.executeUpdateQuery("delete from DistAgentCacheItem where cachekey like ?", new Object[] { "%" + str + "%" });
    }

    /** clear cache by given mode
     * returns estimated of elements cleared */
    public int clearCache(CacheClearMode clearMode) {
        // TODO: implement clearing cache for given mode
        if (clearMode == CacheClearMode.ALL_ELEMENTS) {

        } else {

        }
        return -1;
    }
    /** check cache every X seconds to clear TTL caches */
    public void onTimeClean(long checkSeq) {
        dao.executeAnyQuery("delete from DistAgentCacheItem where enddate < now()", new Object[0]);
    }
    public void disposeStorage() {
        if (dao != null) {
            dao.close();
        }
    }
}
