package com.distsystem.api;

import java.net.URI;
import java.util.List;
import java.util.Map;


/** POJO class for Web API request */
public class AgentWebApiRequest {
    private long reqSeq;
    private long startTime;
    private String protocol;
    private String method;
    private URI uri;
    private Map<String, List<String>> headers;
    private byte[] content;
    private String serviceName;
    private String serviceMethod;
    private String paramOne;
    private String paramTwo;

    public AgentWebApiRequest(long reqSeq, long startTime, String protocol, String method, URI uri, Map<String, List<String>> headers, byte[] content) {
        this.reqSeq = reqSeq;
        this.startTime = startTime;
        this.protocol = protocol;
        this.method = method;
        this.uri = uri;
        this.headers = headers;
        this.content = content;
        String[] pathTab = uri.getPath().split("/");
        this.serviceName = (pathTab.length>1)?pathTab[1]:"";
        this.serviceMethod = (pathTab.length>2)?pathTab[2]:"";
        this.paramOne = (pathTab.length>3)?pathTab[3]:"";
        this.paramTwo = (pathTab.length>4)?pathTab[4]:"";
    }
    public long getReqSeq() {
        return reqSeq;
    }
    public long getStartTime() {
        return startTime;
    }
    public String getProtocol() {
        return protocol;
    }
    public String getMethod() {
        return method;
    }
    public URI getUri() {
        return uri;
    }
    public Map<String, List<String>> getHeaders() {
        return headers;
    }
    public byte[] getContent() {
        return content;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getServiceMethod() {
        return serviceMethod;
    }

    public String getParamOne() {
        return paramOne;
    }
    public String getParamTwo() {
        return paramTwo;
    }
    public AgentWebApiResponse responseNotFound() {
        return new AgentWebApiResponse(404, headerText, "Not found Web API request for service: " + getServiceName() + ", method: " + getServiceMethod());
    }
    public AgentWebApiResponse responseNotImplemented() {
        return new AgentWebApiResponse(501, headerText, "Not implemented Web API request for service: " + getServiceName() + ", method: " + getServiceMethod());
    }
    public AgentWebApiResponse responseText(int code, String content) {
        return new AgentWebApiResponse(code, headerText, content);
    }
    public AgentWebApiResponse responseOkText(String content) {
        return new AgentWebApiResponse(200, headerText, content);
    }

    public AgentWebApiResponse responseOkJson(String content) {
        return new AgentWebApiResponse(200, headerJson, content);
    }


    public static final Map<String, List<String>> headerJson = Map.of("Content-Type", List.of("application/json"));
    public static final Map<String, List<String>> headerText = Map.of("Content-Type", List.of("text/html"));


}
