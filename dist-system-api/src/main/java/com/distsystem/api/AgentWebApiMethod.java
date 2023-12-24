package com.distsystem.api;

import java.util.Set;
import java.util.function.BiFunction;

/** */
public class AgentWebApiMethod {

    /** full key for this Web API method - GET:/service/method */
    private String fullKey;
    /** HTTP method: GET, POST, PUT, DELETE, ... */
    private final String requestMethod;
    private final String serviceMethod;
    private final BiFunction<String, AgentWebApiRequest, AgentWebApiResponse> method;
    private final Set<String> roles;

    public AgentWebApiMethod(String fullKey, String requestMethod, String serviceMethod, BiFunction<String, AgentWebApiRequest, AgentWebApiResponse> method, Set<String> roles) {
        this.requestMethod = requestMethod;
        this.serviceMethod = serviceMethod;
        this.method = method;
        this.roles = roles;
    }
    public String getRequestMethod() {
        return requestMethod;
    }
    public String getServiceMethod() {
        return serviceMethod;
    }
    public BiFunction<String, AgentWebApiRequest, AgentWebApiResponse> getMethod() {
        return method;
    }
    public String getFullKey() {
        return fullKey;
    }
    public Set<String> getRoles() {
        return roles;
    }

}

