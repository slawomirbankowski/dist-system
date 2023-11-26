package com.distsystem.api;

import com.distsystem.utils.JsonUtils;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;


/** POJO class for Web API request */
public class AgentWebApiRequest {
    private long reqSeq;
    private long startTime;
    private String protocol;
    private String method;

    private String requestKey;
    private Map<String, List<String>> headers;
    private byte[] content;
    private String[] pathTab;
    private String serviceName;
    private String serviceMethod;
    private String paramOne;
    private String paramTwo;
    private String path;
    private Map<String, Object> attributes;

    /** creates new request from HTTP request with URI */
    public AgentWebApiRequest(long reqSeq, long startTime, String protocol, String method, URI uri, Map<String, List<String>> headers, byte[] content, String path, Map<String, Object> attributes) {
        this.reqSeq = reqSeq;
        this.startTime = startTime;
        this.protocol = protocol;
        this.method = method;
        this.headers = headers;
        this.content = content;
        pathTab = uri.getPath().split("/");
        this.serviceName = (pathTab.length>1)?pathTab[1]:"";
        this.serviceMethod = (pathTab.length>2)?pathTab[2]:"";
        this.requestKey =  method + ":/" + serviceName + "/" + serviceMethod;
        this.paramOne = (pathTab.length>3)?pathTab[3]:"";
        this.paramTwo = (pathTab.length>4)?pathTab[4]:"";
        this.path = path;
        this.attributes = attributes;
    }
    /** creates new message from service */
    public AgentWebApiRequest(long reqSeq, long startTime, String protocol, String method, String serviceName, String serviceMethod, String paramOne, String paramTwo, Map<String, List<String>> headers, byte[] content) {
        this.reqSeq = reqSeq;
        this.startTime = startTime;
        this.protocol = protocol;
        this.method = method;
        this.headers = headers;
        this.content = content;
        this.pathTab = new String[] { paramOne, paramTwo };
        this.serviceName = serviceName;
        this.serviceMethod = serviceMethod;
        this.requestKey =  method + ":/" + serviceName + "/" + serviceMethod;
        this.paramOne = paramOne;
        this.paramTwo = paramTwo;
        this.path="";
        this.attributes=Map.of();
    }
    /** creates new local request */
    public AgentWebApiRequest(String requestKeyWithParams, String serviceName) {
        this.reqSeq = AgentWebApiRequest.localRequestSeq.incrementAndGet();
        this.startTime = System.currentTimeMillis();
        this.protocol = "local";
        this.method = "";
        this.headers = Map.of();
        this.content = new byte[0];
        String[] requestTab = requestKeyWithParams.split("\\|");
        this.serviceMethod = (requestTab.length)>0?requestTab[0]:"";
        this.requestKey =  (requestTab.length)>0?requestTab[0]:"";
        this.paramOne = (requestTab.length)>1?requestTab[1]:"";
        this.paramTwo = (requestTab.length)>2?requestTab[2]:"";
        this.pathTab = new String[] { paramOne, paramTwo };
        this.serviceName = serviceName;
        this.path = requestKeyWithParams;
        this.attributes=Map.of();
    }
    /** creates new request from message */
    public AgentWebApiRequest(DistMessage msg) {
        this.reqSeq = AgentWebApiRequest.localRequestSeq.incrementAndGet();
        this.startTime = System.currentTimeMillis();
        this.protocol = "message";
        this.method = msg.getRequestMethod();
        this.headers = Map.of();
        this.content = new byte[0];
        String fullMethodName = "GET:/" + msg.getToService().name() + "/" + msg.getRequestMethod();
        this.serviceMethod = msg.getRequestMethod();
        this.requestKey =  "GET:/" + msg.getToService().name() + "/" + msg.getRequestMethod();
        this.paramOne = "";
        this.paramTwo = "";
        this.pathTab = new String[] { paramOne, paramTwo };
        this.serviceName = serviceName;
        this.path = fullMethodName;
        this.attributes=Map.of();
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
    public Map<String, List<String>> getHeaders() {
        return headers;
    }
    public byte[] getContent() {
        return content;
    }
    public String getContentAsString() {
        return new String(content);
    }
    public String getServiceName() {
        return serviceName;
    }
    public String getRequestKey() {
        return requestKey;
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

    public String[] getPathTab() {
        return pathTab;
    }

    public String getPath() {
        return path;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public AgentWebApiResponse responseNotFound() {
        return new AgentWebApiResponse(404, headerText, "Not found Web API request for service: " + getServiceName() + ", method: " + getServiceMethod());
    }
    public AgentWebApiResponse responseNotFoundMethod(String message) {
        return new AgentWebApiResponse(404, headerText, message);
    }
    public AgentWebApiResponse responseNotImplemented() {
        return new AgentWebApiResponse(501, headerText, "Not implemented Web API request for service: " + getServiceName() + ", method: " + getServiceMethod());
    }
    public AgentWebApiResponse responseText(int code, String c) {
        return new AgentWebApiResponse(code, headerText, c);
    }
    public AgentWebApiResponse responseOkText(String c) {
        return new AgentWebApiResponse(200, headerText, c);
    }

    public AgentWebApiResponse responseOkJsonSerialize(Object obj) {
        return new AgentWebApiResponse(200, headerJson, obj, JsonUtils.serialize(obj));
    }

    public static final Map<String, List<String>> headerJson = Map.of("Content-Type", List.of("application/json"));
    public static final Map<String, List<String>> headerText = Map.of("Content-Type", List.of("text/html"));

    /** */
    public static final AtomicLong localRequestSeq = new AtomicLong();
    /** */
    public static AgentWebApiRequest create(String serviceName, String method, String serviceMethod, Map<String, List<String>> headers) {
        return new AgentWebApiRequest(localRequestSeq.incrementAndGet(), System.currentTimeMillis(),
                "http", method,
                serviceName, serviceMethod,
                "", "",
                headers, new byte[0]);
    }
}
