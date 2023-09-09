package com.distsystem.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ElasticClusterInfo {
    private String id;
    private String host;
    private String ip;
    private String node;

    public ElasticClusterInfo() {
        id = "";
        host = "";
        ip = "";
        node = "";
    }
    public ElasticClusterInfo(String id, String host, String ip, String node) {
        this.id = id;
        this.host = host;
        this.ip = ip;
        this.node = node;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public String toString() {
        return "id:" + id + ", host: " + host + ", ip: " + ip + ", node: " + node;
    }
}
