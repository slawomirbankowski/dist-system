package com.distsystem.api;

import com.distsystem.utils.DistUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ElasticIndexInfo {
    private String health;
    private String status;
    private String index;
    private String uuid;
    @JsonProperty("docs.count")
    private String docsCount;
    @JsonProperty("docs.deleted")
    private String docsDeleted;

    public ElasticIndexInfo() {
        health = "UNKNOWN";
        status = "UNKNOWN";
        index = "NO_INDEX";
        uuid = "UNKNOWN";
        docsCount = "0";
        docsDeleted = "0";
    }
    public ElasticIndexInfo(String health, String status, String index, String uuid, String docsCount, String docsDeleted) {
        this.health = health;
        this.status = status;
        this.index = index;
        this.uuid = uuid;
        this.docsCount = docsCount;
        this.docsDeleted = docsDeleted;
    }

    public String getHealth() {
        return health;
    }

    public void setHealth(String health) {
        this.health = health;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getDocsCount() {
        return DistUtils.parseInt(docsCount, 0);
    }

    public void setDocsCount(String docsCount) {
        this.docsCount = docsCount;
    }
    public int getDocsDeleted() {
        return DistUtils.parseInt(docsDeleted, 0);
    }

    public void setDocsDeleted(String docsDeleted) {
        this.docsDeleted = docsDeleted;
    }

    public String toString() {
        return "index:" + index + ", health: " + health + ", uuid: " + uuid + ", status: " + status + ", docsCount: " + docsCount + ", docsDeleted: " + docsDeleted;
    }
}
