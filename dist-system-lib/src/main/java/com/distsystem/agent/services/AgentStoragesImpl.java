package com.distsystem.agent.services;

import com.distsystem.agent.report.StorageBase;
import com.distsystem.api.*;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.base.ServiceBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.Storages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/** Service to manage Storages, each storage can be connection to one storage of given type.
 * There might be many types of storages like: JDBC, Elasticsearch, Kafka, FTP, MongoDB, HTTP
 * Storage has iterator to get data for given query or method to get all rows for given query.
 *  */
public class AgentStoragesImpl extends ServiceBase implements Storages {

    /** local logger for this clas s*/
    protected static final Logger log = LoggerFactory.getLogger(AgentStoragesImpl.class);

    /** map of storage definitions */
    private final Map<String, StorageDto> storageDefinitions = new HashMap<>();
    /** map of storages */
    private final Map<String, StorageBase> storages = new HashMap<>();

    public AgentStoragesImpl(Agent agent) {
        super(agent);
        parentAgent.getServices().registerService(this);
    }

    /** count objects in this agentable object including this object */
    public long countObjectsService() {
        return 2L;
    }

    /** get type of service: cache, measure, report, flow, space, ... */
    public DistServiceType getServiceType() {
        return DistServiceType.storage;
    }
    /** read configuration and re-initialize this component */
    protected boolean onReinitialize() {
        registerConfigGroup(DistConfig.AGENT_STORAGE_OBJECT);
        return true;
    }

    /** change values in configuration bucket */
    public void initializeConfigBucket(DistConfigBucket bucket) {
        // TODO: insert, update, delete of bucket
    }

    /** update configuration of this Service */
    public void updateConfig(DistConfig newCfg) {
        // TODO: update configuration of this service
    }
    /** close and deinitialize service */
    public void onClose() {
        // TODO: close this reports service
    }

}
