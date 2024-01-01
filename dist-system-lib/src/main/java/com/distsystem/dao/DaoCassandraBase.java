package com.distsystem.dao;

import com.distsystem.api.*;
import com.distsystem.base.DaoBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentComponent;
import com.distsystem.interfaces.HttpCallable;
import com.distsystem.utils.AdvancedMap;
import com.distsystem.utils.DistUtils;
import com.distsystem.utils.HttpConnectionHelper;
import com.distsystem.utils.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/** base class for any Cassandra based DAO */
public class DaoCassandraBase extends DaoBase implements AgentComponent {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(DaoCassandraBase.class);

    /** URL to Elasticsearch REST API */
    private final String cassandraHost;
    private final String cassandraPort;

    /** creates new DAO to JDBC database */
    public DaoCassandraBase(DaoParams params, Agent agent) {
        super(params, agent);
        this.cassandraHost = resolve(params.getUrl());
        this.cassandraPort = resolve(params.getUser());
        onInitialize();
    }
    /** creates new DAO to Cassandra database */
    public DaoCassandraBase(String cassandraHost, String cassandraPort, Agent agent) {
        super(DaoParams.cassandraParams(cassandraHost, cassandraPort), agent);
        this.cassandraHost = resolve(cassandraHost);
        this.cassandraPort = resolve(cassandraPort);
        onInitialize();
    }
    /** count objects in this agentable object including this object */
    public long countObjectsAgentable() {
        return 2L;
    }
    /** read configuration and re-initialize this component */
    public boolean componentReinitialize() {
        // TODO: implement reinitialization
        return true;
    }
    /** returns true if DAO is connected */
    public boolean isConnected() {
        try {


            return true;
        } catch (Exception ex) {

            return false;
        }
    }
    /** test DAO and returns items */
    public AdvancedMap testDao() {
        return AdvancedMap.create(this).appendMap(Map.of("isConnected", isConnected(), "url", getUrl(), "className", this.getClass().getName()));
    }
    /** get URL of this DAO */
    public String getUrl() {
        return cassandraHost + ":" + cassandraPort;
    }

    /** */
    public void onInitialize() {
        try {
            //Cluster.builder().addContactPoint(node);
            log.info("Connecting to Cassandra, URL=" + getUrl());
            //CassandraConnector client = new CassandraConnector();
            //c//lient.connect("127.0.0.1", 9142);
            //this.session = client.getSession();
           // log.info("Connected to Elasticsearch, cluster nodes: " + cluInfo.size() + ", cluster info: " + cluInfo);
        } catch (Exception ex) {
            log.info("Cannot connect to Cassandra at URL:" + getUrl() + ", reason: " + ex.getMessage(), ex);
            addIssueToAgent("onInitialize", ex);
        }
    }
    /** get all indices */
    public Collection<String> getDaoStructures() {
        return List.of("");
    }

    /** create new structure for this DAO */
    public AdvancedMap createStructure(DaoModel model) {
        AdvancedMap status = AdvancedMap.create(this);
        try {
            // TODO: implement creating collection
            //createIndex(model.getTableName());
            return status.append("", "").withStatus("OK");
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
