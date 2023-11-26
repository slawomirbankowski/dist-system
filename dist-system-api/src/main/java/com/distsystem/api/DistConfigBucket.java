package com.distsystem.api;

import com.distsystem.utils.DistUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** bucket of configuration values for given group and key */
public class DistConfigBucket {
    private static final Logger log = LoggerFactory.getLogger(DistConfigBucket.class);

    private final DistConfigGroup parentGroup;
    private final DistConfigBucketKey key;
    /** */
    private final List<DistConfigEntry> entries;
    private final String entriesSerialized;
    private final String entriesHash;

    /** creates new configuration bucket */
    public DistConfigBucket(DistConfigGroup parentGroup, DistConfigBucketKey key, List<DistConfigEntry> entries) {
        this.parentGroup = parentGroup;
        this.key = key;
        this.entries = entries;
        this.entries.sort(Comparator.comparing(DistConfigEntry::getFullConfig));
        this.entriesSerialized = this.entries.stream().map(x -> x.toString()).collect(Collectors.joining());
        this.entriesHash = DistUtils.fingerprint(entriesSerialized);
    }

    /** convert property name like URL into full property name using group, config type and name
     * EXAMPLE: input=URL, output=
     * */
    private String convertPropertyName(String name) {
        if (name.startsWith("AGENT_")) {
            return name;
        } else {
            return parentGroup.getGroupName() + "_" + key.getConfigType() + "_" + name;
        }
    }
    /** get property value for given name */
    public String getProperty(String name) {
        return parentGroup.getParentConfig().getProperty(convertPropertyName(name));
    }
    /** get property for this bucket */
    public String getProperty(String name, String defaultValue) {
        String fullConfig = convertPropertyName(name);
        log.info("Get config property for object, group: " + parentGroup.getGroupName() + ", name: " + name + ", fullConfig: " + fullConfig);
        return parentGroup.getParentConfig().getProperty(fullConfig, defaultValue);

    }
    /** get property for given name as Long value */
    public long getPropertyAsLong(String name, long defaultValue) {
        return DistUtils.parseLong(getProperty(name), defaultValue);
    }
    /** get property for given name as Int value */
    public int getPropertyAsInt(String name, int defaultValue) {
        return DistUtils.parseInt(getProperty(name), defaultValue);
    }
    /** get property for given name as Double value */
    public double getPropertyAsDouble(String name, double defaultValue) {
        return DistUtils.parseDouble(getProperty(name), defaultValue);
    }
    /** get property for given name as Boolean value */
    public boolean getPropertyAsBoolean(String name, boolean defaultValue) {
        return DistUtils.parseBoolean(getProperty(name), defaultValue);
    }

    public DistConfigGroup getParentGroup() {
        return parentGroup;
    }
    public DistConfigBucketKey getKey() {
        return key;
    }
    public List<DistConfigEntry> getEntries() {
        return entries;
    }
    public String getEntriesSerialized() {
        return entriesSerialized;
    }
    public String getEntriesHash() {
        return entriesHash;
    }
}
