package com.distsystem.dao;

import com.distsystem.api.*;
import com.distsystem.base.DaoBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentComponent;
import com.distsystem.utils.AdvancedMap;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/** base class for any MongoDB based DAO */
public class DaoMongodbBase extends DaoBase implements AgentComponent {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(DaoMongodbBase.class);

    /** */
    private String mongoHost;
    /** */
    private int mongoPort;
    /** */
    private String mongoDbName;
    private MongoClient mongoClient;
    private MongoDatabase mongoDb;

    /** creates new DAO to JDBC database */
    public DaoMongodbBase(DaoParams params, Agent agent) {
        super(params, agent);
        this.mongoHost = resolve(params.getHost());
        this.mongoPort = resolveToInt(params.getPortStr(), 27017);
        this.mongoDbName =  resolve(params.getDatabase());
        onInitialize();
    }
    /** creates new DAO to JDBC database */
    public DaoMongodbBase(String mongoHost, String mongoPort, String mongoDatabase, Agent agent) {
        super(DaoParams.mongoParams(mongoHost, mongoPort), agent);
        this.mongoHost = resolve(mongoHost);
        this.mongoPort = resolveToInt(mongoPort, 27017);
        this.mongoDbName = resolve(mongoDatabase);
        onInitialize();
    }

    /** count objects in this agentable object including this object */
    public long countObjectsAgentable() {
        return 2L;
    }
    /** read configuration and re-initialize this component */
    public boolean componentReinitialize() {
        // TODO: implement reinitialization
        try {
            if (mongoClient!=null) {
                mongoClient.close();
            }
        } catch (Exception ex) {
            log.info("Cannot close previous MongoDB client, reason: " + ex.getMessage());
        }
        onInitialize();
        return true;
    }
    /** returns true if DAO is connected */
    public boolean isConnected() {
        try {
            return mongoClient.getDatabase("default") != null;
        } catch (Exception ex) {
            return false;
        }
    }
    /** test DAO and returns items */
    public AdvancedMap testDao() {
        AdvancedMap status = AdvancedMap.create(this);
        return status.appendMap(Map.of("isConnected", isConnected(), "url", getUrl(), "className", this.getClass().getName()));
    }
    /** get URL of this DAO */
    public String getUrl() {
        return mongoHost + ":" + mongoPort;
    }
    /**  */
    public void onInitialize() {
        try {
            log.info("DAO Connecting to MongoDB for agent:" + getAgent().getAgentGuid() + ", host: " + mongoHost + ", port: " + mongoPort + ", database: " + mongoDbName);
            mongoClient = new MongoClient(mongoHost, mongoPort);
            mongoDb = mongoClient.getDatabase(mongoDbName);
            log.info("DAO Connected to MongoDB, cluster nodes: " + mongoDb.getName());
        } catch (Exception ex) {
            log.warn("DAO Cannot connect to MongoDB at host:" + mongoHost + ", port: " + mongoPort + ", database: " + mongoDbName+ ", reason: " + ex.getMessage(), ex);
            addIssueToAgent("onInitialize", ex);
        }
    }
    /** get all MongoDB collections */
    public Collection<String> getDaoStructures() {
        List<String> collections = new LinkedList<>();
        mongoDb.listCollectionNames().iterator().forEachRemaining(collections::add);
        return collections;
    }
    /** create new structure for this DAO */
    public AdvancedMap createStructure(DaoModel model) {
        AdvancedMap status = AdvancedMap.create(this);
        try {
            mongoDb.createCollection(model.getTableName());
            mongoDb.getCollection(model.getTableName());
            //createIndex(model.getTableName());
            return status.append("type", "collection").withStatus("OK");
        } catch (Exception ex) {
            return status.exception(ex);
        }
    }
    /** insert full objects to given structure */
    public <X extends BaseRow> int executeInsertRowsForModel(DaoModel<X> model, List<X> objs) {
        // TODO: implement inserting object into model structure

        return 0;
    }
    /** close current Elasticsearch DAO */
    protected void onClose() {
    }
    public boolean closeDao() {
        return true;
    }

}
