package com.distsystem.dao;

import com.distsystem.api.*;
import com.distsystem.base.DaoBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentComponent;
import com.distsystem.utils.AdvancedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.*;

/** base class for any Redis based DAO */
public class DaoRedisBase extends DaoBase implements AgentComponent {

    /** local logger for this class */
    protected static final Logger log = LoggerFactory.getLogger(DaoRedisBase.class);

    /** Redis host name or address */
    private String redisHost;
    /** Redist port */
    private int redisPort;
    /** Redis client for read/write actions */
    private Jedis jedis;

    /** creates new DAO to Redis database */
    public DaoRedisBase(DaoParams params, Agent agent) {
        super(params, agent);
        this.redisHost = resolve(params.getHost());
        this.redisPort = resolveToInt(params.getPortStr(), 6379);
        onInitialize();
    }
    /** creates new DAO to Redis database */
    public DaoRedisBase(String redisHost, String redisPort, Agent agent) {
        super(DaoParams.redisParams(redisHost, redisPort), agent);
        this.redisHost = resolve(redisHost);
        this.redisPort = resolveToInt(redisPort, 6379);;
        onInitialize();
    }
    /** count objects in this agentable object including this object */
    public long countObjectsAgentable() {
        return 3L;
    }
    /** read configuration and re-initialize this component */
    public boolean componentReinitialize() {
        // TODO: implement reinitialization
        return true;
    }
    /** returns true if DAO is connected */
    public boolean isConnected() {
        try {
            return jedis != null && jedis.isConnected();
        } catch (Exception ex) {
            return false;
        }
    }
    /** test DAO and returns items */
    public AdvancedMap testDao() {
        AdvancedMap status = AdvancedMap.create(this);
        return status.appendMap(Map.of("isConnected", isConnected(),
                "url", getUrl(),
                "className", this.getClass().getName(),
                "structures", getDaoStructures()));
    }
    /** get URL of this DAO */
    public String getUrl() {
        return redisHost + ":" + redisPort;
    }
    /** initialize Redis client */
    public void onInitialize() {
        try {
            log.info("Connecting to DAO Redis, GUID: " + getGuid() + ", redisHost: " + redisHost + ", redisPort: " + redisPort);
            jedis = new Jedis(redisHost, redisPort);
            log.info("Connected to DAO Redis, info: " + jedis.clientInfo() + ", connected: " + jedis.isConnected());
        } catch (Exception ex) {
            log.info("Cannot connect to Redis at redisHost:" + redisHost + ", redisPort: " + redisPort + ", reason: " + ex.getMessage(), ex);
            addIssueToAgent("onInitialize", ex);
        }
    }
    /** get all structures */
    public Collection<String> getDaoStructures() {
        return List.of();
    }
    /** create new structure for this DAO */
    public AdvancedMap createStructure(DaoModel model) {
        AdvancedMap status = AdvancedMap.create(this);
        try {
            return status.withStatus("OK");
        } catch (Exception ex) {
            return status.exception(ex);
        }
    }
    /** insert full objects to given structure */
    public <X extends BaseRow> int executeInsertRowsForModel(DaoModel<X> model, List<X> objs) {
        // TODO: implement inserting object into model structure
        return 0;
    }

    /** close current Redis DAO */
    protected void onClose() {
        closeDao();
    }
    public boolean closeDao() {
        jedis.close();
        return true;
    }

}
