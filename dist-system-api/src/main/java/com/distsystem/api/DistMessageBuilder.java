package com.distsystem.api;

import com.distsystem.api.enums.DistCallbackType;
import com.distsystem.api.enums.DistMessageType;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.DistService;
import com.distsystem.utils.DistUtils;

import java.time.LocalDateTime;
import java.util.function.Function;

/** Builder of messages to be sent between agents.
 * */
public class DistMessageBuilder {

    /** type of message - this could be welcome, system, request, response, noOperation, ... */
    private DistMessageType messageType = DistMessageType.request;
    /** source agent for this message - this is UID of source agent */
    private String fromAgent = "";
    /** source service for this message */
    private DistServiceType fromService = DistServiceType.agent;
    /** UID of destination agent OR any mass-pointer for agent like broadcast, random, round-robin, first, last-connected */
    private String toAgent = DistMessage.sendToBroadcast;
    /** destination service to receive this message */
    private DistServiceType toService = DistServiceType.agent;
    /** method to execute at given service */
    private String requestMethod = "nop";
    /** */
    private String responseMethod = "";
    /** serializable object of this message */
    private Object message = "";
    /** tags to help catalog this message, tags are comma-separated string table */
    private String tags = "";
    /** end of validity for this message, after that time timeout would be called from callbacks and message would be deleted */
    private LocalDateTime validTill = DistMessage.getValidTillDefault();
    /** callbacks to be called when response would be back or there will be timeout or error while sending or resending */
    private final DistCallbacks callbacks = DistCallbacks.createEmpty();

    public DistMessageBuilder fromService(DistServiceType srv) {
        this.fromService = srv;
        return this;
    }
    public DistMessageBuilder fromService(DistService srv) {
        this.fromAgent = srv.getAgent().getAgentGuid();
        this.fromService = srv.getServiceType();
        return this;
    }
    public DistMessageBuilder fromAgent(Agent agent) {
        this.fromAgent = agent.getAgentGuid();
        return this;
    }
    public DistMessageBuilder fromAgent(String agentUid) {
        this.fromAgent = agentUid;
        return this;
    }
    public DistMessageBuilder toAgent(String agentUid) {
        toAgent = DistMessage.sendToBroadcast;
        return this;
    }
    public DistMessageBuilder toAll() {
        toAgent = DistMessage.sendToBroadcast;
        return this;
    }
    public DistMessageBuilder toRandom() {
        toAgent = DistMessage.sendToRandom;
        return this;
    }
    public DistMessageBuilder toService(DistServiceType toService) {
        this.toService = toService;
        return this;
    }
    public DistMessageBuilder toDestination(String agentUid, DistServiceType toService) {
        this.toAgent = agentUid;
        this.toService = toService;
        return this;
    }
    public DistMessageBuilder toDestination(String agentUid, DistServiceType toService, String requestMethod) {
        this.toAgent = agentUid;
        this.toService = toService;
        this.requestMethod = requestMethod;
        return this;
    }

    public DistMessageBuilder withObject(Object obj) {
        this.message = obj;
        return this;
    }
    public DistMessageBuilder withTag(String tag) {
        this.tags = tag;
        return this;
    }
    public DistMessageBuilder withTags(String tags) {
        this.tags = tags;
        return this;
    }
    public DistMessageBuilder withTags(String[] tgs) {
        this.tags = "";
        return this;
    }
    public DistMessageBuilder toDestination(String requestMethod) {
        this.requestMethod = requestMethod;
        return this;
    }
    public DistMessageBuilder validForHours(long hours) {
        this.validTill = LocalDateTime.now().plusHours(hours);
        return this;
    }
    public DistMessageBuilder validForMinutes(long minutes) {
        this.validTill = LocalDateTime.now().plusMinutes(minutes);
        return this;
    }
    public DistMessageBuilder validForSeconds(long seconds) {
        this.validTill = LocalDateTime.now().plusSeconds(seconds);
        return this;
    }
    public DistMessageBuilder validTill(LocalDateTime till) {
        this.validTill = till;
        return this;
    }
    /** */
    public DistMessageBuilder withCallback(DistCallbackType ct, Function<DistMessage, Boolean> callbackMethod) {
        callbacks.addCallback(ct, callbackMethod);
        return this;
    }

    /** build final message to be sent */
    public DistMessageFull build() {
        DistMessage msg = new DistMessage(DistUtils.generateMessageGuid(), LocalDateTime.now(),
                messageType, fromAgent, fromService, toAgent, toService, requestMethod, responseMethod, message, tags, validTill, DistMessageStatus.init);
        return new DistMessageFull(msg, callbacks);
    }

    /** creates empty builder */
    public static DistMessageBuilder empty() {
        return new DistMessageBuilder();
    }

}
