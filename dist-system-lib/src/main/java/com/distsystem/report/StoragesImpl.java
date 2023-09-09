package com.distsystem.report;

import com.distsystem.api.*;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.base.ServiceBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.Storages;
import com.distsystem.utils.DistUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/** Service to manage Storages, each storage can be connection to one storage of given type.
 * There might be many types of storages like: JDBC, Elasticsearch, Kafka, FTP, MongoDB, HTTP
 * Storage has iterator to get data for given query or method to get all rows for given query.
 *  */
public class StoragesImpl extends ServiceBase implements Storages {

    /** local logger for this clas s*/
    protected static final Logger log = LoggerFactory.getLogger(StoragesImpl.class);

    /** map of storage definitions */
    private Map<String, StorageDto> storageDefinitions = new HashMap<>();
    /** map of storages */
    private Map<String, StorageBase> storages = new HashMap<>();

    public StoragesImpl(Agent agent) {
        super(agent);
    }


    /** get type of service: cache, measure, report, flow, space, ... */
    public DistServiceType getServiceType() {
        return DistServiceType.report;
    }
    /** create new service UID for this service */
    protected String createServiceUid() {
        return DistUtils.generateCacheGuid();
    }

    /** process message, returns message with status */
    public DistMessage processMessage(DistMessage msg) {
        // TODO: implement processing message
        return msg;
    }
    /** handle API request in this Web API for this service */
    public AgentWebApiResponse handleRequest(AgentWebApiRequest request) {
        // TODO: implement handling Web Api request by reports
        return request.responseNotImplemented();
    }
    /** close and deinitialize service */
    public void onClose() {
        // TODO: close this reports service
    }

}
