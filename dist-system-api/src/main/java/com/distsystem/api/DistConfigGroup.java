package com.distsystem.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private DistConfig parentConfig;
    /** name of group */
    private String groupName;
    /** all buckets */
    private Map<String, DistConfigBucket> buckets = new HashMap<>();

    /** */
    public DistConfigGroup(DistConfig parentConfig, String groupName) {
        this.parentConfig = parentConfig;
        this.groupName = groupName;
        calculateBuckets();
    }
    /** calculate buckets from all entries */
    public void calculateBuckets() {
        log.info("Start calculating buckets for group, config: " + parentConfig.getConfigGuid() + ", group: "  + groupName);
        List<DistConfigEntry> entries = parentConfig.getPropertiesStartsWith(groupName).entrySet().stream().flatMap(p -> {
            String fullName = p.getKey();
            String[] configKeyParts = fullName.split("_");
            Optional<DistConfigEntry> entry;
            if (configKeyParts.length>=4) {
                // AGENT_CONFIGREADER_OBJECT_HTTP_URL
                String serviceName = configKeyParts[1]; // CONFIGREADER, CACHE,
                String serviceGroup = configKeyParts[2]; // OBJECT, SERVER
                String configType = configKeyParts[3]; // HTTP, JDBC, KAFKA, ELASTICSEARCH, ...
                String configSetting = (configKeyParts.length>4)?configKeyParts[4]:""; // URL, USER, PASS, BROKERS, HOST, PORT, ...
                String configInstance = (configKeyParts.length>5)?configKeyParts[5]:"MAIN"; //
                DistConfigEntry e = new DistConfigEntry(groupName, fullName, serviceName, serviceGroup, configType, configSetting, configInstance, p.getValue());
                log.info("--------------------> service: " + serviceName + ", serviceGroup: " + serviceGroup + ", configType: " + configType + ", configSetting: " + configSetting + ", configInstance: " + configInstance);
                entry = Optional.of(e);
            } else {
                entry = Optional.empty();
            }
            return entry.stream();
        }).toList();
        entries.stream().collect(Collectors.groupingBy(x -> x.getConfigKey())).entrySet().stream().forEach(g -> {
            DistConfigBucket bucket = new DistConfigBucket(this, g.getKey(), g.getValue());
            log.info("---> Configuration group adding new bucket, config: " + parentConfig.getConfigGuid() + ", group: "  + groupName + ", bucketKey: " + g.getKey() + ", hash: " + bucket.getEntriesHash() + ", entries: " + g.getValue().size());
            buckets.put(g.getKey(), bucket);
        });
        log.info("Calculated config group of configuration values for config: " + parentConfig.getConfigGuid() + ", group: "  + groupName + ", all entries: " + entries.size() + ", all buckets: " + buckets.size());
    }
    /** get parent config for this group */
    public DistConfig getParentConfig() {
        return parentConfig;
    }
    public String getGroupName() {
        return groupName;
    }
    public Map<String, DistConfigBucket> getBuckets() {
        return buckets;
    }

}
