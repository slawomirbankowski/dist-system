package com.distsystem.api.info;

import java.util.List;
import java.util.Set;

/** Information class about DAOs in Agent */
public class AgentDaosInfo {

    private final List<AgentDaoInfo> daos;
    private final Set<String> producers;

    public AgentDaosInfo(List<AgentDaoInfo> daos, Set<String> producers) {
        this.daos = daos;
        this.producers = producers;
    }
    public List<AgentDaoInfo> getDaos() {
        return daos;
    }
    public Set<String> getProducers() {
        return producers;
    }
}
