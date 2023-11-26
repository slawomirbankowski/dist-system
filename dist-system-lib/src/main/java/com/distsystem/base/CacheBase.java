package com.distsystem.base;

import com.distsystem.api.*;
import com.distsystem.api.enums.DistComponentType;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.api.info.CacheInfo;
import com.distsystem.encoders.KeyEncoderNone;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.Cache;
import com.distsystem.interfaces.CacheKeyEncoder;
import com.distsystem.utils.CacheStats;
import com.distsystem.utils.DistUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/** manager to connect all storages, policies, agents
 * to perform clean based on time
 * replace all cache with fresh objects
 * */
public abstract class CacheBase extends ServiceBase implements Cache {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(CacheBase.class);

    /** stats about this cache manager - long term diagnostic information
     * statistics can be persisted and continued when agent is on
     * statistics could be shared with other agents to make global key statistics to beter manage cache objects
     * */
    protected final CacheStats cacheStats = new CacheStats();

    /** default mode for cache objects added without mode */
    protected CacheMode defaultMode = CacheMode.modeTtlTenSeconds;

    /** key encoder to hide passwords and secrets in keys */
    protected CacheKeyEncoder keyEncoder;
    /** policy to add cache Objects to storages and changing mode, ttl, priority, tags */
    protected CachePolicy policy;

    /** initialize current manager with properties
     * this is creating storages, connecting to storages
     * creating cache policy, create agent and connecting to other cache agents */
    public CacheBase(Agent parentAgent) {
        super(parentAgent);
    }

    /** create new service UID for this service */
    protected String createGuid() {
        return DistUtils.generateCacheGuid();
    }

    /** create new message builder starting this agent */
    public DistMessageBuilder createMessageBuilder() {
        return DistMessageBuilder.empty().fromService(this);
    }

    /** get type of service: cache, measure, report, */
    public DistServiceType getServiceType() {
        return DistServiceType.cache;
    }

    /** get type of this component */
    public DistComponentType getComponentType() {
        return DistComponentType.cache;
    }
    /** get key encoder - this is a class to encode key to protect passwords, secrets of a key */
    public CacheKeyEncoder getKeyEncoder() {
        return keyEncoder;
    }

    /** get value of cache configuration */
    public String getConfigValue(String cfgName) {
        return getConfig().getProperty(cfgName);
    }
    /** get unique identifier for this CacheManager object */
    public String getCacheGuid() { return guid; }
    /** get item from cache as String if exists or None */
    public String getCacheObjectAsString(String key) {
        Optional<CacheObject> co = getCacheObject(key);
        if (co.isPresent()) {
            return co.get().getValue().toString();
        } else {
            return "";
        }
    }
    /** get info about cache */
    public CacheInfo getCacheInfo() {
        return new CacheInfo(guid, createDate, cacheStats.checksCount(),
                cacheStats.addedItemsCount(), closed,
            getAgent().getIssues().getIssues().size(), getAgent().getEvents().getEvents().size(),
            getItemsCount(), getObjectsCount(), getStoragesInfo());
    }
    /** get custom map of info about service */
    public Map<String, String> getServiceInfoCustomMap() {
        return Map.of("keyEncoder", keyEncoder.getClass().getName());
    }

    /** initialize key encoder to encode secrets */
    protected void initializeEncoder() {
        // initialize encoder for secrets and passwords in key
        keyEncoder = new KeyEncoderNone();
        // TODO: finish initialization of encoder based on configuration
    }

    /** initialize serializer used for serialization of an object into byte[] or String to be saved in external storages */
    protected void initializeSerializer() {
        String serializerDef = getConfig().getProperty(DistConfig.AGENT_SERIALIZER_DEFINITION, DistConfig.AGENT_SERIALIZER_DEFINITION_SERIALIZABLE_VALUE);
        //serializer = new ComplexSerializer(serializerDef);
        // TODO: finish initialization of serializer based on configuration
    }

    /** add issue to cache manager to be revoked by parent
     * issue could be Exception, Error, problem with connecting to storage,
     * internal error, not consistent state that is unknown and could be used by parent manager */
    public void addIssue(DistIssue issue) {
        cacheStats.addIssue();
        getAgent().getIssues().addIssue(issue);
    }
    /** add issue with method and exception */
    public void addIssue(String methodName, Exception ex) {
        addIssue(new DistIssue(this, methodName, ex));
    }
    /** add new event and distribute it to callback methods,
     * event could be useful information about change of cache status, new connection, refresh of cache, clean */
    protected void addEvent(AgentEvent event) {
        getAgent().getEvents().addEvent(event);
    }
    /** set new callback method for events for given type */
    public void setCallback(String eventType, Function<AgentEvent, String> callback) {
        getAgent().getEvents().setCallback(eventType, callback);
    }
    /** set object to cache */
    public CacheSetBack setCacheObject(String key, Object value, CacheMode mode) {
        return setCacheObject(key, value, mode, Collections.emptySet());
    }
    public CacheSetBack setCacheObject(String key, Object value) {
        return setCacheObject(key, value, CacheMode.modeKeep, Collections.emptySet());
    }
    /** get all recent issues with cache */
    public Queue<DistIssue> getIssues() {
        return getAgent().getIssues().getIssues();
    }
    /** get all recent events added to cache */
    public Queue<AgentEvent> getEvents() {
        return getAgent().getEvents().getEvents();
    }

    public <T> T withCache(String key, Supplier<? extends T> supplier) {
        return withCache(key, supplier, defaultMode, Collections.emptySet());
    }
    public <T> T withCache(String key, Supplier<? extends T> supplier, CacheMode mode) {
        return withCache(key, supplier, mode, Collections.emptySet());
    }
    public <T> T withCache(String key, Supplier<? extends T> supplier, Set<String> groups) {
        return withCache(key, supplier, defaultMode, groups);
    }

    public <T> T withCache(String key, Function<String, ? extends T> mapper) {
        return withCache(key, mapper, defaultMode, Collections.emptySet());
    }
    public <T> T withCache(String key, Function<String, ? extends T> mapper, CacheMode mode) {
        return withCache(key, mapper, mode, Collections.emptySet());
    }
    public <T> T withCache(String key, Function<String, ? extends T> mapper, Set<String> groups) {
        return withCache(key, mapper, defaultMode, groups);
    }

}
