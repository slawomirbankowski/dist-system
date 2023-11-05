package com.distsystem.interfaces;

import com.distsystem.api.DaoParams;
import com.distsystem.api.info.AgentDaoInfo;
import com.distsystem.api.info.AgentDaoSimpleInfo;

/** basic interface for any DAO - Data Access Object.
 * */
public interface Dao {

    /** returns true if DAO is connected */
    boolean isConnected();
    /** get initialization parameters */
    DaoParams getParams();
    /** close current DAO */
    boolean closeDao();
    /** get info about DAO */
    AgentDaoInfo getInfo();
    /** get simple info */
    AgentDaoSimpleInfo getSimpleInfo();
    /** get unique ID of this DAO */
    String getGuid();

}
