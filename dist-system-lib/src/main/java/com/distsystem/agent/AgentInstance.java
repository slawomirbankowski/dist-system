package com.distsystem.agent;

import com.distsystem.DistFactory;
import com.distsystem.agent.impl.*;
import com.distsystem.api.*;
import com.distsystem.api.enums.DistComponentType;
import com.distsystem.api.enums.DistMessageType;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.api.info.AgentInfo;
import com.distsystem.base.ServiceBase;
import com.distsystem.base.dtos.DistAgentServiceRow;
import com.distsystem.interfaces.*;
import com.distsystem.serializers.ComplexSerializer;
import com.distsystem.utils.DistUtils;
import com.distsystem.utils.DistMessageProcessor;
import com.distsystem.utils.DistWebApiProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/** agent class to be connected to dist-system applications, Kafka, Elasticsearch or other global agent repository
 * Agent is also connecting directly to other agents */
public class AgentInstance extends ServiceBase implements Agent, DistService {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(AgentInstance.class);
    /** date ant time of creation */
    private final LocalDateTime createDate = LocalDateTime.now();
    /** configuration for agent */
    private final DistConfig config;
    /** friendly name of current agent */
    private final String agentName;
    /** generate secret of this agent to be able to put commands */
    private final String agentSecret = UUID.randomUUID().toString();
    /** short GUID of agent  */
    private String agentShortGuid;

    /** manager for services registered in agent  */
    private final AgentServices agentServices = new AgentServicesImpl(this);

    /** external configuration reader */
    private final AgentConfigReader agentConfigReader = new AgentConfigReaderImpl(this);
    /** manager for threads in Agent system */
    private final AgentThreads agentThreads = new AgentThreadsImpl(this);
    /** manager for timers in Agent system */
    private final AgentTimers agentTimers = new AgentTimersImpl(this);
    /** manager for registrations */
    private final AgentRegistrations agentRegistrations = new AgentRegistrationsImpl(this);
    /** manager for agent connections to other agents */
    private final AgentConnectors agentConnectors = new AgentConnectorsImpl(this);
    /** manager for events from Agent and all services
     *  */
    private final AgentEvents agentEvents = new AgentEventsImpl(this);
    /** manager for issues gathered from Agent and all services
     * Issues could be viewed or checked by any external code */
    private final AgentIssues agentIssues = new AgentIssuesImpl(this);
    /** manager for Web API Objects to direct synchronous communication with this agent*/
    private final AgentApi agentApi = new AgentApiImpl(this);
    /** manager for DAO objects */
    private final AgentDaoImpl agentDao = new AgentDaoImpl(this);
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
        agentConfigReader.reinitialize();
        agentServices.setPolicy(policy);
        // self register of agent as service
        agentServices.registerService(this);
        agentEvents.addCallbackMethods(callbacksMethods);
        agentApi.getApisCount();
        serializer = ComplexSerializer.createSerializer(serializers);
        agentDao.createDaos(daos);
    }

    /** read configuration and re-initialize this component */
    public boolean reinitialize() {
        agentServices.reinitializeAllServices();
        return true;
    }
    /** get type of service: cache, measure, report, flow, space, ... */
    public DistServiceType getServiceType() {
        return DistServiceType.agent;
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

    /** get basic information about service */
    public DistServiceInfo getServiceInfo() {
        // DistServiceType serviceType, String getServiceClass, String serviceGuid, LocalDateTime createdDateTime, boolean closed, Map<String, String> customAttributes
        return new DistServiceInfo(getServiceType(), getClass().getName(), getGuid(), createDate, closed, getServiceInfoCustomMap());
    }
    /** get row for registration services */
    public DistAgentServiceRow getServiceRow() {
        return new DistAgentServiceRow(getAgentGuid(), getAgentGuid(), getServiceType().name(), createDate, (closed)?0:1, LocalDateTime.now());
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
                .addHandlerGet("ping", createTextHandler(param -> "pong"))
                .addHandlerGet("createdDate", (m, req) -> req.responseOkText( getCreateDate().toString()))
                .addHandlerGet("info", createJsonHandler(param -> getAgentInfo()))
                .addHandlerGet("kill", createJsonHandler(param -> kill()))
                .addHandlerGet("config", createJsonHandler(param -> getConfig().getHashMap(false)))
                .addHandlerGet("threads", createJsonHandler(param -> agentThreads.getThreadsInfo()))

                .addHandlerGet("timers", createJsonHandler(param -> agentTimers.getInfo()))

                .addHandlerGet("registrations", createJsonHandler(param -> agentRegistrations.getRegistrationKeys()))
                .addHandlerGet("registration-connected-agents", createJsonHandler(param -> agentRegistrations.getAgents()))
                .addHandlerGet("registered-servers", createJsonHandler(param -> agentRegistrations.getServers()))
                .addHandlerGet("registration-infos", createJsonHandler(param -> agentRegistrations.getRegistrationInfos()))

                .addHandlerGet("service-keys", createJsonHandler(param -> agentServices.getServiceKeys()))
                .addHandlerGet("service-types", createJsonHandler(param -> agentServices.getServiceTypes()))
                .addHandlerGet("services", createJsonHandler(param -> agentServices.getServiceInfos()))
                .addHandlerGet("service", createJsonHandler(param -> agentServices.getServiceInfo(param)))
                .addHandlerPost("service-init-all", createJsonHandler(param -> agentServices.initializeAllPossible()))

                .addHandlerGet("server-keys", createJsonHandler(param -> agentConnectors.getServerKeys()))
                .addHandlerGet("client-keys", createJsonHandler(param -> agentConnectors.getClientKeys()))

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
        log.info("Initializing agent for guid: " + guid);
        // TODO: check if all items are initialized
        agentRegistrations.reinitialize();
        agentConnectors.reinitialize();
        agentApi.openApis();
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
        agentIssues.addIssue(new DistIssue(this, methodName, ex));
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
                agentApi.getInfo(),
                agentConnectors.getInfo(),
                agentServices.getServiceInfos(),
                agentRegistrations.getInfo(),
                agentTimers.getInfo(),
                agentThreads.getThreadsInfo(),
                agentDao.getInfo(),
                getAgentEvents().getEvents().size(),
                getAgentIssues().getIssues().size());
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
    public AgentThreads getAgentThreads() {
        return agentThreads;
    }
    /** get agent timers manager */
    public AgentTimers getAgentTimers() {
        return agentTimers;
    }
    /** get agent service manager */
    public AgentServices getAgentServices() {
        return agentServices;
    }
    /** get agent connector manager to manage direct connections to other agents, including sending and receiving messages */
    public AgentConnectors getAgentConnectors() {
        return agentConnectors;
    }
    /** get agent registration manager to register this agent in global repositories (different types: JDBC, Kafka, App, Elasticsearch, ... */
    public AgentRegistrations getAgentRegistrations() {
        return agentRegistrations;
    }
    /** get agent events manager to add events and set callbacks */
    public AgentEvents getAgentEvents() {
        return agentEvents;
    }
    /** get agent issue manager for adding issues */
    public AgentIssues getAgentIssues() {
        return agentIssues;
    }
    /** get agent DAOs manager for external connections to JDBC, Elasticsearch, Kafka, Redis */
    public AgentDao getAgentDao() {
        return agentDao;
    }
    /** get WebAPI for this Agent */
    public AgentApi getAgentApi() {
        return agentApi;
    }

    /** close all items in this agent */
    public void onClose() {
        log.info("Closing agent: " + guid);
        closed = true;
        log.info("Agent is trying to close APIs, agent: " + guid);
        agentApi.close();
        log.info("Agent is trying to close threads, agent: " + guid + ", threadsCount: " + agentThreads.getThreadsCount());
        agentThreads.close();
        log.info("Agent is trying to close timers, agent: " + guid + ", TimerTasksCount: " + agentTimers.getTimerTasksCount());
        agentTimers.close();
        log.info("Agent is trying to close services, agent: " + guid + ", ServicesCount: " + agentServices.getServicesCount());
        agentServices.close();
        log.info("Agent is trying to close connectors, agent: " + guid + ", ServersCount: " + agentConnectors.getServersCount() + ", ClientsCount: " + agentConnectors.getClientsCount());
        agentConnectors.close();
        log.info("Agent is trying to close registrations, agent: " + guid + ", RegistrationsCount" + agentRegistrations.getRegistrationsCount() + ", AgentsCount: " + agentRegistrations.getAgentsCount());
        agentRegistrations.close();
        log.info("Agent is trying to close events, agent: " + guid + ", EventsCount: " + agentEvents.getEvents().size());
        agentEvents.close();
        log.info("Agent is trying to close issues, agent: " + guid + ", IssuesCount: " + agentIssues.getIssues().size());
        agentIssues.close();
        log.info("Agent is trying to close DAOs, agent: " + guid + ", DaosCount: " + agentDao.getDaosCount());
        agentDao.close();
        log.info("Agent is trying to close message processors, agent: " + guid + ", ErrorMessagesCount: " + messageProcessor.getErrorMessagesCount());
        messageProcessor.close();
        log.info("Agent is trying to close ConfigReader, agent: " + guid + ", ReadersCount: " + agentConfigReader.getReadersCount());
        agentConfigReader.close();
        log.info("Agent is fully closed!, agent: " + guid);
    }

    /** process message by this agent service, choose method and , returns status */
    public DistMessage processMessage(DistMessage msg) {
        log.info("Process message by AgentInstance, message: " + msg);
        return messageProcessor.process(msg.getMethod(), msg);
    }

    /** create new message builder starting this agent */
    public DistMessageBuilder createMessageBuilder() {
        return DistMessageBuilder.empty().fromAgent(this);
    }

    /** message send to agent(s) */
    public DistMessageFull sendMessage(DistMessageFull msg) {
        log.info("SENDING MESSAGE " + msg.getMessage());
        getAgentConnectors().sendMessage(msg);
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
        log.info("METHOD PING from agent: " + msg.getFromAgent());
        if (msg.isTypeRequest()) {
            return msg.pong(getAgentGuid());
        } else {

            return msg.pong(getAgentGuid());
        }
    }
    /** register new config group */
    public DistConfigGroup registerConfigGroup(String groupName) {
        return config.registerConfigGroup(groupName);
    }
    /** method to get registration keys for this agent */
    private DistMessage getRegistrationKeys(String methodName, DistMessage msg) {
        getAgentRegistrations().getRegistrationKeys();
        // TODO: create response message with registration keys
        return msg.pong(getAgentGuid());
    }

    /** set this Agent instance as default one for current JVM */
    public Agent setAsDefaultAgent() {
        log.info("Set current agent as default in DistFactory, agent GUID: " + getAgentGuid());
        DistFactory.setDefaultAgent(this);
        return this;
    }
    /** wait in this thread till agent would be killed by external command */
    public void waitTillKill() {
        // TODO: implement waiting here in this thread till agent is killed by external command
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

}
