package com.distsystem.utils;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Optional;

/** response of HTTP(s) call - in case of OK or ERROR */
public class HttpResponseContent {

    /** true when call returned 2xx, false otherwise - if call returned 3xx or 4xx or 5xx or there is no connection or timeout */
    private boolean isOk;
    /** final HTTP code returned OR -1 if there is no connection */
    private int code;
    /** read object from call - this is the response body */
    private Object outObject;
    /** length of output object */
    private long outLength;
    private String contentType;
    private String error;
    /** total request-response time in milliseconds */
    private long totalTimeMs;

    /** creates new HTTP response content */
    public HttpResponseContent(boolean isOk, int code, Object outObject, long outLength, String contentType, String error, long totalTimeMs) {
        this.isOk = isOk;
        this.code = code;
        this.outObject = outObject;
        this.outLength = outLength;
        this.contentType = contentType;
        this.error = error;
        this.totalTimeMs = totalTimeMs;
    }

    public boolean isOk() {
        return isOk;
    }

    public int getCode() {
        return code;
    }

    public Object getOutObject() {
        return outObject;
    }
    public String getOutString() {
        if (isOk) {
            return "" + outObject;
        } else {
            return "";
        }
    }

    public <T> Optional<T> parseOutputTo(Class<T> outClass) {
        try {
            T obj = JsonUtils.deserialize("" + getOutObject(), outClass);
            if (obj != null) {
                return Optional.of(obj);
            } else {
                return Optional.empty();
            }
        } catch (Exception ex) {
            return Optional.empty();
        }
    }
    /** */
    public Optional<String> parseOutputToString() {

        return Optional.empty();
    }
    /** */
    public Optional<Long> parseOutputToLong() {


        return Optional.empty();
    }
    public <T> Optional<T> parseOutputTo(TypeReference<T> type) {
        try {
            T obj = JsonUtils.deserialize("" + getOutObject(), type);
            if (obj != null) {
                return Optional.of(obj);
            } else {
                return Optional.empty();
            }
        } catch (Exception ex) {
            return Optional.empty();
        }
    }
    public long getOutLength() {
        return outLength;
    }

    public String getContentType() {
        return contentType;
    }

    public String getError() {
        return error;
    }

    public long getTotalTimeMs() {
        return totalTimeMs;
    }
    public String getInfo() {
        return "code=" + code + ", time=" + totalTimeMs + ", len=" + outLength;
    }

}
