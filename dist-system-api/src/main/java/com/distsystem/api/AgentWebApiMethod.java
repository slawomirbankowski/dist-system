package com.distsystem.api;

import java.util.function.BiFunction;

public class AgentWebApiMethod {

    private String requestMethod;
    private String serviceMethod;
    private BiFunction<String, AgentWebApiRequest, AgentWebApiResponse> method;

    public AgentWebApiMethod(String requestMethod, String serviceMethod, BiFunction<String, AgentWebApiRequest, AgentWebApiResponse> method) {
        this.requestMethod = requestMethod;
        this.serviceMethod = serviceMethod;
        this.method = method;
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
}

