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
    private final String key;
    /** */
    private final List<DistConfigEntry> entries;
    private final String entriesSerialized;
    private final String entriesHash;

    public DistConfigBucket(DistConfigGroup parentGroup, String key, List<DistConfigEntry> entries) {
        this.parentGroup = parentGroup;
        this.key = key;
        this.entries = entries;
        this.entries.sort(Comparator.comparing(DistConfigEntry::getFullConfig));
        this.entriesSerialized = this.entries.stream().map(x -> x.toString()).collect(Collectors.joining());
        this.entriesHash = DistUtils.fingerprint(entriesSerialized);
    }

    public DistConfigGroup getParentGroup() {
        return parentGroup;
    }
    public String getKey() {
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
