package com.distsystem.api.info;

public class AgentDaoSimpleInfo {
    private String key;
    private String daoType;
    private String url;

    public AgentDaoSimpleInfo(String key, String daoType, String url) {
        this.key = key;
        this.daoType = daoType;
        this.url = url;
    }

    public String getKey() {
        return key;
    }
    public String getDaoType() {
        return daoType;
    }
    public String getUrl() {
        return url;
    }
}
