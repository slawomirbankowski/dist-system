package com.distsystem.agent.semaphore;

import com.distsystem.api.ServiceObjectParams;
import com.distsystem.dao.DaoJdbcBase;

/** semaphores based on JDBC connection */
public class SemaphoreJdbc extends SemaphoreBase {

    private DaoJdbcBase dao;

    /** */
    public SemaphoreJdbc(ServiceObjectParams params) {
        super(params);
    }

    /** count objects in this agentable object including this object */
    public long countObjectsAgentable() {
        return 2L;
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
