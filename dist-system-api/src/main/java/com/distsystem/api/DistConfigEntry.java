package com.distsystem.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DistConfigEntry {
    private static final Logger log = LoggerFactory.getLogger(DistConfigEntry.class);

    private String groupName;
    private String fullConfig; // full key
    private String serviceName; // CACHE, API, DAO,
    private String serviceGroup; // OBJECT, SERVER
    private String configType; // HTTP, JDBC, KAFKA, ELASTICSEARCH, ...
    private String configSetting; // URL, USER, PASS, BROKERS, HOST, PORT, ...
    private String configInstance; // PRIMARY, SECONDARY, TETRIARY
    private String configValue; //

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
    public String getConfigKey() {
        return configType + "_" + configInstance;
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
