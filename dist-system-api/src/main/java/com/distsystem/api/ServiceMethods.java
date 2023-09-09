package com.distsystem.api;

import com.distsystem.api.enums.DistServiceType;
import com.distsystem.api.info.CacheInfo;
import com.distsystem.api.info.StorageInfos;
import com.distsystem.utils.CacheStats;

/** available methods inside services - each method can be called with Distributed Messages
 *
 * */
public class ServiceMethods {
    public static final ServiceMethod agentCacheClear = new ServiceMethod(DistServiceType.agent, "clearCache", String.class, String.class);
    public static ServiceMethod agentGetStorages = new ServiceMethod(DistServiceType.agent, "getStorages", Object.class, StorageInfos.class);
    public static ServiceMethod agentGetCacheInfo = new ServiceMethod(DistServiceType.agent, "getCacheInfo", Object.class, CacheInfo.class);
    public static ServiceMethod agentGetStats = new ServiceMethod(DistServiceType.agent, "getStats", Object.class, CacheStats.class);
    public static ServiceMethod agentGetConfig = new ServiceMethod(DistServiceType.agent, "getConfig", Object.class, DistConfig.class);
    public static ServiceMethod agentSetObject = new ServiceMethod(DistServiceType.agent, "setObject", String.class, CacheObjectSerialized.class);
    public static ServiceMethod agentGetObject = new ServiceMethod(DistServiceType.agent, "getObject", String.class, CacheObject.class);

    public static ServiceMethod cacheClearContains = new ServiceMethod(DistServiceType.cache, "clearCacheContains", String.class, String.class);
    public static ServiceMethod cacheRemoveObjectByKey = new ServiceMethod(DistServiceType.cache, "removeObjectByKey", String.class, String.class);

}

