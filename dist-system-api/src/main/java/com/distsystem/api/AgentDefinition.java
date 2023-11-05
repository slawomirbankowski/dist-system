package com.distsystem.api;

import java.util.Map;
import java.util.Properties;

/** simple definition of agent to be serialized */
public class AgentDefinition {

    /** GUID of Agent to be found*/
    private String agentGuid = "";
    /** properties */
    private Map<String, String> properties = Map.of();

    public AgentDefinition() {
    }

    public String getAgentGuid() {
        return agentGuid;
    }
    public void setAgentGuid(String agentGuid) {
        this.agentGuid = agentGuid;
    }
    public Map<String, String> getProperties() {
        return properties;
    }
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
    /** change this definition into DistConfig */
    public DistConfig toDistConfig() {
        DistConfig cfg = DistConfig.buildEmptyConfig();
        //cfg.setMap(properties);
        return cfg;
    }

    @Override
    public String toString() {
        return "PING,createdServers=";
    }

}
