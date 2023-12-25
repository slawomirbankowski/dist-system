package com.distsystem.api;

import com.distsystem.api.info.DistConfigGroupInfo;
import com.distsystem.interfaces.DistService;
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
 *  TYPE=HTTP
 *  NAME=URL
 *  KEY=PRIMARY
 * */
public class DistConfigGroup {
    private static final Logger log = LoggerFactory.getLogger(DistConfigGroup.class);

    /** parent configuration with all values */
    private final DistConfig parentConfig;
    /** name of group */
    private final String groupName;

    /** all buckets in this configuration group */
    private final Map<DistConfigBucketKey, DistConfigBucket> buckets = new HashMap<>();
    /** parent service that will be notified in any configuration value would be changed */
    private final DistService parentService;
    /** all added entries to this group */
    private final List<DistConfigEntry> entries = new LinkedList<>();
    /** creates new group of configuration values */
    public DistConfigGroup(DistConfig parentConfig, String groupName, DistService parentService) {
        this.parentConfig = parentConfig;
        this.groupName = groupName;
        this.parentService = parentService;
        calculateBuckets();
    }

    /** close current group */
    public void close() {
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
    public void calculateBuckets() {
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
            DistConfigBucket bucket = new DistConfigBucket(this, g.getKey(), g.getValue());
            log.debug("Configuration bucket group adding new bucket, config: " + parentConfig.getConfigGuid() + ", group: "  + groupName + ", bucketKey: " + g.getKey() + ", hash: " + bucket.getEntriesHash() + ", entries: " + g.getValue().size());
            newBuckets.put(g.getKey(), bucket);
        });
        newBuckets.values().stream().forEach(bucket -> {
            parentService.initializeConfigBucket(bucket);
        });
        log.debug("Recalculated buckets, config: " + parentConfig.getConfigGuid() + ", group: "  + groupName + ", previous count: " + buckets.size() + ", new count: " + newBuckets.size());
        // TODO: merge existing buckets with new ones
        // newBuckets with buckets, initialize all new buckets
        //parentService.initializeConfigBucket();
        buckets.putAll(newBuckets);
        log.debug("Calculated config group of configuration values for config: " + parentConfig.getConfigGuid() + ", group: "  + groupName + ", all entries: " + newEntries.size() + ", all buckets: " + buckets.size());
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
