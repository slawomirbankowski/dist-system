package com.distsystem.api;

import java.util.List;

// TODO: remove this class
public class AgentWelcomeEndpointInfo {

    private String welcome = "";

    private List<String> endpoints;
    private List<String> services;
    private List<String> servers;

    public AgentWelcomeEndpointInfo(String welcome, List<String> endpoints, List<String> services, List<String> servers) {
        this.welcome = "Distributed cache library for JVM applications or through socket/http connectors.";
        this.endpoints = endpoints;
        this.services = services;
        this.servers = servers;
    }

    public String getWelcome() {
        return welcome;
    }

    public List<String> getEndpoints() {
        return endpoints;
    }

    public List<String> getServices() {
        return services;
    }

    public List<String> getServers() {
        return servers;
    }
}
