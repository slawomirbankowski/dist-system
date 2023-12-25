package com.distsystem.agent;

import com.distsystem.DistFactory;
import com.distsystem.agent.services.*;
import com.distsystem.api.*;
import com.distsystem.api.enums.DistComponentType;
import com.distsystem.api.enums.DistMessageType;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.api.info.AgentInfo;
import com.distsystem.api.info.AgentServiceInfo;
import com.distsystem.base.ServiceBase;
import com.distsystem.api.dtos.DistAgentServiceRow;
import com.distsystem.interfaces.*;
import com.distsystem.serializers.ComplexSerializer;
import com.distsystem.utils.DistUtils;
import com.distsystem.utils.DistMessageProcessor;
import com.distsystem.utils.DistWebApiProcessor;
import com.distsystem.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/** agent class to be connected to dist-system applications, Kafka, Elasticsearch or other global agent repository
 * Agent is also connecting directly to other agents */
public class AgentInstance extends ServiceBase implements Agent, DistService, Resolver {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(AgentInstance.class);
    /** start time of agent initialization */
    private final long agentStartTime = System.currentTimeMillis();
    /** total initialization time */
    private long totalInitializationTime = 0L;
    /** configuration for agent */
    private final DistConfig config;
    /** friendly name of current agent */
    private final String agentName;
    /** generate secret of this agent to be able to put commands */
    private final String agentSecret = UUID.randomUUID().toString();
    /** short GUID of agent  */
    private String agentShortGuid;

    /** manager for services registered in agent  */
    private final AgentServices services = new AgentServicesImpl(this);
    /** external configuration reader */
    private final AgentConfigReader agentConfigReader = new AgentConfigImpl(this);
    /** manager for threads in Agent system */
    private final AgentThreads threads = new AgentThreadsImpl(this);
    /** manager for timers in Agent system */
    private final AgentTimers timers = new AgentTimersImpl(this);
    /** manager for registrations */
    private final AgentRegistrations registrations = new AgentRegistrationsImpl(this);
    /** manager for agent connections to other agents */
    private final AgentConnectors connectors = new AgentConnectorsImpl(this);
    /** manager for events from Agent and all services
     *  */
    private final AgentEvents events = new AgentEventsImpl(this);
    /** manager for issues gathered from Agent and all services
     * Issues could be viewed or checked by any external code */
    private final AgentIssues issues = new AgentIssuesImpl(this);
    /** manager for Web API Objects to direct synchronous communication with this agent*/
    private final AgentApi api = new AgentApiImpl(this);
    /** manager for DAO objects */
    private final AgentDaoImpl agentDao = new AgentDaoImpl(this);
    /** authentication service to manage accounts and logins */
    private final AgentAuth auth = new AgentAuthImpl(this);
    /** service to provide message receiver and sender */
    private final Receiver receiver = new AgentReceiverService(this);
    /** cache service */
    private final Cache cache = new AgentCacheImpl(this);
    /** service for reports */
    private final AgentReports reports = new AgentReportsImpl(this);
    /** service for storages */
    private final Storages storages = new AgentStoragesImpl(this);
    /** service for spaces */
    private final AgentSpace space = new AgentSpaceImpl(this);
    /** service for security */
    private final AgentSecurity security = new AgentSecurityImpl(this);
    /** flow service */
    private final AgentFlow flow = new AgentFlowImpl(this);
    /** get semaphores service */
    private final AgentSemaphores semaphores = new AgentSemaphoresImpl(this);
    /** get ML service */
    private final AgentMachineLearning ml = new AgentMachineLearningImpl(this);
    /** get service for managing shared objects */
    private final AgentObjects objects = new AgentObjectsImpl(this);
    /** get service for managing shared objects */
    private final AgentMonitor monitor = new AgentMonitorImpl(this);
    /** get service for managing shared objects */
    private final AgentMeasure measure = new AgentMeasureImpl(this);
    /** get service for managing shared objects */
    private final AgentNotification notification = new AgentNotificationImpl(this);
    /** get service for managing shared objects */
    private final AgentSchedule schedule = new AgentScheduleImpl(this);
    /** memory service to check memory usage of this JVM and Agent */
    private final AgentMemory memory = new AgentMemoryImpl(this);
    /** get service for managing shared objects */
    private final AgentVersion version = new AgentVersionImpl(this);

    /** tags assigned to this Agent, by these tags other Agent can search set of Agent */
    private final Set<String> agentTags = new HashSet<>();
    /** serializer for serialization of DistMessage to external connectors */
    protected DistSerializer serializer;

    /** processor that is connecting message method with current class method to be executed */
    private final DistMessageProcessor messageProcessor = new DistMessageProcessor()
            .addMethod("ping", this::pingMethod)
            .addMethod("getRegistrationKeys", this::getRegistrationKeys);

    /** create new agent - instance with all components and services */
    public AgentInstance(DistConfig config, Map<String, Function<AgentEvent, String>> callbacksMethods, HashMap<String, DistSerializer> serializers,
                         CachePolicy policy, Map<String, DaoParams> daos) {
        super(null);
        this.parentAgent = this;
        log.info("CREATING NEW AGENT with guid: " + getAgentGuid() + ", configuration: " + config.getConfigGuid() + ", host: " + DistUtils.getCurrentHostName());
        this.config = config;
        this.agentName = config.getProperty(DistConfig.AGENT_NAME, DistConfig.AGENT_UNIVERSE_NAME_DEFAULT);
        agentTags.addAll(Arrays.stream(config.getProperty(DistConfig.AGENT_TAGS, "").split(";")).filter(tag -> !tag.isEmpty()).collect(Collectors.toList()));
        // set policy to Cache
        cache.setPolicy(policy);
        // self register of agent as service
        services.registerService(this);
        // add default callbacks
        events.addCallbackMethods(callbacksMethods);
        // set default complex serializer
        serializer = ComplexSerializer.createSerializer(serializers);
        // make agent resolver to resolve values
        config.getResolverManager().addResolver(this);
        // create default DAOs
        agentDao.createDaos(daos);
        log.info("CREATING NEW AGENT with guid: " + getAgentGuid() + " FINISHED");
        touch("AgentInstance");
    }

    /** count objects in this agentable object including this object */
    public long countObjectsService() {
        return 6L + config.countObjects() + agentTags.size() + serializer.countObjects();
    }
    /** count all objects in agent - all services objects */
    public long countAgentObjects() {
        return services.getServices().stream().mapToLong(x -> x.countObjects()).sum();
    }
    /** read configuration and re-initialize this component */
    protected boolean onReinitialize() {
        // nothing to be done here
        return true;
    }
    /** get type of service: cache, measure, report, flow, space, ... */
    public DistServiceType getServiceType() {
        return DistServiceType.agent;
    }
    /** get description of this service */
    public String getServiceDescription() {
        return "Agent that is binding all services and components";
    }
    /** create new service UID for this service */
    protected String createGuid() {
        agentShortGuid = DistUtils.generateShortGuid();
        return DistUtils.generateAgentGuid(agentShortGuid);
    }
    /** get type of this component */
    public DistComponentType getComponentType() {
        return DistComponentType.agent;
    }
    /** get row for registration services */
    public DistAgentServiceRow getServiceRow() {
        return new DistAgentServiceRow(getAgentGuid(), getAgentGuid(), getServiceType().name(), JsonUtils.serialize(getServiceInfo()), createDate, (closed)?0:1, LocalDateTime.now());
    }
    /** get distributed group name */
    public String getDistGroup() {
        return getConfig().getProperty(DistConfig.AGENT_GROUP, DistConfig.AGENT_GROUP_VALUE_DEFAULT);
    }
    /** get distributed system name */
    public String getDistName() {
        return getConfig().getProperty(DistConfig.AGENT_UNIVERSE, DistConfig.AGENT_UNIVERSE_NAME_DEFAULT);
    }
    /** get current agent name */
    public String getAgentName() {
        return agentName;
    }
    /** get type of current environment */
    public String getEnvironmentType() {
        return getConfig().getProperty(DistConfig.AGENT_ENVIRONMENT_TYPE, DistConfig.AGENT_ENVIRONMENT_TYPE_VALUE_DEFAULT);
    }
    /** get name of current environment */
    public String getEnvironmentName() {
        return getConfig().getProperty(DistConfig.AGENT_ENVIRONMENT_NAME, DistConfig.AGENT_ENVIRONMENT_NAME_VALUE_DEFAULT);
    }
    /** additional web API endpoints */
    protected DistWebApiProcessor additionalWebApiProcessor() {
        return new DistWebApiProcessor(getServiceType())
                .addHandlerGet("", (m, req) -> req.responseOkText(welcomeMessage()))
                .addHandlerGet("total-initialization-time", (m, req) -> req.responseOkText(""+totalInitializationTime))
                .addHandlerGet("agent-start-time", (m, req) -> req.responseOkText(""+agentStartTime))
                .addHandlerGet("agent-name", (m, req) -> req.responseOkText(agentName))
                .addHandlerGet("agent-short-guid", (m, req) -> req.responseOkText(""+agentShortGuid))
                .addHandlerGet("services", (m, req) -> req.responseOkText(services.getServiceDescriptions()))
                .addHandlerGet("endpoints", (m, req) -> req.responseOkJsonSerialize(listAllEndpoints()))
                .addHandlerGet("endpoints-text", (m, req) -> req.responseOkText(listAllEndpoints().stream().collect(Collectors.joining("\n"))))
                .addHandlerGet("info", createJsonHandler(param -> getAgentInfo()))
                .addHandlerGet("kill", createJsonHandler(param -> kill()))
                .addHandlerGet("name", (m, req) -> req.responseOkText(getAgentName()))
                .addHandlerGet("tags", (m, req) -> req.responseOkJsonSerialize(getAgentTags()))
                .addHandlerGet("serializer", (m, req) -> req.responseOkJsonSerialize(serializer.getInfo()))
                .addHandlerPost("message", (m, req) -> req.responseOkJsonSerialize(sendMessageAsJson(req.getContentAsString())))
                .addHandlerPost("initialize-again", (m, req) -> req.responseOkJsonSerialize(initializeAgentWithInfo()))
                .addHandlerGet("guid", (m, req) -> req.responseOkText( getAgentGuid()));
    }

    /** get custom map of info about service */
    public Map<String, String> getServiceInfoCustomMap() {
        return Map.of();
    }
    /** returns unmodificable set of Agent tags */
    public Set<String> getAgentTags() {
        return Collections.unmodifiableSet(agentTags);
    }
    /** initialize agent - server, application, jdbc, kafka */
    public void initializeAgent() {
        createEvent("initializeAgent");
        log.info("Initializing agent for guid: " + guid + ", services: " + services.getServicesCount() + ", configurations: " + config.getPropertiesCount() + ", ");
        services.reinitializeAllServices();
        totalInitializationTime = System.currentTimeMillis() - agentStartTime;
        touch("initializeAgent");
    }
    /** initialize again this agent and get info */
    public AgentInfo initializeAgentWithInfo() {
        initializeAgent();
        return getAgentInfo();
    }
    /** list all endpoints for all services */
    public String welcomeMessage() {
        try {
            String txt = new String(this.getClass().getResourceAsStream("/welcome.html").readAllBytes());
            return config.getResolverManager().resolve(txt);
        } catch (Exception ex) {
            log.info("Cannot read resource as stream, reason: " + ex.getMessage(), ex);
            return "";
        }
    }
    /** list all endpoints for all services */
    public List<String> listAllEndpoints() {
        return getCache().withCache("AgentInstance.listAllEndpoints", x ->
            services.getServices().stream().flatMap(s -> s.getWebApiProcessor().getAllHandlers().stream()).sorted().toList()
        );
    }
    /** get total initialization agent time in milliseconds */
    public long getTotalInitializationTime() {
        return totalInitializationTime;
    }
    /** update configuration of this Service to add registrations, services, servers, ... */
    public void updateConfig(DistConfig newCfg) {
        // TODO: update configuration of this service
    }
    /** check if Agent configuration has given property by name */
    public boolean hasConfigProperty(String propName) {
        return getConfig().hasProperty(propName);
    }
    /** add issue with method and exception */
    public void addIssue(String methodName, Exception ex) {
        issues.addIssue(new DistIssue(this, methodName, ex));
    }
    /** get this Agent */
    public Agent getAgent() {
        return this;
    }
    /** get configuration for this agent */
    public DistConfig getConfig() { return config; }
    /** get unique ID of this agent */
    public String getAgentGuid() { return guid; }
    /** get short version ID of this agent GUID */
    public String getAgentShortGuid() { return agentShortGuid; }

    /** get date and time of creating this agent */
    public LocalDateTime getCreateDate() {
        return createDate;
    }

    /** get secret generated or set for this agent */
    public String getAgentSecret() {
        return agentSecret;
    }

    /** get authentication service for managing accounts and login into identity services */
    public AgentAuth getAuth() {
        return auth;
    }
    /** get receiver service to get data from different sources */
    public Receiver getReceiver() {
        return receiver;
    }
    /** get cache connected with this Agent to have copy of */
    public Cache getCache() {
        return cache;
    }
    /** get service for reports to create, update, remove or execute reports */
    public AgentReports getReports() {
        return reports;
    }
    /** get service for storages */
    public Storages getStorages() {
        return storages;
    }
    /** get service for managing spaces - shared spaces with objects that could be modified by any agent */
    public AgentSpace getSpace() {
        return space;
    }
    /** get service for security for managing priviliges, roles, objects */
    public AgentSecurity getSecurity() {
        return security;
    }
    /** get flow service for data flows */
    public AgentFlow getFlow() {
        return flow;
    }
    /** get semaphores service for semaphores and mutexes */
    public AgentSemaphores getSemaphores() {
        return semaphores;
    }
    /** get ML service for Machine Learning models */
    public AgentMachineLearning getMl() {
        return ml;
    }
    /** get service for managing shared objects */

    public AgentObjects getObjects() {
        return objects;
    }
    /** get memory service */
    public AgentMemory getMemory() { return memory; }
    /** get start time of this agent -  System.currentTimeMillis() */
    public long getAgentStartTime() {
        return agentStartTime;
    }
    /** get time in milliseconds of working for this Agent */
    public long getAgentWorkingTime() {
        return System.currentTimeMillis()-agentStartTime;
    }
    /** get monitor service */
    public AgentMonitor getMonitor() {
        return monitor;
    }
    /** get measure service */
    public AgentMeasure getMeasure() {
        return measure;
    }
    /** get notification service to send notifications */
    public AgentNotification getNotification() {
        return notification;
    }
    /** get schedule for service */
    public AgentSchedule getSchedule() {
        return schedule;
    }
    /** get service for version */
    public AgentVersion getVersion() {
        return version;
    }
    /** get serializer/deserializer helper to serialize/deserialize objects when sending through connectors or saving to external storages */
    public DistSerializer getSerializer() {
        return serializer;
    }
    /** get high-level information about this agent */
    public AgentInfo getAgentInfo() {
        return new AgentInfo(guid, getDistName(), getAgentName(), createDate, closed,
                getAgentTags(),
                componentList.stream().map(c -> c.getComponentType().name()).toList(),
                agentConfigReader.getInfo(),
                messageProcessor.getInfo(),
                services.getServiceInfos(),
                config.getConfigGroupInfos());
    }

    /** kill this agent - close it and mark is killed */
    public AgentKillStatus kill() {
        close();
        return new AgentKillStatus(guid, createDate, LocalDateTime.now());
    }
    /** get component to read configuration from external sources */
    public AgentConfigReader getConfigReader() {
        return agentConfigReader;
    }
    /** get agent threads manager */
    public AgentThreads getThreads() {
        return threads;
    }
    /** get agent timers manager */
    public AgentTimers getTimers() {
        return timers;
    }
    /** get agent service manager */
    public AgentServices getServices() {
        return services;
    }
    /** get agent connector manager to manage direct connections to other agents, including sending and receiving messages */
    public AgentConnectors getConnectors() {
        return connectors;
    }
    /** get agent registration manager to register this agent in global repositories (different types: JDBC, Kafka, App, Elasticsearch, ... */
    public AgentRegistrations getRegistrations() {
        return registrations;
    }
    /** get agent events manager to add events and set callbacks */
    public AgentEvents getEvents() {
        return events;
    }
    /** get agent issue manager for adding issues */
    public AgentIssues getIssues() {
        return issues;
    }
    /** get agent DAOs manager for external connections to JDBC, Elasticsearch, Kafka, Redis */
    public AgentDao getAgentDao() {
        return agentDao;
    }
    /** get WebAPI for this Agent */
    public AgentApi getApi() {
        return api;
    }
    /** add new event to events */
    public void addEvent(AgentEvent event) {
        events.addEvent(event);
    }
    /** get detailed info about this service */
    public Map<String, Object> getInfo() {
        return Map.of("agentStartTime", ""+agentStartTime,
                "totalInitializationTime", ""+totalInitializationTime,
                "agentName", agentName,
                "agentShortGuid", agentShortGuid,
                "serializer", serializer.getInfo(),
                "countObjects",""+countObjectsService(),
                "distGroup", getDistGroup(),
                "distName ", getDistName(),
                "environmentType", getEnvironmentType()
        );
    }
    /** close all items in this agent */
    public void onClose() {
        log.info("Closing agent: " + guid);
        closed = true;
        log.info("Agent is trying to close APIs, agent: " + guid);
        api.close();
        log.info("Agent is trying to close threads, agent: " + guid + ", threadsCount: " + threads.getThreadsCount());
        threads.close();
        log.info("Agent is trying to close timers, agent: " + guid + ", TimerTasksCount: " + timers.getTimerTasksCount());
        timers.close();
        log.info("Agent is trying to close services, agent: " + guid + ", ServicesCount: " + services.getServicesCount());
        services.close();
        log.info("Agent is trying to close connectors, agent: " + guid + ", ServersCount: " + connectors.getServersCount() + ", ClientsCount: " + connectors.getClientsCount());
        connectors.close();
        log.info("Agent is trying to close registrations, agent: " + guid + ", RegistrationsCount" + registrations.getRegistrationsCount() + ", AgentsCount: " + registrations.getAgentsCount());
        registrations.close();
        log.info("Agent is trying to close events, agent: " + guid + ", EventsCount: " + events.getEvents().size());
        events.close();
        log.info("Agent is trying to close issues, agent: " + guid + ", IssuesCount: " + issues.getIssues().size());
        issues.close();
        log.info("Agent is trying to close DAOs, agent: " + guid + ", DaosCount: " + agentDao.getDaosCount());
        agentDao.close();
        log.info("Agent is trying to close message processors, agent: " + guid + ", ErrorMessagesCount: " + messageProcessor.getErrorMessagesCount());
        messageProcessor.close();
        log.info("Agent is trying to close ConfigReader, agent: " + guid + ", ReadersCount: " + agentConfigReader.getReadersCount());
        agentConfigReader.close();
        log.info("Agent is fully closed!, agent: " + guid);
    }

    /** parse message from JSON body, process that message and respond as Map  */
    protected Map<String, String> sendMessageAsJson(String messageBodyJson) {
        touch("sendMessageAsJson");

        // TODO: implement parsing message from JSON into DistMessage and run processMessage()
        //DistMessage.createMessage();
        return Map.of();
    }

    /** create new message builder starting this agent */
    public DistMessageBuilder createMessageBuilder() {
        return DistMessageBuilder.empty().fromAgent(this);
    }

    /** message send to agent(s) */
    public DistMessageFull sendMessage(DistMessageFull msg) {
        touch("sendMessage");
        log.info("SENDING MESSAGE " + msg.getMessage());
        getConnectors().sendMessage(msg);
        return msg;
    }
    /** send message to agents */
    public DistMessageFull sendMessage(DistMessage msg, DistCallbacks callbacks) {
        return sendMessage(msg.withCallbacks(callbacks));
    }
    /** create broadcast message send to all clients */
    public DistMessageFull sendMessageBroadcast(DistMessageType messageType, DistServiceType fromService,
                                                DistServiceType toService, String requestMethod, Object message, String tags, LocalDateTime validTill, DistCallbacks callbacks) {
        // DistMessageType messageType, String fromAgent, DistServiceType fromService, String toAgent, DistServiceType toService, String method, Object message,  String tags, LocalDateTime validTill
        DistMessage msg = DistMessage.createMessage(messageType, guid, fromService, "*", toService, requestMethod, message, tags, validTill);
        return sendMessage(msg.withCallbacks(callbacks));
    }
    public DistMessageFull sendMessageBroadcast(DistMessageType messageType, DistServiceType fromService,
                                                DistServiceType toService, String requestMethod, Object message, String tags, DistCallbacks callbacks) {
        DistMessage msg = DistMessage.createMessage(messageType, guid, fromService, "*", toService, requestMethod, message, tags, LocalDateTime.MAX);
        return sendMessage(msg.withCallbacks(callbacks));
    }
    public DistMessageFull sendMessageBroadcast(DistMessageType messageType, DistServiceType fromService,
                                                DistServiceType toService, String requestMethod, Object message, DistCallbacks callbacks) {
        DistMessage msg = DistMessage.createMessage(messageType, guid, fromService, "*", toService, requestMethod, message, "", LocalDateTime.MAX);
        return sendMessage(msg.withCallbacks(callbacks));
    }
    public DistMessageFull sendMessageBroadcast(DistServiceType fromService, DistServiceType toService, String requestMethod, Object message, DistCallbacks callbacks) {
        DistMessage msg = DistMessage.createMessage(DistMessageType.request, guid, fromService, "*", toService, requestMethod, message, "", LocalDateTime.MAX);
        DistMessageFull full = msg.withCallbacks(callbacks);
        return sendMessage(full);
    }
    public DistMessageFull sendMessage(DistService fromService, String toAgent, DistServiceType toService, String method, Object message,
                                   DistCallbacks callbacks) {
        DistMessage msg = DistMessage.createMessage(DistMessageType.request, guid, fromService.getServiceType(), toAgent, toService, method, message, "", LocalDateTime.MAX);
        return sendMessage(msg.withCallbacks(callbacks));
    }
    public DistMessageFull sendMessageAny(DistService fromService, DistServiceType toService, String method, Object message,
                                      DistCallbacks callbacks) {
        DistMessage msg = DistMessage.createMessage(DistMessageType.request, guid, fromService.getServiceType(), "?", toService, method, message, "", LocalDateTime.MAX);
        return sendMessage(msg.withCallbacks(callbacks));
    }


    /** ping this agent, return is pong */
    private DistMessage pingMethod(String methodName, DistMessage msg) {
        touch("pingMethod");
        log.info("METHOD PING from agent: " + msg.getFromAgent());
        if (msg.isTypeRequest()) {
            return msg.pong(getAgentGuid());
        } else {

            return msg.pong(getAgentGuid());
        }
    }
    /** register new config group in agent configuration */
    public DistConfigGroup registerConfigGroup(String groupName, DistService parentService) {
        touch("registerConfigGroup");
        return config.registerConfigGroup(groupName, parentService);
    }
    /** method to get registration keys for this agent */
    private DistMessage getRegistrationKeys(String methodName, DistMessage msg) {
        getRegistrations().getRegistrationKeys();
        // TODO: create response message with registration keys
        return msg.pong(getAgentGuid());
    }

    /** set this Agent instance as default one for current JVM */
    public Agent setAsDefaultAgent() {
        touch("setAsDefaultAgent");
        log.info("Set current agent as default in DistFactory, agent GUID: " + getAgentGuid());
        DistFactory.setDefaultAgent(this);
        return this;
    }
    /** wait in this thread till agent would be killed by external command */
    public void waitTillKill() {
        try {
            log.info("Agent will be waiting in this thread till closed, GUID: " + getAgentGuid() + ", workingTimeMs: " + getCurrentWorkingTimeMs());
            while (!closed) {
                log.trace("Waiting for agent to be killed.");
                Thread.sleep(5000L);
            }
            log.info("Agent has been closed with success, GUID: " + getAgentGuid() + ", workingTimeMs: " + getCurrentWorkingTimeMs());
        } catch (InterruptedException ex) {
            log.info("Agent will be waiting in this thread till closed, GUID: " + getAgentGuid() + ", workingTimeMs: " + getCurrentWorkingTimeMs());
        }
    }
    /** get single value for a key */
    public Optional<String> getValue(String key) {
        log.info("Resolve using agent, key: " + key);

        return Optional.of(webApiProcessor.handleSimpleRequest(key).getContent());
    }
    /** get all known keys */
    public List<String> getKnownKeys() {
        return webApiProcessor.getAllHandlers();
    }

}
