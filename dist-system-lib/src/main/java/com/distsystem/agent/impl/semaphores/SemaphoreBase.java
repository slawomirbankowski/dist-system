package com.distsystem.agent.impl.semaphores;

import com.distsystem.base.Agentable;
import com.distsystem.interfaces.Agent;

/** base class for semaphore implementation in distributed environment */
public abstract class SemaphoreBase extends Agentable {

    /** */
    public SemaphoreBase(Agent parentAgent) {
        super(parentAgent);
    }
    /** lock semaphore */
    public abstract boolean lock(String semaphoreName, long maxWaitingTime);
    /** lock semaphore */
    public abstract boolean unlock(String semaphoreName);

}
