package com.distsystem.api;

import java.util.Set;
import java.util.stream.Collectors;

/** simple info about object in cache */
public class CacheObjectRequest {

    private String key;
    private String value;
    private String mode;
    private Set<String> groups;

    public CacheObjectRequest() {
    }

    public CacheObjectRequest(String key, String value, String mode, Set<String> groups) {
        this.key = key;
        this.value = value;
        this.mode = mode;
        this.groups = groups;
    }

    public String getKey() {
        return key;
    }
    public String getValue() {
        return value;
    }
    public String getMode() {
        return mode;
    }
    public Set<String> getGroups() {
        return groups;
    }

    public void setKey(String key) {
        this.key = key;
    }
    public void setValue(String value) {
        this.value = value;
    }
    public void setMode(String mode) {
        this.mode = mode;
    }
    public void setGroups(Set<String> groups) {
        this.groups = groups;
    }

    @Override
    public String toString() {
        return "key="+ key+", value="+value+", mode="+mode+", groups="+groups.stream().sorted().collect(Collectors.toList());
    }
}
