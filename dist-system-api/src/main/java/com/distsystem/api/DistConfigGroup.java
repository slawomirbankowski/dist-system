package com.distsystem.api;

import com.distsystem.api.info.DistConfigGroupInfo;
import com.distsystem.base.AgentableBase;
import com.distsystem.interfaces.DistService;
import com.distsystem.utils.AdvancedMap;
import com.distsystem.utils.DistUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/** group of configuration for given service or component
 * all configuration values for names that starts with something common like:
 * AGENT_SERVER, CACHE_STORAGE, CONFIG_READER, ...
 * all these groups have many configuration buckets with different types
 * GROUP_TYPE_NAME_KEY = AGENT_CONFIGREADER_OBJECT_HTTP_URL
 *  SERVICE=CONFIGREADER
 *  =OBJECT
 *  TYPE=HTTP - configType
 *  NAME=URL
 *  KEY=PRIMARY -
 * */
public class DistConfigGroup extends AgentableBase {
    private static final Logger log = LoggerFactory.getLogger(DistConfigGroup.class);

    /** parent configuration with all values */
    private final DistConfig parentConfig;
    /** name of group: SEMAPHORE_JDBC_PRIMARY, */
    private final String groupName;
    /** all buckets in this configuration group */
    private final Map<DistConfigBucketKey, DistConfigBucket> buckets = new HashMap<>();
    /** parent service that will be notified in any configuration value would be changed */
    private final DistService parentService;
    /** all added entries to this group */
    private final List<DistConfigEntry> entries = new LinkedList<>();
    private final List<AdvancedMap> results = new LinkedList<>();
    /** creates new group of configuration values
     * groupName - name of the group: DistConfig.AGENT_AUTH_STORAGE, AGENT_CONFIGREADER_OBJECT, AGENT_REGISTRATION_OBJECT, ... */
    public DistConfigGroup(DistConfig parentConfig, String groupName, DistService parentService) {
        super(parentService.getAgent());
        this.parentConfig = parentConfig;
        this.groupName = groupName;
        this.parentService = parentService;
        List<AdvancedMap> initialResults = calculateBuckets();
        results.addAll(initialResults);
    }

    @Override
    protected long countObjectsAgentable() {
        return 1L;
    }
    /** create unique agentable UID */
    protected String createGuid() {
        return DistUtils.generateCustomGuid("CFGGRP_" + parentAgent.getAgentShortGuid());
    }
    @Override
    protected void onClose() {
        parentConfig.unregisterConfigGroup(groupName);
    }
    /** get info about this group */
    public DistConfigGroupInfo getInfo() {
        log.info("Get info for ConfigGroup: " + groupName);
        // DistConfigGroupInfo(String groupName, List<String> bucketKeys, List<String> entries)
        return new DistConfigGroupInfo(groupName,
                buckets.keySet().stream().map(DistConfigBucketKey::toString).collect(Collectors.toList()),
                entries.stream().map(DistConfigEntry::getFullConfig).collect(Collectors.toList()));
    }
    /** calculate buckets from all entries - each bucket has many configuration values for the same instance and type, example:
     * AGENT_SEMAPHORE_OBJECT_JDBC_URL, AGENT_SEMAPHORE_OBJECT_JDBC_DRIVER, AGENT_SEMAPHORE_OBJECT_JDBC_USER, AGENT_SEMAPHORE_OBJECT_JDBC_PASS
     * */
    public List<AdvancedMap> calculateBuckets() {
        log.debug("Start calculating buckets for group, service: " + parentService.getServiceType().name() + ", config: " + parentConfig.getConfigGuid() + ", group: "  + groupName);
        List<DistConfigEntry> newEntries = parentConfig.getPropertiesStartsWith(groupName).entrySet().stream().flatMap(p -> {
            String fullName = p.getKey();
            String[] configKeyParts = fullName.split("_");
            Optional<DistConfigEntry> entry;
            if (configKeyParts.length>=4) {
                // AGENT_CONFIGREADER_OBJECT_HTTP_URL
                String serviceName = configKeyParts[1]; // CONFIGREADER, CACHE, ...
                String serviceGroup = configKeyParts[2]; // OBJECT, SERVER, ...
                String configType = configKeyParts[3]; // HTTP, JDBC, KAFKA, ELASTICSEARCH, ...
                String configSetting = (configKeyParts.length>4)?configKeyParts[4]:DistConfig.DEFAULT; // URL, USER, PASS, BROKERS, HOST, PORT, DEFAULT ...
                String configInstance = (configKeyParts.length>5)?configKeyParts[5]:DistConfig.PRIMARY; // PRIMARY, SECONDARY, TETRIARY
                DistConfigEntry e = new DistConfigEntry(groupName, fullName, serviceName, serviceGroup, configType, configSetting, configInstance, p.getValue());
                log.trace("Configuration bucket, got entry for service: " + serviceName + ", serviceGroup: " + serviceGroup + ", configType: " + configType + ", configSetting: " + configSetting + ", configInstance: " + configInstance);
                entry = Optional.of(e);
            } else {
                entry = Optional.empty();
            }
            return entry.stream();
        }).toList();
        Map<DistConfigBucketKey, DistConfigBucket> newBuckets = new HashMap<>();
        entries.addAll(newEntries);
        newEntries.stream().collect(Collectors.groupingBy(x -> x.getConfigKey())).entrySet().stream().forEach(g -> {
            DistConfigBucket bucket = DistConfigBucket.createBucket(this, g.getKey(), g.getValue());
            log.debug("Configuration bucket group adding new bucket, config: " + parentConfig.getConfigGuid() + ", group: "  + groupName + ", bucketKey: " + g.getKey() + ", hash: " + bucket.getEntriesHash() + ", entries: " + g.getValue().size());
            newBuckets.put(g.getKey(), bucket);
        });
        List<AdvancedMap> results = newBuckets.values().stream().map(bucket -> parentService.initializeConfigBucket(bucket)).toList();
        log.debug("Recalculated buckets, config: " + parentConfig.getConfigGuid() + ", group: "  + groupName + ", previous count: " + buckets.size() + ", new count: " + newBuckets.size());
        // TODO: merge existing buckets with new ones
        // newBuckets with buckets, initialize all new buckets
        //parentService.initializeConfigBucket();
        buckets.putAll(newBuckets);
        log.debug("Calculated config group of configuration values for config: " + parentConfig.getConfigGuid() + ", group: "  + groupName + ", all entries: " + newEntries.size() + ", all buckets: " + buckets.size());
        return results;
    }
    /** adding new configuration bucket from values
     * serviceGroup - OBJECT, SERVER
     * configType - JDBC, ELASTICSEARCH, MONGODB, SOCKET, ...
     * configInstance - PRIMARY, SECONDARY, TETRIARY, ...
     * configValues - key/value map like URL=http://  USER=someuser   PASS=supersecretpassword123
     * AGENT_REGISTRATION_OBJECT_JDBC_URL_PRIMARY
     * AGENGT_serviceName_serviceGroup_configType_key_configInstance
     * */
    public AdvancedMap addConfigValues(String serviceGroup, String configType, String configInstance, Map<String, String> configValues) {
        String serviceName = parentService.getServiceType().name().toUpperCase();
        DistConfigBucketKey key = new DistConfigBucketKey(serviceName, configType, configInstance);
        log.info("Add new configuration values to group: " + groupName +", serviceGroup: " + serviceGroup + ", serviceName: " + serviceName +", configType: " + configType + ", instance: " + configInstance + ", values: " + configValues.size() + " " + configValues.keySet());
        DistConfig newCfg = DistConfig.buildEmptyConfig();
        List<DistConfigEntry> entries = configValues.entrySet().stream().map(cv -> {
            String configFullKey = "AGENT_" + serviceName + "_" + serviceGroup + "_" + configType + "_" + cv.getKey() + "_" + configInstance;
            String configFullValue = cv.getValue();
            log.debug("----> Add new configuration value, fullKey: " + configFullKey);
            DistConfigEntry entry = new DistConfigEntry(groupName, configFullKey, serviceName, serviceGroup, configType, cv.getKey(), configInstance, configFullValue);
            return entry;
        }).collect(Collectors.toList());
        DistConfigBucket configBucket = DistConfigBucket.createBucket(this, key, entries);
        buckets.put(key, configBucket);
        AdvancedMap result = configBucket.initializeConfigBucket();
        results.add(result);
        return result;
    }

    /** get parent config for this group */
    public DistConfig getParentConfig() {
        return parentConfig;
    }
    /** */
    public String getGroupName() {
        return groupName;
    }
    /** */
    public Map<DistConfigBucketKey, DistConfigBucket> getBuckets() {
        return buckets;
    }
    /** get parent service of this group */
    public DistService getParentService() {
        return parentService;
    }
}
