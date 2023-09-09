package com.distsystem.utils.http;

import com.distsystem.utils.HttpConnectionHelper;
import com.distsystem.utils.HttpResponseContent;

import javax.net.ssl.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Optional;

/** HTTPS client implemented based on java.net.URL and HttpsURLConnection  */
public class AgentHttpsClient extends AgentHttpBase {

    public AgentHttpsClient(String baseUrl) {
        super(baseUrl);
    }
    public AgentHttpsClient(String baseUrl, int timeout) {
        super(baseUrl, timeout);
    }

    public HttpResponseContent call(String appendUrl, String method, Optional<String> body, Map<String, String> headers, int timeout) {
        long startTime = System.currentTimeMillis();
        try {
            java.net.URL url = new java.net.URL(baseUrl + appendUrl);
            HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
            headers.entrySet().stream().forEach(x -> {
                con.setRequestProperty(x.getKey(), x.getValue());
            });
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager(){
                        public X509Certificate[] getAcceptedIssuers(){ return null; }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }
            };
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            con.setSSLSocketFactory(sslContext.getSocketFactory());
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
                outObject = tryReadContent(con.getInputStream());
            } catch (Exception ex) {
            }
            con.connect();
            return new HttpResponseContent(false, code, outObject, outLength, contentType, error, System.currentTimeMillis()-startTime);
        } catch (Exception ex) {
            return new HttpResponseContent(true, -1, "", -1, "", ex.getMessage(), System.currentTimeMillis()-startTime);
        }
    }

}
