package com.distsystem.managers;

import com.distsystem.api.*;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.api.info.CacheObjectInfo;
import com.distsystem.api.info.StorageInfo;
import com.distsystem.api.info.StorageInfos;
import com.distsystem.base.CacheBase;
import com.distsystem.base.CacheStorageBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.api.DistMessage;
import com.distsystem.interfaces.Cache;
import com.distsystem.utils.JsonUtils;
import com.distsystem.utils.DistMessageProcessor;
import com.distsystem.utils.DistWebApiProcessor;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/** manager to connect all storages, policies, agents
 * to perform clean based on time
 * replace all cache with fresh objects
 * */
public class CacheManager extends CacheBase {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(CacheManager.class);
    /** last time of clean for storages */
    private long lastCleanTime = System.currentTimeMillis();
    /** all storages to store cache objects - there could be internal storages,
     * Elasticsearch, Redis, local disk, JDBC database with indexed table, and many others */
    private final Map<String, CacheStorageBase> storages = new HashMap<>();
    /** processor that is connecting message method with current class method to be executed */
    private final DistMessageProcessor messageProcessor = new DistMessageProcessor()
            .addMethod(ServiceMethods.agentCacheClear.getMethodName(), this::messageClearCache)
            .addMethod(ServiceMethods.agentGetStorages.getMethodName(), this::messageGetStorages)
            .addMethod(ServiceMethods.agentGetCacheInfo.getMethodName(), this::messageGetCacheInfo)
            .addMethod(ServiceMethods.agentGetStats.getMethodName(), this::messageGetCacheStats)
            .addMethod(ServiceMethods.agentGetConfig.getMethodName(), this::messageGetConfig)
            .addMethod(ServiceMethods.agentSetObject.getMethodName(), this::messageSetObject)
            .addMethod(ServiceMethods.agentGetObject.getMethodName(), this::messageGetObject)
            .addMethod(ServiceMethods.cacheClearContains.getMethodName(), this::messageClearContains)
            .addMethod(ServiceMethods.cacheRemoveObjectByKey.getMethodName(), this::messageClearContains);

     /** initialize current manager with properties
     * this is creating storages, connecting to storages
     * creating cache policy, create agent and connecting to other cache agents */
    public CacheManager(Agent agent, CachePolicy policy) {
        super(agent, policy);
        log.info("Initializing cache in agent: " + agent.getAgentGuid() + ", cache: " + guid);
        initializeStorages();
        initializeTimer();
        addEvent(new AgentEvent(this, "CacheManager", AgentEvent.EVENT_CACHE_START));
    }

    /** additional web API endpoints */
    protected DistWebApiProcessor additionalWebApiProcessor() {
        return new DistWebApiProcessor(getServiceType())
                .addHandlerGet("key-encoder", (m, req) -> req.responseOkText( getKeyEncoder().getClass().getName()))
                .addHandlerGet("storages-info", (m, req) -> req.responseOkJson(JsonUtils.serialize(getStoragesInfo())))
                .addHandlerGet("storage-keys", (m, req) -> req.responseOkJson(JsonUtils.serialize(getStorageKeys())))
                .addHandlerGet("storages-count", (m, req) ->  req.responseOkText( ""+getStoragesCount()))
                .addHandlerPost("initialize-single-storage", (m, req) -> req.responseOkText( "" + initializeSingleStorage(req.getParamOne())))
                .addHandlerGet("items-count", (m, req) -> req.responseOkText(""+getItemsCount()))
                .addHandlerGet("objects-count", (m, req) -> req.responseOkText(""+getObjectsCount()))
                .addHandlerGet("items-per-storage", (m, req) -> req.responseOkJson(JsonUtils.serialize(getItemsCountPerStorage())))
                .addHandlerDelete("objects", (m, req) -> req.responseOkText( "" + clearCacheContains(req.getParamOne()) ))
                .addHandlerDelete("object", (m, req) -> req.responseOkText( "" + removeObjectByKey(req.getParamOne()) ))
                .addHandlerDelete("objects-clear", (m, req) -> req.responseOkText( "" + clearCaches(CacheClearMode.parseClearMode(req.getParamOne())) ))
                .addHandlerPost("object", (m, req) -> setCacheObjectFromRequest(req))
                .addHandlerPut("object", (m, req) -> putCacheObjectFromRequest(req))
                .addHandlerPut("objects", (m, req) -> putCacheObjectsFromRequest(req))
                .addHandlerGet("object", (m, req) -> getObjectFromRequest(req))
                .addHandlerGet("objects-full", (m, req) -> getCacheValuesFromRequest(req))
                .addHandlerGet("object-keys", (m, req) -> req.responseOkJson(JsonUtils.serialize(getCacheKeys(req.getParamOne(), true))))
                .addHandlerGet("object-infos", (m, req) -> req.responseOkJson(JsonUtils.serialize(getCacheInfos(req.getParamOne(), true))))
                .addHandlerGet("guid", (m, req) -> req.responseOkText(getCacheGuid()));
    }
    /** get information about all storages in this cache */
    public List<StorageInfo> getStoragesInfo() {
        return storages.values().stream()
                .map(CacheStorageBase::getStorageInfo)
                .collect(Collectors.toList());
    }

    /** get number of storages */
    public int getStoragesCount() {
        return storages.size();
    }

    /** process message, returns status */
    public DistMessage processMessage(DistMessage msg) {
        log.info("Process message by CacheManager, message: " + msg);
        cacheStats.processMessage();
        return messageProcessor.process(msg.getMethod(), msg);
    }

    /** initialize all storages from configuration */
    private void initializeStorages() {
        addEvent(new AgentEvent(this, "initializeStorages", AgentEvent.EVENT_INITIALIZE_STORAGES));
        String cacheStorageList = ""+getConfig().getProperty(DistConfig.AGENT_CACHE_STORAGES);
        log.info("Initializing cache storages: " + cacheStorageList);
        Arrays.stream(cacheStorageList.split(","))
                .distinct()
                .filter(x -> !x.isEmpty())
                .forEach(storageClass -> initializeSingleStorage(storageClass));
    }
    /** initialize single storage */
    private boolean initializeSingleStorage(String className) {
        try {
            cacheStats.initializeSingleStorage();
            String fullClassName = "com.distsystem.storage." + className;
            addEvent(new AgentEvent(this, "initializeStorages", AgentEvent.EVENT_INITIALIZE_STORAGE, fullClassName));
            log.debug("Initializing storage for class: " + fullClassName + ", current storages: " + storages.size());
            CacheStorageBase storage = (CacheStorageBase)Class.forName(fullClassName)
                    .getConstructor(Cache.class)
                    .newInstance(this);
            CacheStorageBase prevStorage = storages.put(storage.getStorageUid(), storage);
            log.info("Initialized storage: " + storage.getStorageUid() + ", current storages: " + storages.size());
            if (prevStorage != null) {
                log.debug("Got previous storage to dispose: " + prevStorage.getStorageUid());
                addEvent(new AgentEvent(this, "initializeStorages", AgentEvent.EVENT_DISPOSE_STORAGE, fullClassName));
                prevStorage.disposeStorage();
            }
            return true;
        } catch (Exception ex) {
            addIssue("initializeSingleStorage", ex);
            log.warn("Cannot initialize storage for class: " + className + ", reason: " + ex.getMessage(), ex);
            return false;
        }
    }

    /** initialize timers for Cache */
    protected void initializeTimer() {
        addEvent(new AgentEvent(this, "initializeTimer", AgentEvent.EVENT_INITIALIZE_TIMERS));
        // initialization for clean
        log.info("Scheduling clean timer task for cache: " + getCacheGuid());
        getAgent().getAgentTimers().setUpTimer("TIMER_CLEAN_STORAGE", DistConfig.AGENT_CACHE_TIMER_CLEAN_STORAGE_PERIOD, DistConfig.AGENT_CACHE_TIMER_CLEAN_STORAGE_PERIOD_DELAY_VALUE, x -> onTimeClean());
        addEvent(new AgentEvent(this, "initializeTimer", AgentEvent.EVENT_INITIALIZE_TIMER_CLEAN));
        log.info("Scheduling statistics refresh timer task for cache: " + getCacheGuid());
        getAgent().getAgentTimers().setUpTimer("TIMER_STAT_REFRESH", DistConfig.AGENT_CACHE_TIMER_STAT_REFRESH_PERIOD, DistConfig.AGENT_CACHE_TIMER_STAT_REFRESH_PERIOD_DELAY_VALUE, x -> onTimeStatsRefresh());
        addEvent(new AgentEvent(this, "initializeTimer", AgentEvent.EVENT_INITIALIZE_TIMER_RATIO));
    }

    /** executing close of this cache */
    protected void onClose() {
        addEvent(new AgentEvent(this, "onClose", AgentEvent.EVENT_CLOSE_BEGIN));
        log.info("Stopping all timer tasks");
        log.info("Removing caches and dispose storages");
        synchronized (storages) {
            for (CacheStorageBase value : storages.values()) {

                value.clearCacheForGroup("");
                value.disposeStorage();
            }
            storages.clear();
        }
        parentAgent.close();
        addEvent(new AgentEvent(this, "onClose", AgentEvent.EVENT_CLOSE_END));
    }

    /** set object in all or one internal caches */
    private List<CacheObject> writeObjectToStorages(CacheObject co) {
        cacheStats.writeObjectToStorages(co);
        var supportedStorages = co.getSupportedStorages();
        return storages.values().stream()
                .filter(st -> supportedStorages.contains(st.getStorageType()))
                .flatMap(storage -> storage.setObject(co).stream())
                .collect(Collectors.toList());
    }

    /** acquire object from external method, this could be slow because if could be a database query of external service
     * we would like to put cache around */
    private <T> T acquireObject(String key, Function<String, T> acquireMethod, CacheMode mode, Set<String> groups) {
        // Measure time of getting this object from cache
        long startActTime = System.currentTimeMillis();
        T objFromMethod = acquireMethod.apply(key);
        long acquireTimeMs = System.currentTimeMillis()-startActTime; // this is time of getting this object from method
        log.trace("===> Got object from external method/supplier, time: " + acquireTimeMs);
        CacheObject co = new CacheObject(key, objFromMethod, acquireTimeMs, acquireMethod, mode, groups);
        policy.checkAndApply(co, cacheStats);
        writeObjectToStorages(co);
        cacheStats.acquireObject(key, acquireTimeMs);
        //Optional<CacheObject> prev = setItem(co);
        //prev.ifPresent(CacheObject::releaseObject);
        return objFromMethod;
    }

    /** set object to cache */
    public AgentWebApiResponse setCacheObjectFromRequest(AgentWebApiRequest req) {
        List<String> groupsFromHeaders = req.getHeaders().getOrDefault("groups", List.of());
        HashSet<String> groups = new HashSet<>(groupsFromHeaders);
        String cacheMode = String.join("", req.getHeaders().getOrDefault("mode", List.of()));
        CacheMode objMode = CacheMode.fromString(cacheMode);
        String value = new String(req.getContent());
        log.debug("Set Cache object for key: " + req.getParamOne() + ", VALUE=" + value);
        var setBack = setCacheObject(req.getParamOne(), value, objMode, groups);
        // String key, Object value, CacheMode mode, Set<String> groups => CacheSetBack
        return req.responseOkJson(JsonUtils.serialize(setBack.toInfo()));
    }
    public AgentWebApiResponse putCacheObjectFromRequest(AgentWebApiRequest req) {
        String body = new String(req.getContent());
        CacheObjectRequest obj = JsonUtils.deserialize(body, CacheObjectRequest.class);
        if (obj == null) {
            return req.responseNotFound();
        } else {
            Function<String, Object> acquireMethod = s -> obj.getValue();
            //CacheObject co = new CacheObject(obj.getKey(), obj.getValue(), 0L, acquireMethod, CacheMode.fromString(obj.getMode()), obj.getGroups());
            var setBack = setCacheObject(obj.getKey(), obj.getValue(), CacheMode.fromString(obj.getMode()), obj.getGroups());
            return req.responseOkJson(JsonUtils.serialize(setBack.toInfo()));
        }
    }
    public AgentWebApiResponse putCacheObjectsFromRequest(AgentWebApiRequest req) {
        String body = new String(req.getContent());
        List<CacheObjectRequest> objs = JsonUtils.deserialize(body, new TypeReference<List<CacheObjectRequest>>() {});
        if (objs == null || objs.size() == 0) {
            return req.responseNotFound();
        } else {
            var setBacks = objs.stream().map(obj -> setCacheObject(obj.getKey(), obj.getValue(), CacheMode.fromString(obj.getMode()), obj.getGroups()).toInfo()).collect(Collectors.toList());
            return req.responseOkJson(JsonUtils.serialize(setBacks));
        }
    }
    /** get values stored in cache based on Web Request */
    public AgentWebApiResponse getCacheValuesFromRequest(AgentWebApiRequest req) {
        // String containsStr, boolean includeExternal =>? List<CacheObject>
        List<CacheObject> values = getCacheValues(req.getParamOne());
        var objs = values.stream().map(v -> v.serializedFullCacheObject(getAgent().getSerializer())).collect(Collectors.toList());
        return req.responseOkJson( JsonUtils.serialize(objs));
    }
    public AgentWebApiResponse getObjectFromRequest(AgentWebApiRequest req) {
        // String containsStr, boolean includeExternal =>? List<CacheObject>
        Optional<Object> obj = getObject(req.getParamOne());
        if (obj.isEmpty()) {
            return req.responseNotFound();
        } else {
            return req.responseText(200, ""+obj.get());
        }
    }
    /** set object to cache */
    public CacheSetBack setCacheObject(String key, Object value, CacheMode mode, Set<String> groups) {
        cacheStats.setCacheObject(key);
        log.info("Set cache, key=" + key + ", mode=" + mode.getMode());
        Function<String, Object> acquireMethod = s -> value;
        CacheObject co = new CacheObject(key, value, 0L, acquireMethod, mode, groups);
        List<CacheObject> prevObjects = writeObjectToStorages(co);
        return new CacheSetBack(prevObjects, co);
    }
    /** set object to cache */
    public CacheSetBack setCacheObject(CacheObject co) {
        cacheStats.setCacheObject(co.getKey());
        log.info("Set cache, key=" + co.getKey());
        List<CacheObject> prevObjects = writeObjectToStorages(co);
        return new CacheSetBack(prevObjects, co);
    }

    /** if cache contains given key */
    public boolean contains(String key) {
        return storages.values().stream().anyMatch(x -> x.getObject(key).isPresent());
    }

    /** clear caches with given clear cache */
    public int clearCaches(CacheClearMode clearMode) {
        addEvent(new AgentEvent(this, "clearCaches", AgentEvent.EVENT_CACHE_CLEAN));
        storages.values().stream().forEach(x -> x.clearCache(clearMode));
        return 1;
    }
    public Set<String> getStorageKeys() {
        return storages.keySet();
    }
    /** get all cache keys that contains given string */
    public Set<String> getCacheKeys(String containsStr, boolean includeExternal) {
        return storages.values()
                .stream()
                .filter(x -> x.isInternal() || includeExternal)
                .flatMap(x -> x.getKeys(containsStr).stream())
                .collect(Collectors.toSet());
    }
    /** get all cache keys that contains given string;  this is getting keys ONLY in internal */
    public Set<String> getCacheKeys(String containsStr) {
        return getCacheKeys(containsStr, false);
    }
    /** get values stored in cache
     * this might return only first X values */
    public List<CacheObject> getCacheValues(String containsStr, boolean includeExternal) {
        return storages.values()
                .stream()
                .filter(x -> x.isInternal() || includeExternal)
                .flatMap(x -> x.getValues(containsStr).stream())
                .collect(Collectors.toList());
    }
    public List<CacheObject> getCacheValues(String containsStr) {
        return getCacheValues(containsStr, false);
    }

    /** get list of cache infos for given key */
    public List<CacheObjectInfo> getCacheInfos(String containsStr, boolean includeExternal) {
        return storages.values()
                .stream()
                .filter(x -> x.isInternal() || includeExternal)
                .flatMap(x -> x.getInfos(containsStr).stream())
                .collect(Collectors.toList());
    }
    public List<CacheObjectInfo> getCacheInfos(String containsStr) {
        return getCacheInfos(containsStr, false);
    }


    /** get number of objects in all storages
     * if one object is inserted into cache - this is still one object even if this is a list of 1000 elements */
    public int getObjectsCount() {
        return storages.values().stream().map(x -> x.getObjectsCount()).reduce((x, y) -> x+y).orElse(0);
    }

    /** get number of items in cache, if a list with 100 elements is inserted into cache
     * then this is 1 object but 100 items  */
    public int getItemsCount() {
        return storages.values().stream().map(x -> x.getItemsCount()).reduce((x, y) -> x+y).orElse(0);
    }
    public Map<String, Integer> getItemsCountPerStorage() {
        Map<String, Integer> cnts = new HashMap<>();
        storages.values().stream().forEach(x -> cnts.put(x.getStorageUid(), x.getItemsCount()));
        return cnts;
    }
    /** get number of objects in each storage */
    public Map<String, Integer> getObjectsCountPerStorage() {
        Map<String, Integer> cnts = new HashMap<>();
        storages.values().stream().forEach(x -> cnts.put(x.getStorageUid(), x.getObjectsCount()));
        return cnts;
    }
    /** clear cache contains given partial key */
    public int clearCacheContains(String str) {
        parentAgent.sendMessageBroadcast(DistServiceType.cache, DistServiceType.cache, ServiceMethods.cacheClearContains.getMethodName(), str, DistCallbacks.createEmpty());
        return clearCacheContainsInternal(str);
    }
    /** internal method to clear cache by partial key */
    private int clearCacheContainsInternal(String str) {
        storages.values().stream().forEach(x -> x.clearCacheContains(str));
        return 1;
    }
    /** remove object from cache by given key */
    public int removeObjectByKey(String key) {
        parentAgent.sendMessageBroadcast(DistServiceType.cache, DistServiceType.cache, ServiceMethods.cacheRemoveObjectByKey.getMethodName(), key, DistCallbacks.createEmpty());
        storages.values().stream().forEach(x -> x.removeObjectByKey(key));
        return 1;
    }
    /** check cache every X seconds to clear TTL caches */
    public boolean onTimeClean() {
        long checkSeq = cacheStats.check();
        addEvent(new AgentEvent(this, "onTimeClean", AgentEvent.EVENT_TIMER_CLEAN));
        storages.values().stream().forEach(x ->  x.timeToClean(checkSeq, lastCleanTime));
        lastCleanTime = System.currentTimeMillis();
        return true;
    }
    /** */
    public boolean onTimeStatsRefresh() {
        addEvent(new AgentEvent(this, "onTimeStatsRefresh", AgentEvent.EVENT_INITIALIZE_TIMER_RATIO));
        cacheStats.refresh();
        return true;
    }

    /** get item from cache if exists or None */
    public Optional<CacheObject> getCacheObject(String key) {
        cacheStats.getCacheObject(key);
        for (CacheStorageBase storage: storages.values()) {
            long storageReadTimeStart = System.currentTimeMillis();
            Optional<CacheObject> fromCache = storage.getObject(key);
            if (fromCache.isPresent()) {
                try {
                    CacheObject co = fromCache.get();
                    co.use();
                    // TODO: if this is not internal cache - need to increase usage and lastUseDate ???
                    cacheStats.getObjectHit(key, storage, System.currentTimeMillis()-storageReadTimeStart);
                    return Optional.ofNullable(co);
                } catch (Exception ex) {
                    cacheStats.getObjectErrorRead(key, storage);
                    // TODO: log problem with casting value from cache for given key into specific type
                    addIssue("getObject", ex);
                }
            }
        }
        cacheStats.getObjectMiss(key);
        return Optional.empty();
    }
    /** get item from cache if exists
     * returns None if object is not in cache or desired cast if not working */
    public <T> Optional<T> getObject(String key) {
        try {
            return getCacheObject(key).map(co -> (T)co.getValue());
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    /** execute with cache for key
     * if object in cache exists and it is valid, then this object would be returned
     * if not exists then method would be executed to get object, object would be put to cache and returned */
    public <T> T withCache(String key, Supplier<? extends T> m, CacheMode mode, Set<String> groups) {
        try {
            Optional<T> itemFromCache = getObject(key);
            return itemFromCache.orElseGet(() -> acquireObject(key, __ -> m.get(), mode, groups));
        } catch (Exception ex) {
            addIssue("withCache", ex);
            return null;
        }
    }
    public <T> T withCache(String key, Function<String, ? extends T> mapper, CacheMode mode, Set<String> groups) {
        return withCache(key, () -> mapper.apply(key), mode, groups);
    }
    /** method to get registration keys for this agent */
    private DistMessage messageClearCache(String methodName, DistMessage msg) {
        clearCacheContains(msg.getMessage().toString());
        return msg.response("", DistMessageStatus.ok);
    }
    /** method to get registration keys for this agent */
    private DistMessage messageGetStorages(String methodName, DistMessage msg) {
        return msg.response(new StorageInfos(getStoragesInfo()), DistMessageStatus.ok);
    }
    private DistMessage messageGetCacheInfo(String methodName, DistMessage msg) {
        return msg.response(getCacheInfo(), DistMessageStatus.ok);
    }
    private DistMessage messageGetCacheStats(String methodName, DistMessage msg) {
        return msg.response(cacheStats, DistMessageStatus.ok);
    }
    private DistMessage messageGetConfig(String methodName, DistMessage msg) {
        return msg.response(getConfig(), DistMessageStatus.ok);
    }
    private DistMessage messageSetObject(String methodName, DistMessage msg) {
        CacheObjectSerialized cos = (CacheObjectSerialized)msg.getMessage();
        return msg.response(setCacheObject(cos.getKey(), cos), DistMessageStatus.ok);
    }
    private DistMessage messageClearContains(String methodName, DistMessage msg) {
        String str = "" + msg.getMessage();
        log.info("Other Agent cleared cache: " + msg.getFromAgent() + ", current: " + getCacheGuid() + ", STR=" + str + ", delay: " + msg.messageCurrentAgeMs());
        clearCacheContainsInternal(str);
        return msg.response("", DistMessageStatus.ok);
    }
    private DistMessage messageGetObject(String methodName, DistMessage msg) {
        String key = msg.getMessage().toString();
        return msg.response(getCacheObject(key), DistMessageStatus.ok);
    }

    /** update configuration of this Service */
    public void updateConfig(DistConfig newCfg) {
        // TODO: update configuration of this service
    }
}
