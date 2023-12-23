package com.distsystem.agent.services;

import com.distsystem.agent.clients.AgentKafkaClient;
import com.distsystem.api.enums.*;
import com.distsystem.api.info.AgentConnectorsInfo;
import com.distsystem.agent.clients.DatagramClient;
import com.distsystem.agent.clients.HttpClient;
import com.distsystem.agent.clients.SocketServerClient;
import com.distsystem.api.*;
import com.distsystem.api.info.AgentServerInfo;
import com.distsystem.api.info.ClientInfo;
import com.distsystem.base.ServerBase;
import com.distsystem.base.ServiceBase;
import com.distsystem.api.dtos.DistAgentRegisterRow;
import com.distsystem.api.dtos.DistAgentServerRow;
import com.distsystem.interfaces.*;
import com.distsystem.utils.DistUtils;
import com.distsystem.utils.DistMapTimeStorage;
import com.distsystem.utils.DistWebApiProcessor;
import com.distsystem.utils.HashMapMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/** service for connections inside agent - servers and clients
 * using servers and clients two agent can create direct connection to much faster communication */
public class AgentConnectorsImpl extends ServiceBase implements AgentConnectors {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(AgentConnectorsImpl.class);
    /** all servers for connections to other agents */
    private final java.util.concurrent.ConcurrentHashMap<String, AgentServer> servers = new java.util.concurrent.ConcurrentHashMap<>();
    /** all servers for connections to other agents */
    private final java.util.concurrent.ConcurrentHashMap<String, DistAgentServerRow> agentServers = new java.util.concurrent.ConcurrentHashMap<>();
    /** map of map of clients connected to different agents
     * key1 = agentGUID
     * key2 = serverGUID
     * value = client to transfer messages to this agent */
    private final HashMapMap<String, String, AgentClient> clients = new HashMapMap<>();
    /** map of local connectors as part of local clients
     * key=clientGUID
     * value= client
     * */
    private final java.util.concurrent.ConcurrentHashMap<String, AgentClient> localConnectors = new java.util.concurrent.ConcurrentHashMap<>();
    /** queue with clients */
    private final java.util.concurrent.ConcurrentLinkedQueue<AgentClient> clientQueue = new ConcurrentLinkedQueue<>();
    /** table with all clients  */
    private final java.util.ArrayList<AgentClient> clientTable = new ArrayList<>();
    /** messages already sent with callbacks */
    private final DistMapTimeStorage<DistMessageFull> sentMessages = new DistMapTimeStorage();
    /** opened server counter */
    private final AtomicLong openedServersCount = new AtomicLong();

    /** create new connectors */
    public AgentConnectorsImpl(Agent parentAgent) {
        super(parentAgent);
        parentAgent.getServices().registerService(this);
    }
    /** get type of service: cache, measure, report, flow, space, ... */
    public DistServiceType getServiceType() {
        return DistServiceType.connectors;
    }

    /** count objects in this agentable object including this object */
    public long countObjectsService() {
        // TODO: calculate counts of objects
        return 2L;
    }

    /** update configuration of this Service to add registrations, services, servers, ... */
    public void updateConfig(DistConfig newCfg) {
    }
    /** change values in configuration bucket */
    public void initializeConfigBucket(DistConfigBucket bucket) {
        // TODO: insert, update, delete of bucket
        addNewServer(bucket);
    }
    /** run after initialization */
    public void afterInitialization() {
        createEvent("afterInitialization");
        log.info("Set up timer to check servers and clients for agent: " + getParentAgentGuid() +", servers count: " + servers.size());
        parentAgent.getTimers().cancelTimer("TIMER_SERVER_CLIENT");
        parentAgent.getTimers().setUpTimer("TIMER_SERVER_CLIENT", DistConfig.AGENT_CACHE_TIMER_SERVER_CLIENT_PERIOD, DistConfig.TIMER_SERVER_CLIENT_PERIOD_DELAY_VALUE, x -> onTimeServersCheck());
    }
    /** additional web API endpoints */
    protected DistWebApiProcessor additionalWebApiProcessor() {
        return new DistWebApiProcessor(getServiceType())
                .addHandlerGet("info", (m, req) -> req.responseOkJsonSerialize(getInfo()))
                .addHandlerGet("client-keys", (m, req) -> req.responseOkJsonSerialize(getClientKeys()))
                .addHandlerGet("clients-local", (m, req) -> req.responseOkJsonSerialize(localConnectors.values().stream().map(c -> c.getClientInfo()).toList()))
                .addHandlerGet("servers", (m, req) -> req.responseOkJsonSerialize(servers.values().stream().map(s -> s.getInfo()).toList()))
                .addHandlerGet("server-rows", (m, req) -> req.responseOkJsonSerialize(agentServers.values().stream().toList()))
                .addHandlerGet("clients-table", (m, req) -> req.responseOkJsonSerialize(clientTable.stream().map(c -> c.getClientInfo()).toList()))
                .addHandlerGet("client-infos", (m, req) -> req.responseOkJsonSerialize(getClientTableInfos()))
                .addHandlerPost("servers-check", (m, req) -> req.responseOkJsonSerialize(serversCheckWithInfo()))
                .addHandlerGet("server-keys", (m, req) -> req.responseOkJsonSerialize(getServerKeys()));
    }
    /** */
    public List<ClientInfo> getClientTableInfos() {
        return clientTable.stream().map(c -> c.getClientInfo()).toList();
    }
    /** read configuration and re-initialize this component */
    protected boolean onReinitialize() {
        registerConfigGroup(DistConfig.AGENT_CONNECTORS_SERVER);
        // TODO: implement reinitialization
        // ...
        return true;
    }
    /** get full information about connectors - servers, clients */
    public AgentConnectorsInfo getInfo() {
        List<AgentServerInfo> createdServers = servers.values().stream().map(s -> s.getInfo()).collect(Collectors.toList());
        List<DistAgentServerRow> serverDefinitions = agentServers.values().stream().map(as -> as.copyNoPassword()).collect(Collectors.toList());
        List<ClientInfo> clientInfos = clients.getAllValues().stream().map(c -> c.getClientInfo()).collect(Collectors.toList());
        return new AgentConnectorsInfo(createdServers, serverDefinitions, clientInfos);
    }
    /** add new server - put in map of servers and register server to registration services */
    private void addNewServer(DistConfigBucket bucket) {
        String readerClass = DistConfig.AGENT_CONNECTORS_CLASS_MAP.get(bucket.getKey().getConfigType());
        createEvent("addNewServer");
        try {
            openCount.incrementAndGet();
            log.info("Try to initialize external server for agent: " + parentAgent.getAgentGuid() + ", class: " + readerClass + ", bucket key: " + bucket.getKey());
            ServerBase serv = (ServerBase)Class.forName(readerClass)
                    .getConstructor(ServiceObjectParams.class)
                    .newInstance(ServiceObjectParams.create(parentAgent, this, readerClass, bucket));
            openedServersCount.incrementAndGet();
            servers.put(serv.getServerGuid(), serv);
            parentAgent.getRegistrations().registerServer(serv.createServerRow());
        } catch (Exception ex) {
            log.info("Cannot initialize external server for agent: "  + parentAgent.getAgentGuid() + ", class: " + readerClass + ", reason: " + ex.getMessage(), ex);
            addIssueToAgent("addNewServer", ex);
        }
    }

    /** run by agent every X seconds to check servers and clients */
    public boolean onTimeServersCheck() {
        try {
            createEvent("onTimeServersCheck");
            checkCount.incrementAndGet();
            List<DistAgentServerRow> servers = getAgent().getRegistrations().getServers();
            List<DistAgentRegisterRow> connectedAgents = getAgent().getRegistrations().getAgents();
            log.info("Check servers for agent: " + parentAgent.getAgentGuid() + ", connectedAgents: " + connectedAgents.size() + ", servers from registrations: " + servers.size());
            checkActiveServers(servers);
            // TODO: connect to all nearby agents, check statuses
            // TODO: implement communication of agent with other cache agents
            log.info("AGENT REGISTRATION summary for guid: " + parentAgent.getAgentGuid() + ", registrations: " + getAgent().getRegistrations().getRegistrationsCount() + ", connected agents: " + connectedAgents.size() + ", registeredServers: " + servers.size());
            return true;
        } catch (Exception ex) {
            log.warn("Cannot communicate with other agents, reason: " + ex.getMessage(), ex);
            addIssueToAgent("AgentConnectorsImpl.onTimeServersCheck", ex);
            return false;
        }
    }

    /** check servers and returns info */
    public AgentConnectorsInfo serversCheckWithInfo() {
        onTimeServersCheck();
        return getInfo();
    }
    /** get all rows for current servers */
    public List<DistAgentServerRow> getServerRows() {
        return servers.values().stream().map(s -> s.createServerRow()).toList();
    }
    /** get count of servers */
    public int getServersCount() {
        return servers.size();
    }
    /** get all UIDs of servers */
    public List<String> getServerKeys() {
        return servers.values().stream().map(v -> v.getServerGuid()).collect(Collectors.toList());
    }
    /** get number of clients */
    public int getClientsCount() {
        return clients.totalSize();
    }

    /** get client keys */
    public List<String> getClientKeys() {
        return clients.getAllValues().stream().map(x -> x.getClientGuid()).collect(Collectors.toList());
    }
    /** check list of active servers and connect to the server if this is still not connected */
    public void checkActiveServers(List<DistAgentServerRow> activeServers) {
        log.info("Connectors updating servers from registers for agent: " + parentAgent.getAgentGuid() + ", count: " + activeServers.size() + ", agentServers: " + agentServers.size() + ", servers: " + servers.size() + ", clients: " + clients.totalSize());
        for (DistAgentServerRow srv: activeServers) {
            agentServers.putIfAbsent(srv.getServerGuid(), srv);
        }
        // check all servers
        agentServers.values().stream().forEach(srv -> {
            if (!srv.getAgentGuid().equals(parentAgent.getAgentGuid())) {
                Optional<AgentClient> client = clients.getValue(srv.getAgentGuid(), srv.getServerGuid());
                if (client.isEmpty()) {
                    try {
                        log.info("Connectors from agent: " + parentAgent.getAgentGuid() +  ", NO client to agent: " + srv.getAgentGuid() + ", server: " + srv.getServerGuid() + ", type" + srv.getServerType() + ", creating NEW ONE !!!!!!!!!");
                        var createdClient = createClient(srv);
                        if (createdClient.isPresent()) {
                            clients.add(srv.getAgentGuid(), srv.getServerGuid(), createdClient.get());
                            clientQueue.add(createdClient.get());
                            clientTable.add(createdClient.get());
                        }
                    } catch (Exception ex) {
                        log.warn("Cannot create client to server: " + srv.getServerGuid() + ", reason: " + ex.getMessage());
                        addIssueToAgent("checkActiveServers", ex);
                    }
                } else {
                    // client.get().send("CHECK SERVERS - PING AGENT FROM " + parentAgent.getAgentGuid());
                }
            }
        });
        log.info("Connectors AFTER check servers for agent: " + parentAgent.getAgentGuid() + ", agentServers: " + agentServers.size() + ", servers: " + servers.size() + ", clients: " + clients.totalSize());
    }
    /** register new client created local as part of server */
    public void registerLocalClient(AgentClient client) {
        localConnectors.put(client.getClientGuid(), client);
        log.info("Register new local client for agent: " + parentAgent.getAgentGuid() + ", client UID: " + client.getClientGuid() + ", total local clients: " + localConnectors.size());
    }
    /** message send to agents, directed to services, selected method */
    public void sendMessage(DistMessage msg) {
        sendMessage(msg.withNoCallbacks());
    }
    /** message send to agents, directed to services, selected method, add callbacks to be called when response would be back */
    public void sendMessage(DistMessageFull msg) {
        log.debug("Sending some message from agent: " + parentAgent.getAgentGuid() + ", message: " + msg.getMessage().toString() + ", clients: " + clients.size());
        if (msg.getMessage().isTypeRequest()) {
            sentMessages.addItem(msg.getMessage().getMessageUid(), msg, msg.getMessage().getValidTill());
        }
        if (msg.getMessage().isSentToBroadcast()) {
            sendMessageBroadcast(msg);
        } else if (msg.getMessage().isSentToRandom()) {
            sendMessageRandom(msg);
        } else if (msg.getMessage().isSentRoundRobin()) {
            sendMessageRoundRobin(msg);
        } else if (msg.getMessage().isSentToTag()) {
            sendMessageTag(msg);
        } else {
            sendMessageAgent(msg);
        }
    }
    /** message send broadcast */
    public void sendMessageBroadcast(DistMessageFull msg) {
        // sending broadcast - to all known agents
        var allClients = clients.getAllValues();
        log.info("Sending broadcast message from agent: " + parentAgent.getAgentGuid() + " to all clients: " + allClients.size() + ", message UID: " + msg.getMessage().getMessageUid());
        allClients.stream().forEach(client -> {
            var res = client.send(msg.getMessage());
            msg.addClient(client);
            msg.sendWithResult(res);
        });
    }
    /** message send to random agent from clients */
    public void sendMessageRandom(DistMessageFull msg) {
        // TODO: add to message queue and map for responses
        synchronized (clientTable) {
            if (clientTable.size() > 0) {
                int clientId = DistUtils.randomInt(clientTable.size());
                AgentClient cl = clientTable.get(clientId);
                if (cl != null) {
                    cl.send(msg.getMessage());
                }
            } else {
                msg.applyCallback(DistCallbackType.onClientNotFound);
                // TODO: add to call later
            }
        }
    }
    /** message send broadcast */
    public void sendMessageRoundRobin(DistMessageFull msg) {
        AgentClient client = clientQueue.poll();
        if (client != null) {
            clientQueue.add(client);
            client.send(msg.getMessage());
            msg.addClient(client);
            msg.sendWithSuccess();
        } else {
            msg.applyCallback(DistCallbackType.onClientNotFound);
            //msgCallbacks.getCallbacks().applyErrorCallback(msg);
        }
    }
    /** message send to clients with tags */
    public void sendMessageTag(DistMessageFull msg) {
         // TODO: get all agents for given tags and send them message
        String[] tags = msg.getMessage().getTags().split(",");
        clients.getAllValues().stream().filter(client -> client.hasTags(tags)).forEach(client -> {
            client.send(msg.getMessage());
            client.getClientGuid();
        });
    }
    /** message send to only one agent by GUID */
    public void sendMessageAgent(DistMessageFull msg) {
        // sending to exactly one agent
        var clientMap = clients.get(msg.getMessage().getToAgent());
        if (clientMap == null || clientMap.size() == 0) {
            msg.applyCallback(DistCallbackType.onClientNotFound);
        } else {
            // TODO: check if it should be send to any client for that agent or just one of clients
            clientMap.values().stream().forEach(x -> x.send(msg.getMessage()));
        }
    }
    /** mark response for this message, it is executing callbacks onResponse */
    public void markResponse(DistMessage msg) {
        try {
            DistMessageFull msgFull = sentMessages.getByUid(msg.getMessageUid());
            if (msgFull != null) {
                log.debug("Mark response for message: " + msg.getMessageUid() + ", sentMessages: " + sentMessages.getItemsCount() + ", callbacks: " + msgFull.getCallbacks().getCallbacksCount());
                msgFull.applyCallback(DistCallbackType.onResponse, msg);
                sentMessages.removeByUid(msg.getMessageUid(), msgFull.getMessage().getValidTill());
            } else {
                log.debug("Mark response for message: " + msg.getMessageUid() + ", NO original message, messages: " + sentMessages.getItemsCount());
            }
        } catch (Exception ex) {
            addIssueToAgent("markResponse", ex);
        }
    }

    /** close all connectors, clients, servers  */
    protected void onClose() {
        log.info("Closing connectors for agent: " + parentAgent.getAgentGuid() + ", servers: " + servers.size() + ", clients: " + clients.totalSize());
        servers.values().stream().forEach(serv -> {
            serv.close();
        });
        clients.getAllValues().stream().forEach(cli -> cli.close());
        log.info("Closed all connectors for agent: " + parentAgent.getAgentGuid());
    }

    /** create new client that would be used for connecting to given server */
    private Optional<AgentClient> createClient(DistAgentServerRow srv) {
        createEvent("createClient");
        // TODO: create client using abstract factory based on server instead of ifelse(s)
        log.info(" Creating new client that would be connected to agent: " + srv.getAgentGuid() + ", type: " + srv.getServerType() + ", host: " + srv.getServerHost());
        DistMessage welcomeMsg = DistMessage.createMessage(DistMessageType.system, parentAgent.getAgentGuid(), DistServiceType.agent, srv.getAgentGuid(), DistServiceType.agent, "welcome",  "");
        if (srv.getServerType().equals(DistClientType.socket.name())) {
            log.info("Creating new socket client that would be connected to agent: " + srv.getAgentGuid() + ", type: " + srv.getServerType() + ", host: " + srv.getServerHost());
            var client = new SocketServerClient(parentAgent, srv);
            client.send(welcomeMsg);
            return Optional.of(client);
        } else if (srv.getServerType().equals(DistClientType.http.name())) {
            log.info("Creating new socket client that would be connected to agent: " + srv.getAgentGuid() + ", type: " + srv.getServerType() + ", host: " + srv.getServerHost());
            var client = new HttpClient(parentAgent, srv);
            client.send(welcomeMsg);
            return Optional.of(client);
        } else if (srv.getServerType().equals(DistClientType.datagram.name())) {
            log.info("Creating new datagram client that would be connected to agent: " + srv.getAgentGuid() + ", type: " + srv.getServerType() + ", host: " + srv.getServerHost());
            var client = new DatagramClient(parentAgent, srv);
            client.send(welcomeMsg);
            return Optional.of(client);
        } else if (srv.getServerType().equals(DistClientType.kafka.name())) {
            log.info("Creating new Kafka client that would be connected to agent: " + srv.getAgentGuid() + ", type: " + srv.getServerType() + ", host: " + srv.getServerHost());
            var client = new AgentKafkaClient(parentAgent, srv);
            client.send(welcomeMsg);
            return Optional.of(client);
        }
        // TODO: create other types of clients based on servertype that client should connect to
        return Optional.empty();
    }

}
