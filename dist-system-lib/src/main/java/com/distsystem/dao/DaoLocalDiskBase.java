package com.distsystem.dao;

import com.distsystem.api.BaseRow;
import com.distsystem.api.DaoModel;
import com.distsystem.api.DaoParams;
import com.distsystem.base.DaoBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentComponent;
import com.distsystem.utils.AdvancedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;


/** base class for any JDBC based DAO */
public class DaoLocalDiskBase extends DaoBase implements AgentComponent {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(DaoLocalDiskBase.class);
    /** ActiveMQ connection */
    private String basePath;
    private File baseFolder;

    /** creates new DAO to JDBC database */
    public DaoLocalDiskBase(DaoParams params, Agent agent) {
        super(params, agent);
        this.basePath = resolve(params.getUrl());
        this.baseFolder = new java.io.File(basePath);

        //baseFolder.createNewFile();
        onInitialize();
    }
    /** creates new DAO to JDBC database */
    public DaoLocalDiskBase(String basePath, Agent agent) {
        super(DaoParams.activeMqParams(basePath), agent);

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
        return basePath;
    }
    /** returns true if DAO is connected */
    public boolean isConnected() {
        try {
            return baseFolder.exists();
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
    /** initialize connection to LocalDisk */
    public void onInitialize() {
        try {

            log.info("Connected to LocalDisk DAO, url: " + basePath);
        } catch (Exception ex) {
            log.info("Cannot connect to LocalDisk at DAO:" + basePath + ", reason: " + ex.getMessage(), ex);
            addIssueToAgent("onInitialize", ex);
        }
    }
    /** get all files with extension .table */
    public Collection<String> getDaoStructures() {
        try {
            String[] files = baseFolder.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".table");
                }
            });
            return Arrays.stream(files).toList();
        } catch (Exception ex) {
            return List.of();
        }
    }
    /** create new structure for this DAO */
    public AdvancedMap createStructure(DaoModel model) {
        AdvancedMap status = AdvancedMap.create(this);
        try {
            log.info("Create new file for DAO LocalDisk, guid: " + getGuid() + ", name: " + model.getTableName());

            return status.append("type", "topic").withStatus("OK");
        } catch (Exception ex) {
            log.warn("Cannot create new file for DAO LocalDisk, reason: " + ex.getMessage(), ex);
            return status.exception(ex);
        }
    }
    /** insert full objects to given structure */
    public <X extends BaseRow> AdvancedMap executeInsertRowsForModel(DaoModel<X> model, List<X> objs) {
        AdvancedMap status = AdvancedMap.create(this);
        // TODO: implement inserting object into model structure
        try {

            return status;
        } catch (Exception ex) {
            log.warn("Cannot create new topic for DAO LocalDisk, reason: " + ex.getMessage(), ex);
            return status.exception(ex);
        }
    }

    /** close current ActiveMQ DAO */
    protected void onClose() {
        closeDao();
    }
    public boolean closeDao() {
        try {
            // connection.stop();

            return true;
        } catch (Exception ex) {
            log.warn("Cannot close LocalDisk DAO, agentGuid: " + getParentAgentGuid() + ", GUID: " + getGuid() + ", reason: " + ex.getMessage(), ex);
            addIssueToAgent("closeDao", ex);
            return false;
        }
    }

}
