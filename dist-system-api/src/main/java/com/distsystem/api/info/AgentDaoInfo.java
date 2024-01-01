package com.distsystem.api.info;

import com.distsystem.api.enums.DistDaoType;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;

/** Information class about remote agent maintained by other system */
public class AgentDaoInfo {

    private final LocalDateTime createDate;
    private final long workingMinutes;
    private final String key;
    private final DistDaoType daoType;
    private final String url;
    private final boolean connected;
    private final Collection<String> structures;
    private final Set<String> componentGuids;

    public AgentDaoInfo(LocalDateTime createDate, long workingMinutes, String key, DistDaoType daoType, String url, boolean connected,
                        Collection<String> structures, Set<String> componentGuids) {
        this.createDate = createDate;
        this.workingMinutes = workingMinutes;
        this.key = key;
        this.daoType = daoType;
        this.url = url;
        this.connected = connected;
        this.structures = structures;
        this.componentGuids = componentGuids;
    }
    public String getCreateDate() {
        return createDate.toString();
    }
    public long getWorkingMinutes() {
        return workingMinutes;
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
    public Set<String> getComponentGuids() {
        return componentGuids;
    }
}
