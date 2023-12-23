package com.distsystem.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** single entry - configuration name with value, example:
 *      AGENT_CACHE_STORAGE_MONGODB_HOST_PRIMARY=server001.domain.com
 *
 * where
 *      AGENT - indicates that it is configuration value for agents
 *      CACHE - service name
 *      STORAGE - service group
 *      MONGODB - config type
 *      HOST - config setting
 *      PRIMARY - config instance
 *
 * */
public class DistConfigEntry {
    private static final Logger log = LoggerFactory.getLogger(DistConfigEntry.class);

    private final String groupName; // AGENT_CACHE_STORAGE
    private final String fullConfig; // full key like AGENT_CACHE_STORAGE_MONGODB_HOST_PRIMARY
    private final String serviceName; // CACHE, API, DAO,
    private final String serviceGroup; // OBJECT, SERVER
    private final String configType; // HTTP, JDBC, KAFKA, ELASTICSEARCH, REDIS, MONGODB, ...
    private final String configSetting; // URL, USER, PASS, BROKERS, HOST, PORT, ...
    private final String configInstance; // PRIMARY, SECONDARY, TERTIARY
    private final String configValue; //

    public DistConfigEntry(String groupName, String fullConfig, String serviceName, String serviceGroup, String configType, String configSetting, String configInstance, String configValue) {
        this.groupName = groupName;
        this.fullConfig = fullConfig;
        this.serviceName = serviceName;
        this.serviceGroup = serviceGroup;
        this.configType = configType;
        this.configSetting = configSetting;
        this.configInstance = configInstance;
        this.configValue = configValue;
    }

    public String getServiceName() {
        return serviceName;
    }
    public String getServiceGroup() {
        return serviceGroup;
    }
    public String getConfigType() {
        return configType;
    }
    public String getConfigInstance() {
        return configInstance;
    }
    public DistConfigBucketKey getConfigKey() {
        return new DistConfigBucketKey(serviceName, configType, configInstance);
    }

    public String getGroupName() {
        return groupName;
    }
    public String getFullConfig() {
        return fullConfig;
    }
    public String getConfigValue() {
        return configValue;
    }
    public String getConfigSetting() {
        return configSetting;
    }
    public String toString() {
        return fullConfig + "=" + configValue;
    }
}
