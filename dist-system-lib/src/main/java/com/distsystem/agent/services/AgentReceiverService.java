package com.distsystem.agent.services;

import com.distsystem.api.*;
import com.distsystem.api.enums.DistComponentType;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.base.ServiceBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentComponent;
import com.distsystem.interfaces.Receiver;
import com.distsystem.utils.DistUtils;
import com.distsystem.utils.DistWebApiProcessor;
import com.distsystem.utils.JsonUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/** service to receive messages to any listener */
public class AgentReceiverService extends ServiceBase implements Receiver, AgentComponent {

    /** all external methods to process messages */
    protected Map<String, Function<DistMessage, DistMessage>> methodsToProcess = new HashMap<>();

    /** processor class for Web API - instant synchronized API to be used directly with Agent service */
    private final DistWebApiProcessor webApiProcessor = new DistWebApiProcessor()
            .addHandlerGet("ping", (m, req) -> req.responseOkText("ping"))
            .addHandlerGet("methods", (m, req) -> req.responseOkJson(JsonUtils.serialize(methodsToProcess.keySet())));

    /** */
    public AgentReceiverService(Agent parentAgent) {
        super(parentAgent);
    }

    @Override
    public DistComponentType getComponentType() {
        return DistComponentType.receiver;
    }
    /** get global unique ID of this service/component */
    @Override
    public String getGuid() {
        return guid;
    }
    /** get custom map of info about service */
    public Map<String, String> getServiceInfoCustomMap() {
        return Map.of("methodsCount", ""+methodsToProcess.size(),
                "methodNames", String.join(",", methodsToProcess.keySet()));
    }
    /** register new method to process received messages */
    public void registerReceiverMethod(String method, Function<DistMessage, DistMessage> methodToProcess) {
        methodsToProcess.put(method, methodToProcess);
    }
    /** get number of receiver methods */
    public int getMethodsCount() {
        return methodsToProcess.size();
    }
    /** send message to another receiver*/
    public void send(String agentUid, String method, Object obj) {
        DistMessageFull msg = DistMessageBuilder.empty()
                .toDestination(agentUid, DistServiceType.receiver, method)
                .withObject(obj)
                .build();
        parentAgent.sendMessage(msg);
    }
    @Override
    public DistServiceType getServiceType() {
        return DistServiceType.receiver;
    }
    /** process message using registered */
    @Override
    public DistMessage processMessage(DistMessage msg) {
        Function<DistMessage, DistMessage> methodToProcess = methodsToProcess.get(msg.getMethod());
        if (methodToProcess == null) {
            return msg.methodNotFound();
        } else {
            try {
                return methodToProcess.apply(msg);
            } catch (Exception ex) {
                return msg.exception(ex);
            }
        }
    }
    @Override
    public AgentWebApiResponse handleRequest(AgentWebApiRequest request) {
        return webApiProcessor.handleRequest(request);
    }
    @Override
    protected String createServiceUid() {
        return DistUtils.generateCacheGuid();
    }

    @Override
    protected void onClose() {
    }
}
