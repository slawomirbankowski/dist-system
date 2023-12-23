package com.distsystem.api;

import java.util.List;

public class AgentServiceHandlers {

    private String service;
    private List<String> handlers;
    public AgentServiceHandlers(String service, List<String> handlers) {
        this.service = service;
        this.handlers = handlers;
    }
    public String getService() {
        return service;
    }
    public List<String> getHandlers() {
        return handlers;
    }
}

