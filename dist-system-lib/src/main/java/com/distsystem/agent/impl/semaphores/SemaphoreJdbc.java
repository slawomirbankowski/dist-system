package com.distsystem.agent.impl.semaphores;

import com.distsystem.agent.AgentInstance;
import com.distsystem.dao.DaoJdbcBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.DistService;

/** semaphores based on JDBC connection */
public class SemaphoreJdbc extends SemaphoreBase {

    private DaoJdbcBase dao;

    /** */
    public SemaphoreJdbc(AgentInstance parentAgent) {
        super(parentAgent);
    }

    /** lock semaphore */
    public boolean lock(String semaphoreName, long maxWaitingTime) {

        touch();

        return true;
    }
    /** lock semaphore */
    public boolean unlock(String semaphoreName) {
        touch();

        return true;
    }

    /** close all items in this service */
    protected void onClose() {

    }

}
