package com.distsystem.agent.services;

import com.distsystem.agent.semaphore.SemaphoreBase;
import com.distsystem.api.*;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.api.info.AgentSemaphoreManagerInfo;
import com.distsystem.api.info.AgentSemaphoresInfo;
import com.distsystem.base.ServiceBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentSemaphores;
import com.distsystem.utils.AdvancedMap;
import com.distsystem.utils.DistUtils;
import com.distsystem.utils.DistWebApiProcessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/** */
public class AgentSemaphoresImpl extends ServiceBase implements AgentSemaphores {

    /** all available semaphore implementations */
    private final java.util.Map<String, SemaphoreBase> semaphoreManagers = new HashMap<>();
    /**  all known semaphores */
    private final java.util.Map<String, AgentSemaphoreLocal> semaphores = new HashMap<>();

    /** creates new ML service */
    public AgentSemaphoresImpl(Agent parentAgent) {
        super(parentAgent);
        parentAgent.getServices().registerService(this);
    }
    /** count objects in this agentable object including this object */
    public long countObjectsService() {
        // TODO: calculate counts of objects
        return 2L + semaphoreManagers.size()*14L;
    }
    /** read configuration and re-initialize this component */
    protected boolean onReinitialize() {
        // TODO: implement reinitialization
        registerConfigGroup(DistConfig.AGENT_SEMAPHORE_OBJECT);
        return true;
    }
    /** change values in configuration bucket */
    public AdvancedMap initializeConfigBucket(DistConfigBucket bucket) {
        return initializeSemaphore(bucket);
    }

    /** run after initialization */
    public void afterInitialization() {

    }
    /** get detailed info about this service */
    public AgentSemaphoresInfo getInfo() {
        return new AgentSemaphoresInfo(semaphoreManagers.values().stream().map(s -> s.getSemaphoreManagerInfo()).toList());
    }

    /** initialization of semaphore manager for given class name */
    private AdvancedMap initializeSemaphore(DistConfigBucket bucket) {
        AdvancedMap status = AdvancedMap.create(this);
        touch("initializeSemaphore");
        createEvent("initializeSemaphore", bucket.getKey().getConfigType());
        String className = DistConfig.AGENT_SEMAPHORE_CLASS_MAP.get(bucket.getKey().getConfigType());
        try {
            log.info("Try to create new semaphore manager for agent: " + parentAgent.getAgentGuid() + ", class: " + className + ", bucket key: " + bucket.getKey());
            SemaphoreBase sem = (SemaphoreBase)Class.forName(className)
                    .getConstructor(ServiceObjectParams.class)
                    .newInstance(ServiceObjectParams.create(parentAgent, this, className, bucket));
            semaphoreManagers.put(bucket.getKey().toString(), sem);
            return status.withStatus("OK");
        } catch (Exception ex) {
            log.warn("Cannot initialize semaphore manager for agent: " + parentAgent.getAgentGuid() + ", class: " + className + ", reason: " + ex.getMessage());
            return status.exception(ex);
        }
    }
    @Override
    public DistServiceType getServiceType() {
        return DistServiceType.semaphores;
    }
    /** get description of this service */
    public String getServiceDescription() {
        return "Distributed semaphores that can be locked so method could be run once across all agents.";
    }
    /** additional web API endpoints */
    protected DistWebApiProcessor additionalWebApiProcessor() {
        return new DistWebApiProcessor(getServiceType())
                .addHandlerGet("semaphore-managers", (m, req) -> req.responseOkJsonSerialize(getSemaphoreManagerInfos()));
    }
    /** get info objects for semaphore managers */
    public List<AgentSemaphoreManagerInfo> getSemaphoreManagerInfos() {
        return semaphoreManagers.values().stream().map(s -> s.getSemaphoreManagerInfo()).toList();
    }


    /** lock semaphore */
    public synchronized boolean lock(String semaphoreName, long maxWaitingTime) {
        createEvent("lock");
        AgentSemaphoreLocal localSem = semaphores.get(semaphoreName);
        if (localSem == null) {
            localSem = new AgentSemaphoreLocal(semaphoreName);
            semaphores.put(semaphoreName, localSem);
        }
        semaphoreManagers.values().stream().filter(s -> s.lock(semaphoreName, maxWaitingTime)).findFirst();
        boolean locked = true;
        localSem.tryLock(locked);
        return locked;
    }
    /** lock semaphore */
    public synchronized boolean unlock(String semaphoreName) {
        createEvent("unlock");
        AgentSemaphoreLocal localSem = semaphores.get(semaphoreName);
        if (localSem == null) {
            localSem = new AgentSemaphoreLocal(semaphoreName);
            semaphores.put(semaphoreName, localSem);
        }
        // TODO: unlock semaphore

        localSem.tryUnlock(false);
        return true;
    }
    /** lock and unlock semaphore */
    public synchronized <T> Optional<T> withLockedSemaphore(String semaphoreName, long maxWaitingTime, Supplier<T> supplier) {
        createEvent("withLockedSemaphore");
        if (lock(semaphoreName, maxWaitingTime)) {
            T obj = supplier.get();
            unlock(semaphoreName);
            return Optional.of(obj);
        } else {
            return Optional.empty();
        }
    }
    /** update configuration of this Service */
    public void updateConfig(DistConfig newCfg) {

    }
    @Override
    protected void onClose() {
    }
}
