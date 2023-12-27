package com.distsystem.utils;

import com.distsystem.api.*;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.api.info.DistWebApiInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Processor for Agent Web API to handle requests and produce responses
 * it is keeping methods
 * */
public class DistWebApiProcessor {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(DistWebApiProcessor.class);
    /** */
    private final DistServiceType servType;
    /** */
    private final String servName;
    /** key -> method with name like GET:/agent/info
     * value - method to process this Web Api Request */
    private final HashMap<String, AgentWebApiMethod> requestMethodHandlers = new HashMap<>();
    /** all external methods to process messages */
    protected Map<String, Function<DistMessage, DistMessage>> messageHandlers = new HashMap<>();
    private final LinkedList<AgentWebApiEvent> latestRequest = new LinkedList<>();
    private final AtomicLong handledRequests = new AtomicLong();
    private final AtomicLong handledRequestsTimeMs = new AtomicLong();
    /** counter of received messages */
    private final AtomicLong receivedMessages = new AtomicLong();
    private final AtomicLong exceptionMessages = new AtomicLong();

    public DistWebApiProcessor(DistServiceType servType) {
        this.servType = servType;
        this.servName = servType.name();
    }
    public DistWebApiProcessor(String servType) {
        this.servType = DistServiceType.custom;
        this.servName = servType;
    }
    public DistWebApiInfo getInfo() {
        return new DistWebApiInfo(requestMethodHandlers.size(), messageHandlers.size(),
                receivedMessages.get(), exceptionMessages.get(),
                handledRequests.get(), handledRequestsTimeMs.get());
    }
    /** get current number of request handler registered */
    public long getRequestHandlersCount() {
        return requestMethodHandlers.size();
    }
    /** produce handler key from request method and service method */
    private String getHandlerKey(String requestMethod, String serviceMethod) {
        return requestMethod + ":/" + servName + "/" + serviceMethod;
    }
    /** merge with another web api processor */
    public DistWebApiProcessor merge(DistWebApiProcessor processor) {
        int currentHandlers = requestMethodHandlers.size();
        requestMethodHandlers.putAll(processor.requestMethodHandlers);
        log.trace("Merging processor with additional handlers, service: " + servName + ", current handlers: " + currentHandlers +" with: " + processor.requestMethodHandlers.size() + ", final: " + requestMethodHandlers.size() + ", new keys: " + processor.requestMethodHandlers.keySet());
        return this;
    }
    /** register new method to process received messages */
    public DistWebApiProcessor registerReceiverMethod(String method, Function<DistMessage, DistMessage> methodToProcess) {
        messageHandlers.put(method, methodToProcess);
        return this;
    }
    /** get message handler methods */
    public List<String> getMessageHandlerMethods() {
        return messageHandlers.keySet().stream().toList();
    }
    /** get message handlers count */
    public int getMessageHandlersCount() {
        return messageHandlers.size();
    }
    /** add new handler */
    public DistWebApiProcessor addHandler(String requestMethod, String serviceMethod, BiFunction<String, AgentWebApiRequest, AgentWebApiResponse> handler, Set<String> roles) {
        String fullKey = getHandlerKey(requestMethod,serviceMethod);
        AgentWebApiMethod method = new AgentWebApiMethod(fullKey, requestMethod, serviceMethod, handler, roles);
        requestMethodHandlers.put(fullKey, method);
        return this;
    }

    public DistWebApiProcessor addHandlerGet(String serviceMethod, BiFunction<String, AgentWebApiRequest, AgentWebApiResponse> method) {
        return addHandler("GET", serviceMethod, method, AgentRoles.anonymousRoles);
    }
    public DistWebApiProcessor addHandlerGet(String serviceMethod, BiFunction<String, AgentWebApiRequest, AgentWebApiResponse> method, Set<String> roles) {
        return addHandler("GET", serviceMethod, method, roles);
    }
    public DistWebApiProcessor addHandlerPost(String serviceMethod, BiFunction<String, AgentWebApiRequest, AgentWebApiResponse> method) {
        return addHandler("POST", serviceMethod, method, AgentRoles.anonymousRoles);
    }
    public DistWebApiProcessor addHandlerPost(String serviceMethod, BiFunction<String, AgentWebApiRequest, AgentWebApiResponse> method, Set<String> roles) {
        return addHandler("POST", serviceMethod, method, roles);
    }
    public DistWebApiProcessor addHandlerPut(String serviceMethod, BiFunction<String, AgentWebApiRequest, AgentWebApiResponse> method) {
        return addHandler("PUT", serviceMethod, method, AgentRoles.anonymousRoles);
    }
    public DistWebApiProcessor addHandlerPut(String serviceMethod, BiFunction<String, AgentWebApiRequest, AgentWebApiResponse> method, Set<String> roles) {
        return addHandler("PUT", serviceMethod, method, roles);
    }
    public DistWebApiProcessor addHandlerDelete(String serviceMethod, BiFunction<String, AgentWebApiRequest, AgentWebApiResponse> method) {
        return addHandler("DELETE", serviceMethod, method, AgentRoles.anonymousRoles);
    }
    public DistWebApiProcessor addHandlerDelete(String serviceMethod, BiFunction<String, AgentWebApiRequest, AgentWebApiResponse> method, Set<String> roles) {
        return addHandler("DELETE", serviceMethod, method, roles);
    }

    /** handle simple request - find method and execute it to get response for Web API */
    public AgentWebApiResponse handleSimpleRequest(String requestKey) {
        AgentWebApiRequest req = new AgentWebApiRequest(requestKey, servType.name());
        return handleRequest(req);
    }
    /** handle request - find method and execute it to get response for Web API */
    public AgentWebApiResponse handleRequest(AgentWebApiRequest req) {
        String fullMethodName = req.getRequestKey(); // getHandlerKey(req.getMethod(),req.getServiceMethod());
        var requestHandler = requestMethodHandlers.get(fullMethodName);
        handledRequests.incrementAndGet();
        if (requestHandler != null) {
            long startReqTime = System.currentTimeMillis();
            AgentWebApiResponse res = requestHandler.getMethod().apply(req.getServiceMethod(), req);
            long totalReqTime = System.currentTimeMillis() - startReqTime;
            handledRequestsTimeMs.addAndGet(totalReqTime);
            addEvent(req.getReqSeq(), req.getMethod(), req.getServiceMethod(), true, res.getResponseCode(), res.getContent().length());
            return res;
        } else {
            addEvent(req.getReqSeq(), req.getMethod(), req.getServiceMethod(), false, -1, 0);
            return req.responseNotFoundMethod("Cannot find method in service: " + this.servType.name() + ", method: " + req.getMethod() + ", full key: " + fullMethodName + ", existing methods: " + getAllHandlersAsString());
        }
    }
    /** process message - find method and execute it */
    public DistMessage processMessage(DistMessage msg) {
        receivedMessages.incrementAndGet();
        try {
            // GET:/service/method
            String fullMethodName = "GET:/" + msg.getToService().name() + "/" + msg.getRequestMethod();
            var requestHandler = requestMethodHandlers.get(fullMethodName);
            if (requestHandler != null) {
                AgentWebApiRequest req = new AgentWebApiRequest(msg);
                AgentWebApiResponse resp = requestHandler.getMethod().apply("", req);
                return msg.response(resp.getContent(), DistMessageStatus.ok);
            } else {
                Function<DistMessage, DistMessage> msgHandler = messageHandlers.get(msg.getRequestMethod());
                if (msgHandler != null) {
                    try {
                        return msgHandler.apply(msg);
                    } catch (Exception ex) {
                        // TODO: add to agent issue
                        // addIssueToAgent("onProcessMessage", ex);
                        return msg.exception(ex);
                    }
                } else {
                    return msg.methodNotFound();
                }
            }
        } catch (Exception ex) {
            // TODO: status is Exception
            exceptionMessages.incrementAndGet();
            return msg.exception(ex);
        }
    }

    /** add new handler event */
    private void addEvent(long requestSeq, String method, String serviceMethod, boolean methodFound, int responseCode, long responseSize) {
        latestRequest.push(new AgentWebApiEvent(requestSeq, method, serviceMethod, methodFound, responseCode, responseSize));
        while (latestRequest.size() > 100) {
            latestRequest.pop();
        }
    }
    /** get all handlers for this API processor */
    public List<String> getAllHandlers() {
        return requestMethodHandlers.keySet().stream().toList();
    }
    /** get the latest requests in this Api processor */
    public List<AgentWebApiEvent> getLatestRequests() {
        return latestRequest;
    }
    public String getAllHandlersAsString() {
        return getAllHandlers().stream().collect(Collectors.joining("|"));
    }
    /** close WebAPI processor */
    public void close() {
    }
}
