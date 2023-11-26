package com.distsystem.agent.configreaders;

import com.distsystem.api.DistConfig;
import com.distsystem.api.ServiceObjectParams;
import com.distsystem.base.ConfigReaderBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/** */
public class ConfigReaderHttp extends ConfigReaderBase {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(ConfigReaderHttp.class);
    /** */
    public ConfigReaderHttp(ServiceObjectParams params) {
        super(params);
        initialize();
    }

    /** count objects in this agentable object including this object */
    public long countObjectsAgentable() {
        return 2L;
    }
    public void initialize() {
        try {
            String url = getConfigProperty(DistConfig.URL, "");
            if (!url.isEmpty()) {
                String headersStr = getConfigProperty(DistConfig.HEADERS, "");
                log.info("Reading external configuration for agent from http, agent: " + parentAgent.getAgentGuid() + ", URL: " + url);
                // TODO: implement reading configuration from HTTP source
            }
        } catch (Exception ex) {
        }
    }
    /** get info about config reader as map */
    public Map<String, Object> getInfoMap() {
        return Map.of();
    }
    /** read configuration from HTTP */
    public void readConfiguration() {
    }
    /** close this reader */
    protected void onClose() {
    }

}
