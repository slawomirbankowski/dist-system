package com.distsystem.storage;

import com.distsystem.api.*;
import com.distsystem.api.enums.CacheStorageType;
import com.distsystem.api.info.CacheObjectInfo;
import com.distsystem.base.CacheStorageBase;
import com.distsystem.dao.DaoElasticsearchBase;
import com.distsystem.interfaces.Cache;
import com.distsystem.utils.DistUtils;

import java.util.*;
import java.util.stream.Collectors;

/** cache with Elasticsearch index - need to connect to Elasticsearch,
 * create index and read/write items from/to cache
 * */
public class ElasticsearchCacheStorage extends CacheStorageBase {

    /** URL to connect to Elasticsearch */
    private String elasticsearchUrl;
    private String elasticUser;
    private String elasticPass;
    private String elasticIndex;
    /** DAO to Elasticsearch for CRUD operations */
    private DaoElasticsearchBase dao;

    /** default header for Elasticsearch requests */
    private Map<String, String> header;

    /** init Elasticsearch storage */
    public ElasticsearchCacheStorage(Cache cache) {
        super(cache);
        initialize();
    }
    private void initialize() {
        elasticsearchUrl = cache.getConfig().getProperty(DistConfig.AGENT_CACHE_STORAGE_ELASTICSEARCH_URL, "http://localhost:9200");
        elasticUser = cache.getConfig().getProperty(DistConfig.AGENT_CACHE_STORAGE_ELASTICSEARCH_USER, "");
        elasticPass = cache.getConfig().getProperty(DistConfig.AGENT_CACHE_STORAGE_ELASTICSEARCH_PASS, "");
        elasticIndex = cache.getConfig().getProperty(DistConfig.AGENT_CACHE_STORAGE_ELASTICSEARCH_INDEX, DistConfig.AGENT_CACHE_STORAGE_ELASTICSEARCH_INDEX_DEFAULT_VALUE);
        if (!elasticUser.isEmpty()) {
            header = Map.of("Content-Type", "application/json", "Authorization", DistUtils.getBasicAuthValue(elasticUser, elasticPass));
        } else {
            header = Map.of("Content-Type", "application/json");
        }
        log.debug("Connection to Elasticsearch storage URL: " + elasticsearchUrl + ", index with cache objects: " + elasticIndex);
        dao = cache.getAgent().getAgentDao().getOrCreateDaoOrError(DaoElasticsearchBase.class, DaoParams.elasticsearchParams(elasticsearchUrl, elasticUser, elasticPass));
        dao.usedByComponent(this);
        dao.createIndicesWithCheck(Set.of(elasticIndex));
    }

    /** read configuration and re-initialize this component */
    public boolean componentReinitialize() {

        // TODO: reinitialize this component
        return true;
    }
    /** Elasticsearch is external storage */
    public boolean isInternal() { return false; }
    /** returns true if storage is global,
     * it means that one global shared storage is available for all cache instances*/
    public boolean isGlobal() { return true; }
    /**  get additional info parameters for this storage */
    public Map<String, Object> getStorageAdditionalInfo() {
        var ci = dao.getClusterInfo();
        ElasticClusterInfo clusterInfo = ci.stream().findFirst().orElseGet(() -> new ElasticClusterInfo());
        ElasticIndexInfo index = dao.getIndexByName(elasticIndex).stream().findFirst().orElseGet(() -> new ElasticIndexInfo());
        return Map.of("elasticIndex", elasticIndex,
                "connected", ci.size() > 0,
                "host", clusterInfo.getHost(),
                "ip", clusterInfo.getIp(),
                "health", index.getHealth(),
                "status", index.getStatus());
    }
    /** get type of this storage */
    public CacheStorageType getStorageType() {
        return CacheStorageType.elasticsearch;
    }
    /** check if object has given key, optional with specific type */
    public boolean contains(String key) {
        return dao.getDocument(elasticIndex, encodeKey(key)).isPresent();
    }
    /** get item from Elasticsearch */
    public Optional<CacheObject> getObject(String key) {
        var foundDoc = dao.getDocument(elasticIndex, encodeKey(key));
        return parseCacheObject(foundDoc);
    }
    /** */
    private Optional<CacheObject> parseCacheObject(Optional<ElasticDocumentInfo> docInfo) {
        if (docInfo != null && docInfo.isPresent()) {
            return parseCacheObject(docInfo.get());
        } else {
            return Optional.empty();
        }
    }
    /** parse Cache object from Elasticsearch document */
    private Optional<CacheObject> parseCacheObject(ElasticDocumentInfo docInfo) {
        var map = docInfo.get_source();
        if (map == null) {
            return Optional.empty();
        }
        return CacheObjectSerialized.fromMapToCacheObject(map, distSerializer);
    }
    /** put document to Elasticsearch with CacheObject */
    public Optional<CacheObject> setObject(CacheObject o) {
        var prevObj = getObject(encodeKey(o.getKey()));
        CacheObjectSerialized cos = o.serializedFullCacheObject(distSerializer);
        dao.addOrUpdateDocument(elasticIndex, encodeKey(o.getKey()), cos.getSerializedMap(distSerializer));
        return prevObj;
    }
    /** remove objects in cache storage by keys */
    public void removeObjectsByKeys(Collection<String> keys) {
        keys.stream().forEach(key -> dao.deleteDocument(elasticIndex, encodeKey(key)));
    }
    /** remove object in cache storage by key */
    public void removeObjectByKey(String key) {
        dao.deleteDocument(elasticIndex, encodeKey(key));
    }
    /** get number of items in cache */
    public int getItemsCount() {
        return getObjectsCount();
    }
    /** get number of objects in this cache */
    public int getObjectsCount() {
        return dao.getIndexByName(elasticIndex).stream().mapToInt(ElasticIndexInfo::getDocsCount).sum();
    }
    /** get keys for all cache items */
    public Set<String> getKeys(String containsStr) {
        return getValues(containsStr).stream().map(CacheObject::getKey).collect(Collectors.toSet());
    }
    /** get info values */
    public List<CacheObjectInfo> getInfos(String containsStr) {
        return getValues(containsStr).stream().map(CacheObject::getInfo).collect(Collectors.toList());
    }
    /** get values of cache objects that contains given String in key */
    public List<CacheObject> getValues(String containsStr) {
        var searchRes = dao.searchComplex(elasticIndex, "wildcard", "key", "*" + containsStr + "*");
        var documents = searchRes
                .stream()
                .flatMap(x -> x.getDocuments().stream())
                .flatMap(doc -> parseCacheObject(doc).stream())
                .collect(Collectors.toList());
        return documents;
    }
    /** clear caches with given clear cache */
    public int clearCacheForGroup(String groupName) {
        // TODO: implements
        return 1;
    }
    /** clear cache by given mode
     * returns estimated of elements cleared */
    public int clearCache(CacheClearMode clearMode) {
        return -1;
    }
    /** clear cache contains given partial key */
    public int clearCacheContains(String str) {
        return dao.deleteByQuery(elasticIndex, "wildcard", Map.of("key", "*" + str + "*")).stream().mapToInt(x-> x.getDeleted()).sum();
    }
    /** check cache every X seconds to clear TTL caches */
    public void onTimeClean(long checkSeq) {
    }
}

class ElasticsearchIndex {

}