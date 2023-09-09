package com.distsystem.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ElasticIndexCreateInfo {
    private String acknowledged;
    private String shards_acknowledged;
    private String index;

    public ElasticIndexCreateInfo() {
    }
    public ElasticIndexCreateInfo(String acknowledged, String shards_acknowledged, String index) {
        this.acknowledged = acknowledged;
        this.shards_acknowledged = shards_acknowledged;
        this.index = index;
    }
    public String getAcknowledged() {
        return acknowledged;
    }
    public void setAcknowledged(String acknowledged) {
        this.acknowledged = acknowledged;
    }
    public String getShards_acknowledged() {
        return shards_acknowledged;
    }
    public void setShards_acknowledged(String shards_acknowledged) {
        this.shards_acknowledged = shards_acknowledged;
    }
    public String getIndex() {
        return index;
    }
    public void setIndex(String index) {
        this.index = index;
    }
}
