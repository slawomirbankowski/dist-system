package com.distsystem.utils;

import com.distsystem.interfaces.HttpCallable;
import com.distsystem.utils.http.AgentHttpClient;
import com.distsystem.utils.http.AgentHttpsClient;

import java.util.Map;

/** Helper for HTTP connection - this is helping get proper client for HTTP connection */
public class HttpConnectionHelper {

    // TODO: change classes to create HTTP and HTTPs clients

    private HttpConnectionHelper() {
    }

    /** */
    public static HttpCallable createHttpClient(String url, int defaultTimeout) {
        if (url.startsWith("https")) {
            return new AgentHttpsClient(url, defaultTimeout);
        } else {
            return new AgentHttpClient(url, defaultTimeout);
        }
    }
    /** */
    public static HttpCallable createHttpClient(String url) {
        if (url.startsWith("https")) {
            return new AgentHttpsClient(url, 5000);
        } else {
            return new AgentHttpClient(url, 5000);
        }
    }


    public static final Map<String, String> emptyHeaders = Map.of();
    public static final Map<String, String> applicationJsonHeaders = Map.of("Content-Type", "application/json");
    public static final Map<String, String> applicationTextHeaders = Map.of("Content-Type", "text/html");

    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_DELETE = "DELETE";


}
