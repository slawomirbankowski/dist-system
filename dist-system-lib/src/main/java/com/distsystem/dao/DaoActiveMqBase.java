package com.distsystem.dao;

import com.distsystem.api.*;
import com.distsystem.base.DaoBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentComponent;

import com.distsystem.utils.AdvancedMap;
import jakarta.jms.*;
import jakarta.jms.Queue;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;


/** base class for any JDBC based DAO */
public class DaoActiveMqBase extends DaoBase implements AgentComponent {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(DaoActiveMqBase.class);
    /** ActiveMQ connection */
    private ActiveMQConnection connection;
    private String activeMqUrl; // "tcp://localhost:61616"

    /** creates new DAO to JDBC database */
    public DaoActiveMqBase(DaoParams params, Agent agent) {
        super(params, agent);
        this.activeMqUrl = resolve(params.getUrl());
        onInitialize();
    }
    /** creates new DAO to JDBC database */
    public DaoActiveMqBase(String activeMqUrl, Agent agent) {
        super(DaoParams.activeMqParams(activeMqUrl), agent);
        this.activeMqUrl = resolve(activeMqUrl);
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

    /** get URL of this DAO */
    public String getUrl() {
        return activeMqUrl;
    }
    /** returns true if DAO is connected */
    public boolean isConnected() {
        try {
            Session session = connection.createSession();
            //session.getDe
            session.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /** test DAO and returns items */
    public AdvancedMap testDao() {

        return AdvancedMap.create(this)
                .append("", "", "", "", "", "")
                .appendMap(Map.of("isConnected", isConnected(), "url", getUrl(), "className", this.getClass().getName()));
    }
    /** initialize connection to ActiveMQ */
    public void onInitialize() {
        try {
            connection = ActiveMQConnection.makeConnection(activeMqUrl);
            //ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(activeMqUrl);
            //connection = connectionFactory.createConnection();
            connection.start();
            ConnectionMetaData metadata = connection.getMetaData();
            log.info("Connected to ActiveMQ, url: " + activeMqUrl+ ", getClientID: " + connection.getClientID() + ", JMSProviderName: " + metadata.getJMSProviderName() + ", JMSVersion: " + metadata.getJMSVersion());
        } catch (Exception ex) {
            log.info("Cannot connect to ActiveMQ at URL:" + activeMqUrl + ", reason: " + ex.getMessage(), ex);
            addIssueToAgent("onInitialize", ex);
        }
    }
    /** get all topics and queues */
    public Collection<String> getDaoStructures() {
        try {
            List<String> structures = new LinkedList<>();
            connection.getDestinationSource().getQueues().forEach(q -> structures.add(q.getPhysicalName()));
            connection.getDestinationSource().getTopics().forEach(t -> structures.add(t.getPhysicalName()));
            return structures;
        } catch (Exception ex) {
            return List.of();
        }
    }
    /** create new structure for this DAO */
    public AdvancedMap createStructure(DaoModel model) {
        AdvancedMap status = AdvancedMap.create(this);
        try {
            log.info("Create new topic for DAO ActiveMQ, guid: " + getGuid() + ", name: " + model.getTableName());
            Session session = connection.createSession();
            session.createQueue(model.getTableName());
            session.close();
            return status.append("type", "topic").withStatus("OK");
        } catch (Exception ex) {
            log.warn("Cannot create new topic for DAO ActiveMQ, reason: " + ex.getMessage(), ex);
            return status.exception(ex);
        }
    }
    /** insert full objects to given structure */
    public <X extends BaseRow> AdvancedMap executeInsertRowsForModel(DaoModel<X> model, List<X> objs) {
        AdvancedMap status = AdvancedMap.create(this);
        // TODO: implement inserting object into model structure
        try {
            Session session = connection.createSession();
            Queue queue = session.createQueue(model.getTableName());
            MessageProducer producer = session.createProducer(queue);
            objs.forEach(o -> {
                try {
                    MapMessage mm = session.createMapMessage();
                    mm.setObject("", "");
                    producer.send(queue, mm);
                } catch (Exception ex) {

                }
            });
            session.close();
            return status;
        } catch (Exception ex) {
            log.warn("Cannot insert tows into topic for DAO ActiveMQ, reason: " + ex.getMessage(), ex);
            return status.exception(ex);
        }
    }

    /** close current ActiveMQ DAO */
    protected void onClose() {
        closeDao();
    }
    public boolean closeDao() {
        try {
            connection.stop();
            return true;
        } catch (Exception ex) {
            log.warn("Cannot close ActiveMQ DAO, agentGuid: " + getParentAgentGuid() + ", GUID: " + getGuid() + ", reason: " + ex.getMessage(), ex);
            addIssueToAgent("closeDao", ex);
            return false;
        }
    }

}
