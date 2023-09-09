package com.distsystem.api.info;

import com.distsystem.api.enums.DistDaoType;

import java.time.LocalDateTime;
import java.util.Collection;

/** Information class about remote agent maintained by other system */
public class AgentDaoInfo {

    private LocalDateTime createDate;
    private String key;
    private DistDaoType daoType;
    private String url;
    private boolean connected;
    private Collection<String> structures;

    public AgentDaoInfo(LocalDateTime createDate, String key, DistDaoType daoType, String url, boolean connected, Collection<String> structures) {
        this.createDate = createDate;
        this.key = key;
        this.daoType = daoType;
        this.url = url;
        this.connected = connected;
        this.structures = structures;
    }
    public String getCreateDate() {
        return createDate.toString();
    }
    public String getKey() {
        return key;
    }
    public DistDaoType getDaoType() {
        return daoType;
    }
    public String getUrl() {
        return url;
    }
    public boolean isConnected() {
        return connected;
    }
    public Collection<String> getStructures() {
        return structures;
    }
}
