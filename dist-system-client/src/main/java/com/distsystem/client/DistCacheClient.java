package com.distsystem.client;

import com.distsystem.interfaces.HttpCallable;
import com.distsystem.utils.HttpConnectionHelper;

/** client to connect to standalone Cache Application and provide cache through REST endpoints OR direct Socket connections */
public class DistCacheClient {

    private String baseUrl;
    private HttpCallable connectionHelper;

    public DistCacheClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.connectionHelper = HttpConnectionHelper.createHttpClient(baseUrl);
    }

    /** check connection */
    public boolean isConnected() {
        return "pong".equals(ping());
    }
    /** ping application, should returns: pong */
    public String ping() {
        return connectionHelper.callGet("/api/v1/ping").getOutObject().toString();
    }

}
