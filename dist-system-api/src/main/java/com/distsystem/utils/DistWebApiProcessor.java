package com.distsystem.utils;

import com.distsystem.api.AgentWebApiRequest;
import com.distsystem.api.AgentWebApiResponse;
import com.distsystem.api.enums.DistServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.function.BiFunction;

/** Processor for Agent Web API to handle requests and produce responses
 * it is keeping methods
 * */
public class DistWebApiProcessor {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(DistWebApiProcessor.class);
    /** */
    private final DistServiceType servType;
    /** key -> method with name like GET:info
     * value - method to process this Web Api Request */
    private final HashMap<String, BiFunction<String, AgentWebApiRequest, AgentWebApiResponse>> requestMethodHandlers = new HashMap<>();

    public DistWebApiProcessor(DistServiceType servType) {
        this.servType = servType;
    }
    /** get current number of request handler registered */
    public long getRequestHandlersCount() {
        return requestMethodHandlers.size();
    }
    /** merge with another web api processor */
    public DistWebApiProcessor merge(DistWebApiProcessor processor) {
        int currentHandlers = requestMethodHandlers.size();
        requestMethodHandlers.putAll(processor.requestMethodHandlers);
        log.info("Merging processor with additional handlers, service: " + servType.name() + ", current handlers: " + currentHandlers +" with: " + processor.requestMethodHandlers.size() + ", final: " + requestMethodHandlers.size() + ", new keys: " + processor.requestMethodHandlers.keySet());
        return this;
    }
    /** add new handler */
    public DistWebApiProcessor addHandler(String requestMethod, String serviceMethod, BiFunction<String, AgentWebApiRequest, AgentWebApiResponse> method) {
        String fullKey = requestMethod + ":" + serviceMethod;
        requestMethodHandlers.put(fullKey, method);
        return this;
    }
    public DistWebApiProcessor addHandlerGet(String serviceMethod, BiFunction<String, AgentWebApiRequest, AgentWebApiResponse> method) {
        return addHandler("GET", serviceMethod, method);
    }
    public DistWebApiProcessor addHandlerPost(String serviceMethod, BiFunction<String, AgentWebApiRequest, AgentWebApiResponse> method) {
        return addHandler("POST", serviceMethod, method);
    }
    public DistWebApiProcessor addHandlerPut(String serviceMethod, BiFunction<String, AgentWebApiRequest, AgentWebApiResponse> method) {
        return addHandler("PUT", serviceMethod, method);
    }
    public DistWebApiProcessor addHandlerDelete(String serviceMethod, BiFunction<String, AgentWebApiRequest, AgentWebApiResponse> method) {
        return addHandler("DELETE", serviceMethod, method);
    }
    /** handle request - find method and execute it to get response for Web API */
    public AgentWebApiResponse handleRequest(AgentWebApiRequest req) {
        String fullMethodName = req.getMethod() + ":" + req.getServiceMethod();
        var requestHandler = requestMethodHandlers.get(fullMethodName);
        if (requestHandler != null) {
            return requestHandler.apply(req.getServiceMethod(), req);
        }
        return req.responseNotFound();
    }

    /** close WebAPI processor */
    public void close() {
    }
}
