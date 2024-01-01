package com.distsystem.interfaces;

import com.distsystem.api.DaoModel;
import com.distsystem.api.DaoParams;
import com.distsystem.api.dtos.DistAgentDaoRow;
import com.distsystem.api.info.AgentDaoInfo;
import com.distsystem.api.info.AgentDaoSimpleInfo;
import com.distsystem.utils.AdvancedMap;

import java.util.Collection;
import java.util.Map;

/** basic interface for any DAO - Data Access Object.
 * */
public interface Dao {

    /** returns true if DAO is connected */
    boolean isConnected();
    /** test DAO and returns items */
    AdvancedMap testDao();
    /** */
    AdvancedMap reinitializeDao();
    /** get initialization parameters */
    DaoParams getParams();
    /** close current DAO */
    boolean closeDao();
    /** get info about DAO */
    AgentDaoInfo getInfo();
    /** get URL of this DAO */
    String getUrl();
    /** get all structures from DAO: tables, indices, topics, Document databases, ... */
    Collection<String> getDaoStructures();
    /** create new structure for this DAO */
    AdvancedMap createStructure(DaoModel model);
    /** check if given DAO structure is in underlying storage */
    boolean checkDaoStructure(String structureName);
    /** add component that is using this DAO */
    void usedByComponent(AgentComponent component);
    /** remove component from usage of this DAO */
    void notUsedByComponent(AgentComponent component);
    /** create row for DAO */
    DistAgentDaoRow toRow();
    /** get simple info */
    AgentDaoSimpleInfo getSimpleInfo();
    /** get unique ID of this DAO */
    String getGuid();

}
