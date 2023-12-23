package com.distsystem.agent.apis;

import com.distsystem.api.AgentWebApiRequest;
import com.distsystem.api.AgentWebApiResponse;
import com.distsystem.api.DistConfig;
import com.distsystem.api.info.AgentApiInfo;
import com.distsystem.base.AgentWebApi;
import com.distsystem.interfaces.AgentApi;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/** simple implementation of web api for agent
 * Web API is simple REST api to access direct agent services with methods
 * It is creating HTTP server that is redirecting request to proper service based on URI
 * Agent Web API URI must be like:
 *  http(s)://host[:port]/SERVICE/METHOD
 * Where:
 * SERVICE = agent | cache | report |
 * METHOD = method in service
 * */
public class WebSimpleApi extends AgentWebApi {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(WebSimpleApi.class);
    /** parent API class to execute request methods */
    private final AgentApi parentApi;
    /** HTTP server for Agent Web API */
    private com.sun.net.httpserver.HttpServer httpServer;
    /** handler to handle HTTP requests */
    private WebSimpleApi.WebSimpleApiHandler httpHandler;
    /** port of this Web API server */
    private final int webApiPort;
    /** if API is started */
    private boolean started = false;

    /** creates new Web API and initialize it */
    public WebSimpleApi(AgentApi api) {
        super(api.getAgent());
        parentApi = api;
        webApiPort = parentApi.getAgent().getConfig().getPropertyAsInt(DistConfig.AGENT_API_PORT, DistConfig.AGENT_API_PORT_DEFAULT_VALUE);
        startApi();

    }

    /** count objects in this agentable object including this object */
    public long countObjectsAgentable() {
        return 2L;
    }
    /** start this Agent Web API */
    public void startApi() {
        try {
            log.info("Starting new Web API for agent as HTTP server at port:" + webApiPort + ", agent: " + parentApi.getAgent().getAgentGuid());
            httpServer = HttpServer.create(new InetSocketAddress(webApiPort), 0);
            httpHandler = new WebSimpleApi.WebSimpleApiHandler(parentApi);
            httpServer.createContext("/", httpHandler);
            httpServer.setExecutor(Executors.newFixedThreadPool(10)); // set default thread as Executor
            httpServer.start();
            started = true;
            log.info("Started HTTP server as Agent Web API on port: " + webApiPort +", agent: " + parentApi.getAgent().getAgentGuid());
        } catch (Exception ex) {
            log.warn("Cannot start HTTP server as Agent Web API, reason: " + ex.getMessage());
            parentApi.getAgent().getIssues().addIssue("WebSimpleApi.startApi", ex);
            started = false;
        }
    }
    /** get type of this Agent Web API */
    public String getApiType() {
        return "simple";
    }
    /** get port of this WebAPI */
    public int getPort() {
        return webApiPort;
    }
    /** get information about this simple API */
    public AgentApiInfo getInfo() {
        return new AgentApiInfo(getApiType(), webApiPort,
                httpHandler.getHandledRequestsCount(),
                httpHandler.getHandledRequestsTime(),
                httpHandler.getHandledRequestsErrors());
    }
    /** get number of requests */
    public long getHandledRequestsCount() {
        return httpHandler.getHandledRequestsCount();
    }
    /** get total time of requests */
    public long getHandledRequestsTime() {
        return httpHandler.getHandledRequestsTime();
    }
    /** get count of errors in requests */
    public long getHandledRequestsErrors() {
        return httpHandler.getHandledRequestsErrors();
    }

    /** check this Web API */
    public boolean check() {
        return true;
    }
    /** close this Agent Web API */
    protected void onClose() {
        try {
            log.info("Try to close Agent Web API");
            httpServer.stop(3);
            started = false;
        } catch (Exception ex) {
            parentApi.getAgent().getIssues().addIssue("WebSimpleApi.close", ex);
        }
    }
    /** handler of HTTP requests */
    static class WebSimpleApiHandler implements HttpHandler {
        /** local logger for this class*/
        protected static final Logger log = LoggerFactory.getLogger(WebSimpleApiHandler.class);
        /** parent API for this web server */
        private AgentApi parentApi;
        /** total number of requests handled */
        private AtomicLong handledRequestsCount = new AtomicLong();
        /** total time of handled requests */
        private AtomicLong handledRequestsTime = new AtomicLong();
        /** total number of errors in requests */
        private AtomicLong handledRequestsErrors = new AtomicLong();

        public WebSimpleApiHandler(AgentApi parentApi) {
            this.parentApi = parentApi;
        }

        /** handle HTTP request */
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            long reqSeq = AgentWebApi.requestSeq.incrementAndGet();
            long startTime = System.currentTimeMillis();
            try {
                handledRequestsCount.incrementAndGet();
                Map<String, Object> attributes = new HashMap<>();
                attributes.putAll(httpExchange.getHttpContext().getAttributes());
                //httpExchange.getHttpContext().getAttributes();
                //httpExchange.getRequestURI().getRawPath()
                AgentWebApiRequest req = new AgentWebApiRequest(reqSeq, startTime, httpExchange.getProtocol(), httpExchange.getRequestMethod(), httpExchange.getRequestURI(), httpExchange.getRequestHeaders(), httpExchange.getRequestBody().readAllBytes(), httpExchange.getRequestURI().getRawQuery(), attributes);
                log.info(">>>>> HANDLE REQUEST [" + reqSeq + "], protocol: " + httpExchange.getProtocol() + ", method: " + httpExchange.getRequestMethod() + ", HEADERS.size: " + httpExchange.getRequestHeaders().size() + ", URI: " + httpExchange.getRequestURI().toString() + ", service: " + req.getServiceName());
                AgentWebApiResponse response = parentApi.getAgent().getServices().dispatchRequest(req);
                var respBytes = response.getResponseContent();
                httpExchange.getResponseHeaders().putAll(response.getHeaders());
                long totalTime = System.currentTimeMillis() - startTime;
                handledRequestsTime.addAndGet(totalTime);
                httpExchange.getResponseHeaders().put("Request-Time", List.of(""+totalTime));
                httpExchange.getResponseHeaders().put("Request-Seq", List.of(""+reqSeq));
                httpExchange.getResponseHeaders().put("Request-User", List.of("ANONYMOUS")); // TODO: add Authorization to request-response
                httpExchange.getResponseHeaders().put("Agent-Guid", List.of(parentApi.getAgent().getAgentGuid()));
                httpExchange.sendResponseHeaders(response.getResponseCode(), respBytes.length);
                OutputStream os = httpExchange.getResponseBody();
                os.write(respBytes);
                os.flush();
                log.info(">>>>> END OF HANDLE REQUEST[" + reqSeq + "], totalTime: " + totalTime + ", code: " + response.getResponseCode() + " content.len: " + response.getResponseContent().length + ", headers: " + httpExchange.getResponseHeaders().size());
                os.close();
            } catch (Exception ex) {
                handledRequestsErrors.incrementAndGet();
                log.warn("Web API Exception: " + ex.getMessage());
                parentApi.getAgent().getIssues().addIssue("WebSimpleApiHandler.handle", ex);
                String errorResponse = ">>>>> ERROR DURING REQUEST [" + reqSeq +  "] +, reason: " + ex.getMessage();
                httpExchange.sendResponseHeaders(501, errorResponse.length());
                OutputStream os = httpExchange.getResponseBody();
                os.write(errorResponse.getBytes());
                os.flush();
            }
        }
        public long getHandledRequestsCount() {
            return handledRequestsCount.get();
        }
        public long getHandledRequestsTime() {
            return handledRequestsTime.get();
        }
        public long getHandledRequestsErrors() {
            return handledRequestsErrors.get();
        }
    }
}
