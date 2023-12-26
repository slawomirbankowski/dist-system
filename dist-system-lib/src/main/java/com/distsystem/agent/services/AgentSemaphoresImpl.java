package com.distsystem.agent.services;

import com.distsystem.agent.semaphore.SemaphoreBase;
import com.distsystem.api.*;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.api.info.AgentMemoryInfo;
import com.distsystem.api.info.AgentSemaphoresInfo;
import com.distsystem.base.ServiceBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentSemaphores;

import java.util.HashMap;
import java.util.Optional;
import java.util.function.Supplier;

/** */
public class AgentSemaphoresImpl extends ServiceBase implements AgentSemaphores {

    /** all available semaphore implementations */
    private final java.util.Map<String, SemaphoreBase> semaphoreManagers = new HashMap<>();

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
    public void initializeConfigBucket(DistConfigBucket bucket) {
        initializeSemaphore(bucket);
    }

    /** run after initialization */
    public void afterInitialization() {

    }
    /** get detailed info about this service */
    public AgentSemaphoresInfo getInfo() {
        return new AgentSemaphoresInfo(semaphoreManagers.values().stream().map(s -> s.getSemaphoreManagerInfo()).toList());
    }

    /** initialization of semaphore manager for given class name */
    private void initializeSemaphore(DistConfigBucket bucket) {
        touch("initializeSemaphore");
        String className = DistConfig.AGENT_SEMAPHORE_CLASS_MAP.get(bucket.getKey().getConfigType());
        try {
            log.info("Try to create new semaphore manager for agent: " + parentAgent.getAgentGuid() + ", class: " + className + ", bucket key: " + bucket.getKey());
            SemaphoreBase sem = (SemaphoreBase)Class.forName(className)
                    .getConstructor(ServiceObjectParams.class)
                    .newInstance(ServiceObjectParams.create(parentAgent, this, className, bucket));
            semaphoreManagers.put(bucket.getKey().toString(), sem);
        } catch (Exception ex) {
            log.warn("Cannot initialize semaphore manager for agent: " + parentAgent.getAgentGuid() + ", class: " + className + ", reason: " + ex.getMessage());
        }
    }
    @Override
    public DistServiceType getServiceType() {
        return DistServiceType.semaphores;
    }
    /** get description of this service */
    public String getServiceDescription() {
        return "Distributed semaphores";
    }
    /** lock semaphore */
    public synchronized boolean lock(String semaphoreName, long maxWaitingTime) {
        createEvent("lock");
        semaphoreManagers.values().stream().filter(s -> s.lock(semaphoreName, maxWaitingTime)).findFirst();
        return true;
    }
    /** lock semaphore */
    public synchronized boolean unlock(String semaphoreName) {
        createEvent("unlock");

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
