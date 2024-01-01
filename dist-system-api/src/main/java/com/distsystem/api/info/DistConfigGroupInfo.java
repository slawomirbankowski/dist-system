package com.distsystem.api.info;

import java.io.Serializable;
import java.util.List;

public class DistConfigGroupInfo implements Serializable {

    private final String groupName;
    private final List<String> bucketKeys;
    private final List<String> entries;

    public DistConfigGroupInfo(String groupName, List<String> bucketKeys, List<String> entries) {
        this.groupName = groupName;
        this.bucketKeys = bucketKeys;
        this.entries = entries;
    }

    public String getGroupName() {
        return groupName;
    }
    public List<String> getBucketKeys() {
        return bucketKeys;
    }
    public List<String> getEntries() {
        return entries;
    }

    /** */
    public static DistConfigGroupInfo emptyInfo() {
        return new DistConfigGroupInfo("", List.of(), List.of());
    }
}
