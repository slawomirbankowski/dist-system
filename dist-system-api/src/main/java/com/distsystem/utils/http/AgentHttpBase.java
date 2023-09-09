package com.distsystem.utils.http;

import com.distsystem.interfaces.HttpCallable;
import com.distsystem.utils.HttpConnectionHelper;
import com.distsystem.utils.HttpResponseContent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Optional;

/** base HTTP client */
public abstract class AgentHttpBase implements HttpCallable {

    /** base URL of HTTP(s) connection */
    protected String baseUrl;
    /** default timeout in milliseconds to be applied for HTTP connection
     * default is 10seconds
     * */
    protected int defaultTimeout = 10000;

    public AgentHttpBase(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    public AgentHttpBase(String baseUrl, int timeout) {
        this.baseUrl = baseUrl;
        this.defaultTimeout = timeout;
    }

    public abstract HttpResponseContent call(String appendUrl, String method, Optional<String> body, Map<String, String> headers, int timeout);

    /** GET method without body and JSON default header */
    public HttpResponseContent callGet(String appendUrl) {
        return call(appendUrl, HttpConnectionHelper.METHOD_GET, Optional.empty(), HttpConnectionHelper.applicationJsonHeaders, defaultTimeout);
    }
    public HttpResponseContent callGet(String appendUrl, Map<String, String> headers) {
        return call(appendUrl, HttpConnectionHelper.METHOD_GET, Optional.empty(), headers, defaultTimeout);
    }

    public HttpResponseContent callGet(String appendUrl, String body) {
        return call(appendUrl, HttpConnectionHelper.METHOD_GET, Optional.of(body), HttpConnectionHelper.applicationJsonHeaders, defaultTimeout);
    }
    public HttpResponseContent callPost(String appendUrl) {
        return call(appendUrl, HttpConnectionHelper.METHOD_POST, Optional.empty(), HttpConnectionHelper.applicationJsonHeaders, defaultTimeout);
    }
    public HttpResponseContent callPost(String appendUrl, Map<String, String> headers, String body) {
        return call(appendUrl, HttpConnectionHelper.METHOD_POST, Optional.empty(), headers, defaultTimeout);
    }
    public HttpResponseContent callPost(String appendUrl, String body) {
        return call(appendUrl, HttpConnectionHelper.METHOD_POST, Optional.of(body), HttpConnectionHelper.applicationJsonHeaders, defaultTimeout);
    }
    public HttpResponseContent callPostText(String appendUrl, String body) {
        return call(appendUrl, HttpConnectionHelper.METHOD_POST, Optional.of(body), HttpConnectionHelper.applicationTextHeaders, defaultTimeout);
    }
    public HttpResponseContent callPut(String appendUrl, String body) {
        return call(appendUrl, HttpConnectionHelper.METHOD_PUT, Optional.of(body), HttpConnectionHelper.applicationJsonHeaders, defaultTimeout);
    }
    public HttpResponseContent callPut(String appendUrl, Map<String, String> headers, String body) {
        return call(appendUrl, HttpConnectionHelper.METHOD_PUT, Optional.of(body), headers, defaultTimeout);
    }
    public HttpResponseContent callDelete(String appendUrl) {
        return call(appendUrl, HttpConnectionHelper.METHOD_DELETE, Optional.empty(), HttpConnectionHelper.applicationJsonHeaders, defaultTimeout);
    }
    public HttpResponseContent callDelete(String appendUrl, Map<String, String> headers) {
        return call(appendUrl, HttpConnectionHelper.METHOD_DELETE, Optional.empty(), headers, defaultTimeout);
    }
    public HttpResponseContent callDelete(String appendUrl, Map<String, String> headers, String body) {
        return call(appendUrl, HttpConnectionHelper.METHOD_DELETE, Optional.of(body), headers, defaultTimeout);
    }
    protected String tryReadContent(java.io.InputStream inpStr) {
        try {
            StringBuilder responseText = new StringBuilder();
            BufferedReader responseReader = new BufferedReader(new InputStreamReader(inpStr));
            var line = responseReader.readLine();
            while (line != null) {
                responseText.append(line);
                line = responseReader.readLine();
                if (line != null) {
                    responseText.append("\r\n");
                }
            }
            return responseText.toString();
        } catch (Exception ex) {
            return "";

        }
    }

}
