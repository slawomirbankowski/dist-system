package com.distsystem.interfaces;

import com.distsystem.api.DaoParams;
import com.distsystem.api.dtos.DistAgentDaoRow;
import com.distsystem.api.info.AgentDaoInfo;
import com.distsystem.api.info.AgentDaoSimpleInfo;

import java.util.Map;

/** basic interface for any DAO - Data Access Object.
 * */
public interface Dao {

    /** returns true if DAO is connected */
    boolean isConnected();
    /** test DAO and returns items */
    Map<String, Object> testDao();
    /** */
    Map<String, Object> reinitializeDao();
    /** get initialization parameters */
    DaoParams getParams();
    /** close current DAO */
    boolean closeDao();
    /** get info about DAO */
    AgentDaoInfo getInfo();
    /** create row for DAO */
    DistAgentDaoRow toRow();
    /** get simple info */
    AgentDaoSimpleInfo getSimpleInfo();
    /** get unique ID of this DAO */
    String getGuid();

}
