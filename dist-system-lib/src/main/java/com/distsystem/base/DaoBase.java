package com.distsystem.base;

import com.distsystem.api.DaoParams;
import com.distsystem.api.dtos.DistAgentDaoRow;
import com.distsystem.api.enums.DistComponentType;
import com.distsystem.api.info.AgentDaoInfo;
import com.distsystem.api.info.AgentDaoSimpleInfo;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentComponent;
import com.distsystem.interfaces.Dao;
import com.distsystem.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/** BAse class for any DAO connection to any storage: JDBC, Elasticsearch, Kafka, Redis, MongoDB, Cassandra and others... */
public abstract class DaoBase extends AgentableBase implements Dao, AgentComponent {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(DaoBase.class);

    /** parameters for this DAO */
    protected final DaoParams params;

    /** all components that are using this DAO */
    protected Map<String, AgentComponent> components = new HashMap<>();

    /** creates new DAO to JDBC database */
    public DaoBase(DaoParams params, Agent agent) {
        super(agent);
        parentAgent.addComponent(this);
        this.params = params;
    }
    /** reinitialize this DAO and test it */
    public Map<String, Object> reinitializeDao() {
        componentReinitialize();
        return testDao();
    }
    /** get unique ID of this DAO */
    public String getGuid() {
        return params.getKey();
    }
    /** get type of this component */
    public DistComponentType getComponentType() {
        return DistComponentType.dao;
    }
    /** get initialization parameters */
    public DaoParams getParams() {
        return params;
    }
    /** get simple info */
    public AgentDaoSimpleInfo getSimpleInfo() {
        return new AgentDaoSimpleInfo(params.getKey(), params.getDaoType().name(), getUrl());
    }
    /** add component that is using this DAO */
    public void usedByComponent(AgentComponent component) {
        components.put(component.getGuid(), component);
        log.info("Added component that is using DAO, DAO GUID: " + getGuid() + ", URL: " + getUrl() + ", component: " + component.getComponentType() + ", component GUID: " + component.getGuid() + ", current components: " + components.size());
    }
    /** */
    public void notUsedByComponent(AgentComponent component) {
        components.remove(component.getGuid());
    }
    /** get all structures from DAO: tables, indices, topics, Document databases, ... */
    public abstract Collection<String> getDaoStructures();
    /** check if given DAO structure is in underlying storage */
    public boolean checkDaoStructure(String structureName) {
        return getDaoStructures().contains(structureName);
    }

    /** get URL of this DAO */
    public abstract String getUrl();
    /** get info about DAO */
    public AgentDaoInfo getInfo() {
        return new AgentDaoInfo(createDate, params.getKey(), params.getDaoType(), getUrl(), isConnected(), getDaoStructures());
    }
    /** create row for DAO */
    public DistAgentDaoRow toRow() {
        return new DistAgentDaoRow(parentAgent.getAgentGuid(), guid, params.getDaoType().name(),
                getUrl(), JsonUtils.serialize(getDaoStructures()));
    }
    /** close this object */
    protected void onClose() {
        log.info("Closing object, nothing to be done here");
    }
}
