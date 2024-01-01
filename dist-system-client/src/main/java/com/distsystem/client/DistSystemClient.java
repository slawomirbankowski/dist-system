package com.distsystem.client;

import com.distsystem.api.info.AgentInfo;
import com.distsystem.interfaces.HttpCallable;
import com.distsystem.utils.HttpConnectionHelper;

import java.util.Optional;

/** client to connect to standalone Cache Application and provide cache through REST endpoints OR direct Socket connections */
public class DistSystemClient {

    /** base URL */
    private String baseUrl;
    /** connection helper */
    private HttpCallable connectionHelper;

    /** client of agent distributed system  */
    public DistSystemClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.connectionHelper = HttpConnectionHelper.createHttpClient(baseUrl);
    }

    /** check connection */
    public boolean isConnected() {
        return "pong".equals(agentPing());
    }
    /** ping application, should return: pong */
    public String agentPing() {
        return connectionHelper.callGet("/agent/ping").getOutObject().toString();
    }

    public Optional<AgentInfo> agentInfo() {
        return connectionHelper.callGet("/agent/info").parseOutputTo(AgentInfo.class);
        // return connectionHelper.callGet("/agent/info").getOutObject().toString();
    }

}
