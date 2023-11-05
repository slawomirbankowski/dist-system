package com.distsystem.base;

import com.distsystem.agent.AgentInstance;

import java.util.Map;

/** reader of configuration from external sources - to be overritten by implementation class for HTTP, Kafka, ... */
public abstract class ConfigReaderBase extends Agentable {

    /** */
    public ConfigReaderBase(AgentInstance agent) {
        super(agent);
    }
    /** read configuration from external sources */
    public abstract void readConfiguration();

    /** get info about config reader as map */
    public abstract Map<String, Object> getInfoMap();
}
