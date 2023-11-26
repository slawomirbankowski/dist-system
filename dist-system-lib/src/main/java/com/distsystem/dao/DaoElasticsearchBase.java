package com.distsystem.dao;

import com.distsystem.api.*;
import com.distsystem.base.DaoBase;
import com.distsystem.interfaces.*;
import com.distsystem.utils.DistUtils;
import com.distsystem.utils.HttpConnectionHelper;
import com.distsystem.utils.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/** base class for any JDBC based DAO */
public class DaoElasticsearchBase extends DaoBase implements AgentComponent {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(DaoElasticsearchBase.class);

    /** URL to Elasticsearch REST API */
    private final String elasticUrl;
    private final String elasticUser;
    private final String elasticPass;
    /** HTTP client to Elasticsearch */
    private HttpCallable conn;
    private Map<String, String> defaultHeaders;

    /** creates new DAO to JDBC database */
    public DaoElasticsearchBase(DaoParams params, Agent agent) {
        super(params, agent);
        this.elasticUrl = resolve(params.getUrl());
        this.elasticUser = resolve(params.getUser());
        this.elasticPass = resolve(params.getPass());
        onInitialize();
    }
    /** creates new DAO to JDBC database */
    public DaoElasticsearchBase(String elasticUrl, String elasticUser, String elasticPass, Agent agent) {
        super(DaoParams.elasticsearchParams(elasticUrl, elasticUser, elasticPass), agent);
        this.elasticUrl = resolve(elasticUrl);
        this.elasticUser = resolve(elasticUser);
        this.elasticPass = resolve(elasticPass);
        onInitialize();
    }

    /** count objects in this agentable object including this object */
    public long countObjectsAgentable() {
        return 2L;
    }
    /** read configuration and re-initialize this component */
    public boolean componentReinitialize() {
        // TODO: implement reinitialization
        return true;
    }

    /** returns true if DAO is connected */
    public boolean isConnected() {
        return getClusterInfo().size() == 1;
    }

    /** test DAO and returns items */
    public Map<String, Object> testDao() {
        return Map.of("isConnected", isConnected(), "url", getUrl(), "className", this.getClass().getName());
    }

    /** get URL of this DAO */
    public String getUrl() {
        return elasticUrl;
    }

    public String getElasticUrl() {
        return elasticUrl;
    }
    public String getElasticUser() {
        return elasticUser;
    }
    public String getElasticPass() {
        return elasticPass;
    }

    public String getResolvedUrl() {
        return resolve(elasticUrl);
    }
    public String getResolvedUser() {
        return resolve(elasticUser);
    }
    public String getResolvedPass() {
        return resolve(elasticPass);
    }

    public void onInitialize() {
        try {
            log.info("Connecting to Elasticsearch, URL=" + getResolvedUrl() + ", user: " + getResolvedUser());
            defaultHeaders = Map.of("Content-Type", "application/json", "Authorization", DistUtils.getBasicAuthValue(getResolvedUser(), getResolvedPass()));
            conn = HttpConnectionHelper.createHttpClient(getResolvedUrl());
            List<ElasticClusterInfo> cluInfo = getClusterInfo();
            log.info("Connected to Elasticsearch, cluster nodes: " + cluInfo.size() + ", cluster info: " + cluInfo);
        } catch (Exception ex) {
            log.info("Cannot connect to Elasticsearch at URL:" + getResolvedUrl() + ", reason: " + ex.getMessage(), ex);
            addIssueToAgent("onInitialize", ex);
        }
    }
    /** get all indices */
    public Collection<String> getDaoStructures() {
        return getIndicesNames();
    }
    /** get Elasticsearch cluster info */
    public List<ElasticClusterInfo> getClusterInfo() {
        return conn.callGet("/_cat/master?v=true&format=json", defaultHeaders).parseOutputTo(new TypeReference<List<ElasticClusterInfo>>() {}).orElseGet(() -> List.of());
    }
    public List<ElasticIndexCreateInfo> createIndicesWithCheck(Set<String> indicesNamesToCreate) {
        var existingIndices = getIndicesNames();
        var indxs = new HashSet<String>();
        indicesNamesToCreate.stream().forEach(i -> indxs.add(i));
        indxs.removeAll(existingIndices);
        return indxs.stream().flatMap(name -> createIndex(name).stream()).collect(Collectors.toList());
    }
    /** create new default index by name */
    public Optional<ElasticIndexCreateInfo> createIndex(String indexName) {
        String createBody = JsonUtils.serialize(Map.of());
        return conn.callPut("/" + indexName , defaultHeaders, createBody).parseOutputTo(new TypeReference<ElasticIndexCreateInfo>() {});
    }
    /** delete index by name */
    public boolean deleteIndex(String indexName) {
        String createBody = JsonUtils.serialize(Map.of());
        return conn.callDelete("/" + indexName , defaultHeaders).isOk();
    }
    /** get list of all indices on that Elasticsearch */
    public List<ElasticIndexInfo> getIndices() {
        Optional<List<ElasticIndexInfo>> indices = conn.callGet("/_cat/indices?format=json", defaultHeaders).parseOutputTo(new TypeReference<List<ElasticIndexInfo>>() {});
        return indices.orElseGet(() -> List.of());
    }
    /** get index by name of EMPTY if no such index */
    public Optional<ElasticIndexInfo> getIndexByName(String name) {
        return getIndices().stream().filter(i -> i.getIndex().equals(name)).findFirst();
    }
    /** get names of all indices in the set */
    public Set<String> getIndicesNames() {
        return getIndices().stream().map(x -> x.getIndex()).collect(Collectors.toSet());
    }
    public Optional<ElasticDocumentInfo> getDocument(String indexName, String key) {
        return conn.callGet("/" + indexName + "/_doc/" + key, defaultHeaders).parseOutputTo(new TypeReference<ElasticDocumentInfo>() {});
    }
    public Optional<ElasticInsertInfo> addOrUpdateDocument(String indexName, String key, Map<String, String> doc) {
        String insertBody = JsonUtils.serialize(doc);
        return conn.callPut("/" + indexName + "/_doc/" + key, defaultHeaders, insertBody).parseOutputTo(new TypeReference<ElasticInsertInfo>() {});
    }
    public Optional<ElasticInsertInfo> deleteDocument(String indexName, String key) {
        return conn.callDelete("/" + indexName + "/_doc/" + key, defaultHeaders).parseOutputTo(new TypeReference<ElasticInsertInfo>() {});
    }
    public Optional<ElasticSearchInfo> searchSimple(String indexName, String query) {
        return conn.callGet("/" + indexName + "/_search?q=" + query, defaultHeaders).parseOutputTo(new TypeReference<ElasticSearchInfo>() {});
    }
    public Optional<ElasticSearchInfo> searchComplex(String indexName, String key, String value) {
        return searchComplex(indexName, "match", key, value);
    }

    public Optional<ElasticSearchInfo> searchComplex(String indexName, String matchType, String key, String value) {
        return searchComplex(indexName, matchType, Map.of(key, value));
    }
    /** get search complex results as list of T objects */
    public <T> List<T> searchComplexToMapsOptional(String indexName, String matchType, String key, String value, Function<Map<String, Object>, Optional<T>> mappingFun) {
        return searchComplex(indexName, matchType, key, value)
                .stream()
                .flatMap(x -> x.getMaps().stream())
                .flatMap(m -> mappingFun.apply(m).stream())
                .collect(Collectors.toList());
    }
    /** get search complex results as list of T objects */
    public <T> List<T> searchComplexToMaps(String indexName, String matchType, String key, String value, Function<Map<String, Object>, T> mappingFun) {
        return searchComplex(indexName, matchType, key, value)
                .stream()
                .flatMap(x -> x.getMaps().stream())
                .map(m -> mappingFun.apply(m))
                .collect(Collectors.toList());
    }
    public Optional<ElasticSearchInfo> searchComplex(String indexName, String matchType, Map<String, String> matchMap) {
        var searchStructure = Map.of("query", Map.of("bool", Map.of(matchType, matchMap)));
        String searchBody = JsonUtils.serialize(searchStructure);
        return conn.callPost("/" + indexName + "/_search", defaultHeaders, searchBody)
                .parseOutputTo(new TypeReference<ElasticSearchInfo>() {});
    }
    public Optional<ElasticDocumentDeleteInfo> deleteByQuery(String indexName, String matchType, Map<String, String> matchMap) {
        var searchStructure = Map.of("query", Map.of("bool", Map.of(matchType, matchMap)));
        String searchBody = JsonUtils.serialize(searchStructure);
        return conn.callDelete("/" + indexName + "/_search", defaultHeaders, searchBody)
                .parseOutputTo(new TypeReference<ElasticDocumentDeleteInfo>() {});
    }

    /** close current Elasticsearch DAO */
    protected void onClose() {
    }

    public boolean closeDao() {
        return true;
    }

}
