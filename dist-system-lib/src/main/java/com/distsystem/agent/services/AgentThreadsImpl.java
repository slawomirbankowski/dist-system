package com.distsystem.agent.services;

import com.distsystem.api.*;
import com.distsystem.api.enums.DistComponentType;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.api.info.AgentThreadInfo;
import com.distsystem.api.info.DistThreadsInfo;
import com.distsystem.base.ServiceBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentComponent;
import com.distsystem.interfaces.AgentThreads;
import com.distsystem.utils.DistUtils;
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
        parentAgent.getServices().registerService(this);
    }

    /** count objects in this agentable object including this object */
    public long countObjectsService() {
        // TODO: calculate counts of objects
        return 2L;
    }
    /** get type of service: cache, measure, report, flow, space, ... */
    public DistServiceType getServiceType() {
        return DistServiceType.threads;
    }

    /** update configuration of this Service to add registrations, services, servers, ... */
    public void updateConfig(DistConfig newCfg) {
    }

    /** read configuration and re-initialize this component */
    protected boolean onReinitialize() {
        // TODO: implement reinitialization
        initialized = true;
        return true;
    }

    /** additional web API endpoints */
    protected DistWebApiProcessor additionalWebApiProcessor() {
        return new DistWebApiProcessor(getServiceType())
                .addHandlerGet("info", (m, req) -> req.responseOkJsonSerialize(getInfo()))
                .addHandlerGet("threads-all", (m, req) -> req.responseOkJsonSerialize(DistUtils.getAllThreads()))
                .addHandlerGet("threads-info", (m, req) -> req.responseOkJsonSerialize(registeredThreads.stream().map(t -> t.getInfo()).toList()))
                .addHandlerGet("threads-friendly-names", (m, req) -> req.responseOkJsonSerialize(registeredThreads.stream().map(t -> t.getFriendlyName()).toList()))
                .addHandlerGet("threads-guids", (m, req) -> req.responseOkJsonSerialize(registeredThreads.stream().map(t -> t.getThreadGuid()).toList()))
                .addHandlerGet("threads-names", (m, req) -> req.responseOkJsonSerialize(registeredThreads.stream().map(t -> t.getThreadName()).toList()))
                .addHandlerPost("stop", (m, req) -> req.responseOkJsonSerialize(stopThread(req.getParamOne())))
                .addHandlerGet("thread-info", (m, req) -> req.responseOkJsonSerialize(threadInfo(req.getParamOne())));
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
        createEvent("registerThread");
        synchronized (registeredThreads) {
            createdCount.incrementAndGet();
            AgentThreadObject thObj = new AgentThreadObject(parent, thread, threadFriendlyName);
            registeredThreads.add(thObj);

        }
    }

    /** get information about managed threads */
    public DistThreadsInfo getInfo() {
        var infos = registeredThreads.stream().map(x -> x.getInfo()).collect(Collectors.toList());
        return new DistThreadsInfo(infos.size(), infos);
    }
    /** stop thread for GUID or name*/
    public Optional<AgentThreadInfo> stopThread(String guid) {
        createEvent("stopThread");
        Optional<AgentThreadObject> th = registeredThreads.stream().filter(t -> t.getThreadGuid().equals(guid)).findFirst();
        return th.map(t -> t.stopThread());
    }
    /** stop thread for GUID or name*/
    public Optional<AgentThreadInfo> threadInfo(String guid) {
        createEvent("threadInfo");
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
