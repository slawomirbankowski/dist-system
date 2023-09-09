package com.distsystem.utils.http;

import com.distsystem.utils.HttpConnectionHelper;
import com.distsystem.utils.HttpResponseContent;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.Optional;

/** HTTP client implemented based on java.net.URL and HttpURLConnection */
public class AgentHttpClient extends AgentHttpBase  {

    public AgentHttpClient(String baseUrl) {
        super(baseUrl);
    }
    public AgentHttpClient(String baseUrl, int timeout) {
        super(baseUrl, timeout);
    }

    public HttpResponseContent call(String appendUrl, String method, Optional<String> body, Map<String, String> headers, int timeout) {
        long startTime = System.currentTimeMillis();
        try {
            java.net.URL url = new java.net.URL(baseUrl + appendUrl);

            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            headers.entrySet().stream().forEach(x -> {
                con.setRequestProperty(x.getKey(), x.getValue());
            });
            con.setRequestMethod(method);
            con.setConnectTimeout(timeout);
            con.setDoInput(true);
            if ((method.equals(HttpConnectionHelper.METHOD_POST) || method.equals(HttpConnectionHelper.METHOD_PUT)) && body.isPresent()) {
                con.setDoOutput(true);
                java.io.BufferedWriter w = new java.io.BufferedWriter(new java.io.OutputStreamWriter(con.getOutputStream()));
                w.write(body.orElse(""));
                w.flush();
                w.close();
            } else {
                con.setDoOutput(false);
            }

            String error = tryReadContent(con.getErrorStream());
            int code = -1;
            try {
                code = con.getResponseCode();
            } catch (Exception ex) {
            }
            String contentType = con.getContentType();

            Object outObject = null;
            long outLength = -1;
            try {
                outLength = con.getContentLengthLong();
                outObject = con.getContent();
            } catch (Exception ex) {
            }
            con.connect();
            return new HttpResponseContent(false, code, outObject, outLength, contentType, error, System.currentTimeMillis()-startTime);
        } catch (IOException ex) {
            return new HttpResponseContent(true, -1, "", -1, "", ex.getMessage(), System.currentTimeMillis()-startTime);
        }
    }

}
