package com.distsystem.api.info;

public class AgentDaoSimpleInfo {
    private final String key;
    private final String daoType;
    private final String url;

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
