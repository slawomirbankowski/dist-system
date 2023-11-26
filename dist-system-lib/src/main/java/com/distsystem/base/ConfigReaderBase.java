package com.distsystem.base;

import com.distsystem.agent.AgentInstance;
import com.distsystem.api.ServiceObjectParams;
import com.distsystem.api.enums.DistComponentType;
import com.distsystem.interfaces.Agent;

import java.util.Map;

/** reader of configuration from external sources - to be overritten by implementation class for HTTP, Kafka, ... */
public abstract class ConfigReaderBase extends ServiceObjectBase {

    /** */
    public ConfigReaderBase(ServiceObjectParams params) {
        super(params);
    }
    /** read configuration from external sources */
    public abstract void readConfiguration();

    /** get info about config reader as map */
    public abstract Map<String, Object> getInfoMap();

}
