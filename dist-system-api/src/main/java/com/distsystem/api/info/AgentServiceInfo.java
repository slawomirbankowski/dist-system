package com.distsystem.api.info;


import com.distsystem.api.enums.DistClientType;

import java.util.List;

/** information class about Server manager in Agent. */
public class AgentServiceInfo {

    private List<String> serviceKeys;

    private List<AgentServiceSimpleInfo> serviceInfos;

    public AgentServiceInfo(List<String> serviceKeys, List<AgentServiceSimpleInfo> serviceInfos) {
        this.serviceKeys = serviceKeys;
        this.serviceInfos = serviceInfos;
    }
    public List<String> getServiceKeys() {
        return serviceKeys;
    }
    public List<AgentServiceSimpleInfo> getServiceInfos() {
        return serviceInfos;
    }
}
