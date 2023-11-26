package com.distsystem.agent.services;

import com.distsystem.api.*;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.base.ServiceBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.Receiver;
import com.distsystem.utils.DistWebApiProcessor;
import com.distsystem.utils.JsonUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/** service to receive messages to any custom listener */
public class AgentReceiverService extends ServiceBase implements Receiver {

    /** */
    public AgentReceiverService(Agent parentAgent) {
        super(parentAgent);
        parentAgent.getServices().registerService(this);
    }

    /** count objects in this agentable object including this object */
    public long countObjectsService() {
        return 1L;
    }

    /** read configuration and re-initialize this component */
    protected boolean onReinitialize() {
        // TODO: implement reinitialization
        return true;
    }

    /** additional web API endpoints */
    protected DistWebApiProcessor additionalWebApiProcessor() {
        return new DistWebApiProcessor(getServiceType())
                .addHandlerGet("method-names", (m, req) -> req.responseOkJsonSerialize(webApiProcessor.getMessageHandlerMethods()))
                .addHandlerPost("send", (m, req) -> req.responseOkJsonSerialize(sendMessage(req.getContentAsString())));
    }

    /** get custom map of info about service */
    public Map<String, String> getServiceInfoCustomMap() {
        return Map.of("methodsCount", ""+webApiProcessor.getMessageHandlersCount(),
                "methodNames", String.join(",", webApiProcessor.getMessageHandlerMethods()));
    }
    /** register new method to process received messages */
    public void registerReceiverMethod(String method, Function<DistMessage, DistMessage> methodToProcess) {
        webApiProcessor.registerReceiverMethod(method, methodToProcess);
    }
    /** get number of receiver methods */
    public int getMethodsCount() {
        return webApiProcessor.getMessageHandlersCount();
    }

    /** send message to another receiver*/
    public Map<String, String> sendMessage(String messageJson) {
        DistMessageFull msg = DistMessageFull.fromJson(messageJson);
        DistMessageFull response = parentAgent.sendMessage(msg);
        return response.getMessage().toMap();
    }
    /** send message to another receiver*/
    public DistMessageFull send(String agentUid, String method, Object obj) {
        DistMessageFull msg = DistMessageBuilder.empty()
                .toDestination(agentUid, DistServiceType.receiver, method)
                .withObject(obj)
                .build();
        return parentAgent.sendMessage(msg);
    }
    @Override
    public DistServiceType getServiceType() {
        return DistServiceType.receiver;
    }

    /** update configuration of this Service */
    public void updateConfig(DistConfig newCfg) {
        // TODO: update configuration of this service
    }
    @Override
    protected void onClose() {
    }
}
