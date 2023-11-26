package com.distsystem.agent.semaphore;

import com.distsystem.api.ServiceObjectParams;
import com.distsystem.base.ServiceObjectBase;

/** base class for semaphore implementation in distributed environment */
public abstract class SemaphoreBase extends ServiceObjectBase {

    /** */
    public SemaphoreBase(ServiceObjectParams params) {
        super(params);
    }
    /** lock semaphore */
    public abstract boolean lock(String semaphoreName, long maxWaitingTime);
    /** lock semaphore */
    public abstract boolean unlock(String semaphoreName);

}
