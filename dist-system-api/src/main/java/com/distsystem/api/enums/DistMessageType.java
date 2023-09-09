package com.distsystem.api.enums;

/** types of messages -
 * system - when client is connecting there should be sent message of type welcome, this is to confirm security, get agentUID correctly;
 * request - any message to be sent to given service to do something, execute an action, returns response;
 * response - response of request
 *
 * */
public enum DistMessageType {
    /** when client is connecting, the first message should be welcome type to exchange Agent info, servers, services, ... */
    system,
    /** request - this is a message to be sent to some selected agent(s) and executed on given service with method */
    request,
    /** response - this is a message as a response from request, back from service/method */
    response
}
