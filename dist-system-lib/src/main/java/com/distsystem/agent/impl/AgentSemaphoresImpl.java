package com.distsystem.agent.impl;

import com.distsystem.agent.AgentInstance;
import com.distsystem.agent.impl.semaphores.SemaphoreBase;
import com.distsystem.api.DistConfig;
import com.distsystem.api.DistMessage;
import com.distsystem.api.enums.DistServiceType;
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
        parentAgent.getAgentServices().registerService(this);
        DistConfig cfg = parentAgent.getConfig();
        initializeSemaphores(cfg);
        log.info("--------> Created new semaphores service with GUID: " + guid + ", CONFIG: " + getConfig().getConfigGuid() + ", properties: " + getConfig().getProperties().size());
    }

    /** */
    private void initializeSemaphores(DistConfig cfg) {
        if (parentAgent.getConfig().hasProperty(DistConfig.AGENT_SEMAPHORE_OBJECT_JDBC_URL)) {
            initializeSemaphoreForClass("com.distsystem.agent.impl.semaphores.SemaphoreJdbc");
        }
    }
    /** initialization of semaphore manager for given class name */
    private void initializeSemaphoreForClass(String className) {
        try {
            log.info("Try to create new semaphore manager for agent: " + parentAgent.getAgentGuid() + ", class: " + className);
            SemaphoreBase sem = (SemaphoreBase)Class.forName(className)
                    .getConstructor(AgentInstance.class)
                    .newInstance(parentAgent);
            semaphoreManagers.put(className, sem);
        } catch (Exception ex) {
            log.warn("Cannot initialize semaphore manager for agent: " + parentAgent.getAgentGuid() + ", class: " + className + ", reason: " + ex.getMessage());
        }
    }

    /** read configuration and re-initialize this component */
    public boolean reinitialize() {
        // TODO: implement reinitialization
        return true;
    }
    @Override
    public DistServiceType getServiceType() {
        return DistServiceType.semaphores;
    }
    @Override
    public DistMessage processMessage(DistMessage msg) {
        return msg.methodNotFound();
    }

    /** lock semaphore */
    public synchronized boolean lock(String semaphoreName, long maxWaitingTime) {
        semaphoreManagers.values().stream().filter(s -> s.lock(semaphoreName, maxWaitingTime)).findFirst();

        return true;
    }
    /** lock semaphore */
    public synchronized boolean unlock(String semaphoreName) {

        return true;
    }
    /** lock and unlock semaphore */
    public synchronized <T> Optional<T> withLockedSemaphore(String semaphoreName, long maxWaitingTime, Supplier<T> supplier) {
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
