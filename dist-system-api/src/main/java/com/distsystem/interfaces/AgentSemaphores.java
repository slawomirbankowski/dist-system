package com.distsystem.interfaces;

import java.util.Optional;
import java.util.function.Supplier;

/** distributed semaphores in agent distributed environment */
public interface AgentSemaphores extends DistService {

    /** lock and unlock semaphore */
    <T> Optional<T> withLockedSemaphore(String semaphoreName, long maxWaitingTime, Supplier<T> supplier);
    /** lock semaphore */
    boolean lock(String semaphoreName, long maxWaitingTime);
    /** lock semaphore */
    boolean unlock(String semaphoreName);

}
