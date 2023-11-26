package com.distsystem.api;

import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.DistService;

import java.time.LocalDateTime;

/** initial parameters for service object */
public class ServiceObjectParams {

    private final Agent agent;
    private final DistService parentService;
    private final DistConfigBucket bucket;
    private final String className;
    private final LocalDateTime createdDate = LocalDateTime.now();

    public ServiceObjectParams(Agent agent, DistService parentService, String className, DistConfigBucket bucket) {
        this.agent = agent;
        this.parentService = parentService;
        this.className = className;
        this.bucket = bucket;
    }

    /** get agent for this service object */
    public Agent getAgent() {
        return agent;
    }
    /** get parent service that owns this object */
    public DistService getParentService() {
        return parentService;
    }
    /** get configurational bucket */
    public DistConfigBucket getBucket() {
        return bucket;
    }
    /** get creation date and time of this parameters */
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    /** get name of class to be created for service object */
    public String getClassName() {
        return className;
    }

    /** create parameters for service object */
    public static ServiceObjectParams create(Agent agent, DistService parentService, String className, DistConfigBucket bucket) {
        return new ServiceObjectParams(agent, parentService, className, bucket);
    }
}
