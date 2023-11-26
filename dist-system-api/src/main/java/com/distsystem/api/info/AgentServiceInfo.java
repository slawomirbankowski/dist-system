package com.distsystem.api.info;


import com.distsystem.api.enums.DistClientType;

import java.util.List;

/** information class about Server manager in Agent. */
public class AgentServiceInfo {

    private List<String> serviceKeys;

    public AgentServiceInfo(List<String> serviceKeys) {
        this.serviceKeys = serviceKeys;
    }

    public List<String> getServiceKeys() {
        return serviceKeys;
    }
}
