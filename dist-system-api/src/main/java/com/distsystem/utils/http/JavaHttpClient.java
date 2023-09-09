package com.distsystem.utils.http;

import com.distsystem.utils.HttpResponseContent;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** HTTP client implemented on native java.net.http.HttpClient class */
public class JavaHttpClient extends AgentHttpBase {

    public JavaHttpClient(String baseUrl) {
        super(baseUrl);
    }
    public JavaHttpClient(String baseUrl, int timeout) {
        super(baseUrl, timeout);
    }

    public HttpResponseContent call(String appendUrl, String method, Optional<String> body, Map<String, String> headers, int timeout) {
        long startTime = System.currentTimeMillis();
        try {
            HttpClient client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(Duration.ofSeconds(timeout/1000))
                    .build();
            HttpRequest.BodyPublisher bodyPub;
            if (body.isPresent() ) {
                bodyPub = HttpRequest.BodyPublishers.ofString(body.get());
            } else {
                bodyPub = HttpRequest.BodyPublishers.noBody();
            }
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + appendUrl))
                    .timeout(Duration.ofSeconds(timeout/1000))
                    .header("Content-Type", "application/json")
                    .method(method, bodyPub)
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String contentType = "";
            response.headers().map().getOrDefault("Content-Type", List.of());
            return new HttpResponseContent(false, response.statusCode(), response.body(), response.body().length(), contentType, "", System.currentTimeMillis()-startTime);
        } catch (IOException | InterruptedException ex) {
            return new HttpResponseContent(true, -1, "", -1, "", ex.getMessage(), System.currentTimeMillis()-startTime);
        }
    }

}
