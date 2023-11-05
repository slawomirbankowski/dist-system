package com.distsystem.agent.impl;

import com.distsystem.api.*;
import com.distsystem.api.enums.DistComponentType;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.api.info.AgentThreadInfo;
import com.distsystem.api.info.DistThreadsInfo;
import com.distsystem.base.ServiceBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentComponent;
import com.distsystem.interfaces.AgentThreads;
import com.distsystem.utils.DistWebApiProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.stream.Collectors;

public class AgentThreadsImpl extends ServiceBase implements AgentThreads {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(AgentThreadsImpl.class);
    /** all registered threads */
    private final java.util.concurrent.ConcurrentLinkedQueue<AgentThreadObject> registeredThreads = new java.util.concurrent.ConcurrentLinkedQueue<>();

    /** creates service manager for agent with parent agent assigned */
    public AgentThreadsImpl(Agent parentAgent) {
        super(parentAgent);
        parentAgent.getAgentServices().registerService(this);
    }

    /** get type of service: cache, measure, report, flow, space, ... */
    public DistServiceType getServiceType() {
        return DistServiceType.threads;
    }
    /** process message, returns message with status */
    public DistMessage processMessage(DistMessage msg) {
        return msg.notSupported();
    }

    /** update configuration of this Service to add registrations, services, servers, ... */
    public void updateConfig(DistConfig newCfg) {
    }

    /** read configuration and re-initialize this component */
    public boolean reinitialize() {
        // TODO: implement reinitialization
        return true;
    }

    /** additional web API endpoints */
    protected DistWebApiProcessor additionalWebApiProcessor() {
        return new DistWebApiProcessor(getServiceType())
                .addHandlerPost("info", (m, req) -> req.responseOkJsonSerialize(getThreadsInfo()))
                .addHandlerPost("threads-info", (m, req) -> req.responseOkJsonSerialize(registeredThreads.stream().map(t -> t.getInfo()).toList()))
                .addHandlerPost("threads-friendly-names", (m, req) -> req.responseOkJsonSerialize(registeredThreads.stream().map(t -> t.getFriendlyName()).toList()))
                .addHandlerPost("threads-guids", (m, req) -> req.responseOkJsonSerialize(registeredThreads.stream().map(t -> t.getThreadGuid()).toList()))
                .addHandlerPost("threads-names", (m, req) -> req.responseOkJsonSerialize(registeredThreads.stream().map(t -> t.getThreadName()).toList()))
                .addHandlerPost("stop", (m, req) -> req.responseOkJsonSerialize(stopThread(req.getParamOne())))
                .addHandlerPost("thread-info", (m, req) -> req.responseOkJsonSerialize(threadInfo(req.getParamOne())));
    }
    /** get type of this component */
    public DistComponentType getComponentType() {
        return DistComponentType.threads;
    }

    /** get number of threads */
    public int getThreadsCount() {
        return registeredThreads.size();
    }
    /** register thread to be maintained by Agent */
    public void registerThread(AgentComponent parent, Thread thread, String threadFriendlyName) {
        synchronized (registeredThreads) {
            AgentThreadObject thObj = new AgentThreadObject(parent, thread, threadFriendlyName);
            registeredThreads.add(thObj);
        }
    }
    /** get information about managed threads */
    public DistThreadsInfo getThreadsInfo() {
        var infos = registeredThreads.stream().map(x -> x.getInfo()).collect(Collectors.toList());
        return new DistThreadsInfo(infos.size(), infos);
    }
    /** stop thread for GUID or name*/
    public Optional<AgentThreadInfo> stopThread(String guid) {
        Optional<AgentThreadObject> th = registeredThreads.stream().filter(t -> t.getThreadGuid().equals(guid)).findFirst();
        return th.map(t -> t.stopThread());
    }
    /** stop thread for GUID or name*/
    public Optional<AgentThreadInfo> threadInfo(String guid) {
        return registeredThreads.stream().filter(t -> t.getThreadGuid().equals(guid)).map(t -> t.getInfo()).findFirst();
    }

    /** close - make sure all threads would be closed */
    protected void onClose() {
        log.info("Closing all threads: " + registeredThreads.size());
        synchronized (registeredThreads) {
            registeredThreads.forEach(AgentThreadObject::close);
        }
    }

}
