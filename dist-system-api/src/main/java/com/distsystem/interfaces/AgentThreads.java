package com.distsystem.interfaces;


import com.distsystem.api.info.DistThreadsInfo;

/** interface for threads manager in agent
 * object to manage threads created in agent - to be sure that all threads are properly maintained and stopped when not needed */
public interface AgentThreads {

    /** register thread to be maintained */
    void registerThread(AgentComponent parent, Thread thread, String threadFriendlyName);
    /** get number of threads */
    int getThreadsCount();
    /** get information about managed threads in Dist system */
    DistThreadsInfo getThreadsInfo();
    /** close  */
    void close();
}
