package com.distsystem.interfaces;

import com.distsystem.utils.HttpResponseContent;

import java.util.Map;
import java.util.Optional;

/** interface for HTTP/REST classes */
public interface HttpCallable {

    HttpResponseContent call(String appendUrl, String method, Optional<String> body, Map<String, String> headers, int timeout);
    /** GET method without body and JSON default header */
    HttpResponseContent callGet(String appendUrl);
    HttpResponseContent callGet(String appendUrl, Map<String, String> headers);
    HttpResponseContent callGet(String appendUrl, String body);
    HttpResponseContent callPost(String appendUrl);
    HttpResponseContent callPost(String appendUrl, Map<String, String> headers, String body);
    HttpResponseContent callPost(String appendUrl, String body);
    HttpResponseContent callPostText(String appendUrl, String body);
    HttpResponseContent callPut(String appendUrl, String body);
    HttpResponseContent callPut(String appendUrl, Map<String, String> headers, String body);
    HttpResponseContent callDelete(String appendUrl);
    HttpResponseContent callDelete(String appendUrl, Map<String, String> headers);
    HttpResponseContent callDelete(String appendUrl, Map<String, String> headers, String body);
}
