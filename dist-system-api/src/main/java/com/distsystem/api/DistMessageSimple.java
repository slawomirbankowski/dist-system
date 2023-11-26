package com.distsystem.api;


import java.io.Serializable;
import java.time.LocalDateTime;

/** interface for message sent by dist service to another service between agent
 * message should be serialized and sent using known servers-clients like Socket, Datagram, Kafka, HTTP, ...
 *  */
public class DistMessageSimple implements Serializable {

    /** unique ID of this message */
    private String messageUid;
    /** date and time of creation of this message */
    private LocalDateTime createdDate;
    /** type of message - this could be welcome, system, request, response, noOperation, ... */
    private String messageType;
    /** source agent for this message - this is UID of source agent */
    private String fromAgent;
    /** source service for this message */
    private String fromService;
    /** UID of destination agent OR any mass-pointer for agent like broadcast, random, round-robin, first, last-connected */
    private String toAgent;
    /** destination service to receive this message */
    private String toService;
    /** method to execute at given service */
    private String requestMethod;
    /** */
    private String responseMethod;
    /** status */
    private String status;
    /** tags to help catalog this message, tags are comma-separated string table */
    private String tags;
    /** end of validity for this message, after that time timeout would be called from callbacks and message would be deleted */
    private LocalDateTime validTill;

    public DistMessageSimple(String messageUid, LocalDateTime createdDate, String messageType, String fromAgent, String fromService, String toAgent, String toService, String requestMethod, String responseMethod, String status, String tags, LocalDateTime validTill) {
        this.messageUid = messageUid;
        this.createdDate = createdDate;
        this.messageType = messageType;
        this.fromAgent = fromAgent;
        this.fromService = fromService;
        this.toAgent = toAgent;
        this.toService = toService;
        this.requestMethod = requestMethod;
        this.responseMethod = responseMethod;
        this.status = status;
        this.tags = tags;
        this.validTill = validTill;
    }

    public String getMessageUid() {
        return messageUid;
    }

    public String getCreatedDate() {
        return createdDate.toString();
    }

    public String getMessageType() {
        return messageType;
    }

    public String getFromAgent() {
        return fromAgent;
    }

    public String getFromService() {
        return fromService;
    }

    public String getToAgent() {
        return toAgent;
    }

    public String getToService() {
        return toService;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getResponseMethod() {
        return responseMethod;
    }

    public String getStatus() {
        return status;
    }

    public String getTags() {
        return tags;
    }

    public String getValidTill() {
        return validTill.toString();
    }
}
