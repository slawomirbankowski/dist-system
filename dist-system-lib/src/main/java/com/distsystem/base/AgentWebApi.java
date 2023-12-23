package com.distsystem.base;

import com.distsystem.api.info.AgentApiInfo;
import com.distsystem.interfaces.Agent;

import java.util.concurrent.atomic.AtomicLong;

/** interface for WebAPI to publish Web REST API to directly connect to an Agent and manage it.
 * Web API is fast direct connection to Agent with many methods to be called, endpoints are in format:
 *  METHOD /service/method/parameters
 * Example:
 *  GET /agent/ping
 *  GET /agent/info
 *  POST /cache/initialize-single-storage/com.distsystem.storages.JdbcStorage
 *  DELETE /cache/objects/key_to_be_cleared
 * */
public abstract class AgentWebApi extends AgentableBase {
    /** sequence for requests - GLOBAL ONE !!!, this is counting all API requests for WebSimpleApi */
    public static final AtomicLong requestSeq = new AtomicLong();

    /** */
    public AgentWebApi(Agent parentAgent) {
        super(parentAgent);
    }

    /** get type of this API */
    public abstract String getApiType();
    /** get information about this simple API */
    public abstract AgentApiInfo getInfo();
    /** get port of this WebAPI */
    public abstract int getPort();
    /** start this API */
    public abstract void startApi();
    /** check this Web API */
    public abstract boolean check();
    /** get number of requests */
    public abstract long getHandledRequestsCount();
    /** get total time of requests */
    public abstract long getHandledRequestsTime();
    /** get count of errors in requests */
    public abstract long getHandledRequestsErrors();

}
