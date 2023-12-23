package com.distsystem.interfaces;

import com.distsystem.api.info.AgentConfigReaderInfo;

import java.util.List;

/** interface for class to read configuration from external sources like JDBC, HTTP, */
public interface AgentConfigReader extends DistService {

    /** get info object for this reader */
    AgentConfigReaderInfo getInfo();
    /** get number of external config readers currently available */
    int getReadersCount();
    /** read configuration now from all readers*/
    void readConfig();
    /** get all keys for config readers */
    List<String> getReaderKeys();

}
