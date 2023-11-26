package com.distsystem.agent.services;

import com.distsystem.api.*;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.api.info.AgentConfigReaderInfo;
import com.distsystem.base.ConfigReaderBase;
import com.distsystem.base.ServiceBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentConfigReader;
import com.distsystem.utils.DistWebApiProcessor;
import com.distsystem.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

/** external configuration reader from different sources to feed agent services with fresh configuration */
public class AgentConfigImpl extends ServiceBase implements AgentConfigReader {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(AgentConfigImpl.class);
    /** all external readers */
    private final java.util.concurrent.ConcurrentHashMap<String, ConfigReaderBase> configReaders = new java.util.concurrent.ConcurrentHashMap<>();
    /** reader object counter */
    private final AtomicLong readerObjectCount = new AtomicLong();

    /** created new config reader service */
    public AgentConfigImpl(Agent parentAgent) {
        super(parentAgent);
        parentAgent.getServices().registerService(this);
    }

    /** count objects in this agentable object including this object */
    public long countObjectsService() {
        return 2L + configReaders.size()*2L;
    }

    /** get type of service: cache, measure, report, flow, space, ... */
    public DistServiceType getServiceType() {
        return DistServiceType.config;
    }

    /** update configuration of this Service to add registrations, services, servers, ... */
    public void updateConfig(DistConfig newCfg) {
    }

    /** additional web API endpoints */
    protected DistWebApiProcessor additionalWebApiProcessor() {
        return new DistWebApiProcessor(getServiceType())
                .addHandlerGet("info", (m, req) -> req.responseOkJsonSerialize(getInfo()))
                .addHandlerGet("keys", (m, req) -> req.responseOkJsonSerialize(getReaderKeys()))
                .addHandlerGet("resolve", (m, req) -> req.responseOkText(resolveWithManager(req.getParamOne())))
                .addHandlerGet("property", (m, req) -> req.responseOkText(getProperty(req.getParamOne())))
                .addHandlerPost("property", (m, req) -> req.responseOkText(setProperty(req.getParamOne(), req.getParamTwo())))
                .addHandlerPost("properties", (m, req) -> req.responseOkJsonSerialize(setProperties(req.getContentAsString())))
                .addHandlerGet("properties", (m, req) -> req.responseOkJsonSerialize(getConfig().getHashMap(false)))
                .addHandlerPost("read-config", (m, req) -> req.responseOkJsonSerialize(readConfigWithInfo()))
                .addHandlerDelete("reader", (m, req) -> req.responseOkJsonSerialize(removeConfigReader(req.getParamOne())))
                .addHandlerGet("reader", (m, req) -> req.responseOkJsonSerialize(infoConfigReader(req.getParamOne())))
                .addHandlerPost("reader", (m, req) -> req.responseOkJsonSerialize(createConfigReader(req.getContentAsString())))
                .addHandlerGet("readers", (m, req) -> req.responseOkJsonSerialize(configReaders.values().stream().map(r -> r.getInfoMap()).toList()));
    }

    /** get info object for this reader */
    public AgentConfigReaderInfo getInfo() {
        return new AgentConfigReaderInfo(readCount.get(), readerObjectCount.get(), getReadersCount(), getReaderKeys());
    }
    /** resolve given value info full value */
    protected String resolveWithManager(String value) {
        return getConfig().getResolverManager().resolve(value);
    }
    /** set single property and recalculate */
    protected String setProperty(String key, String value) {
        createEvent("setProperty");
        Properties p = new Properties();
        p.setProperty(key, value);
        getConfig().mergeWithConfig(DistConfig.buildConfig(p));
        return getConfig().getProperty(key, "");
    }
    /** set many properties using */
    protected Map<String, String> setProperties(String bodyJson) {
        createEvent("setProperties");
        Map<String, String> props = JsonUtils.deserializeToMap(bodyJson);
        getConfig().mergeWithConfig(DistConfig.buildConfigFromMap(props));
        return props;
    }
    /** remove and stop confiugration reader by key */
    public Map<String, String> removeConfigReader(String readerKey) {
        createEvent("removeConfigReader");
        ConfigReaderBase reader = configReaders.remove(readerKey);
        if (reader != null) {
            reader.close();
            return Map.of("exists", "true", "removed", "true", "readerKey", readerKey, "guid", reader.getGuid());
        } else {
            return Map.of("exists", "false");
        }
    }
    /** create new configuration reader */
    public Map<String, Object> infoConfigReader(String readerKey) {
        createEvent("infoConfigReader");
        ConfigReaderBase reader = configReaders.get(readerKey);
        if (reader != null) {
            return reader.getInfoMap();
        } else {
            return Map.of("exists", "false");
        }
    }
    /** create new configuration reader */
    public Map<String, String> createConfigReader(String bodyJson) {
        createEvent("createConfigReader");
        Map<String, String> props = JsonUtils.deserializeToMap(bodyJson);
        // TODO: create config reader based on bodyJson - add these into DistConfig and create ConfigBucket
        //DistConfigBucket b = new DistConfigBucket();
        return Map.of("removed", "false");
    }
    /** get configuration property */
    protected String getProperty(String key) {
        return getConfig().getProperty(key, "");
    }
    /** change values in configuration bucket */
    public void initializeConfigBucket(DistConfigBucket bucket) {
        // TODO: initialize or deinitialize
        createEvent("initializeConfigBucket");

        initializeReader(bucket);
    }

    /** reinitialize */
    protected boolean onReinitialize() {
        createEvent("onReinitialize");
        log.info("Initialization for ConfigReader to read from external sources for agent: " + parentAgent.getAgentGuid());
        registerConfigGroup(DistConfig.AGENT_CONFIGREADER_OBJECT);
        parentAgent.getTimers().cancelTimer("AgentConfigReader");
        long timerMs = parentAgent.getConfig().getPropertyAsLong(DistConfig.AGENT_CONFIGREADER_TIMER, 60000L);
        parentAgent.getTimers().setUpTimer("AgentConfigReader", timerMs, timerMs, x -> {
            readConfig();
            return true;
        });
        initialized = true;
        return true;
    }

    /** get number of external config readers currently available */
    public int getReadersCount() {
        return configReaders.size();
    }
    /** get all keys for config readers */
    public List<String> getReaderKeys() {
        return configReaders.entrySet().stream().map(x -> x.getKey()).toList();
    }
    /** read configuration now from all readers*/
    public void readConfig() {
        checkCount.incrementAndGet();
        createEvent("readConfig");
        synchronized (configReaders) {
            configReaders.values().forEach(cr -> {
                try {
                    readCount.incrementAndGet();
                    cr.readConfiguration();
                } catch (Exception ex) {
                    log.warn("Cannot read configuration from external config reader, agent: " + parentAgent.getAgentGuid() + ", reader: " + cr.toString());
                    addIssueToAgent("readConfig", ex);
                }
            });
        }
    }
    /** read configuration and get info */
    public AgentConfigReaderInfo readConfigWithInfo() {
        readConfig();
        return getInfo();
    }
    /** initialize new config reader for configuration bucket with values */
    private void initializeReader(DistConfigBucket bucket) {
        String className = DistConfig.AGENT_CONFIGREADER_MAP.get(bucket.getKey().getConfigType());
        try {
            createEvent("initializeReader");
            log.info("Try to initialize external configuration reader for agent: " + parentAgent.getAgentGuid() + ", class: " + className + ", bucket key: " + bucket.getKey());
            ConfigReaderBase reader = (ConfigReaderBase)Class.forName(className)
                    .getConstructor(ServiceObjectParams.class)
                    .newInstance(ServiceObjectParams.create(parentAgent, this, className, bucket));
            synchronized (configReaders) {
                readerObjectCount.incrementAndGet();
                try {
                    reader.readConfiguration();
                } catch (Exception ex) {
                    log.warn("Cannot read configuration from external config reader, agent: " + parentAgent.getAgentGuid() + ", reader: " + reader.toString());
                    addIssueToAgent("initializeReader", ex);
                }
                configReaders.put(bucket.getKey().toString(), reader);
            }
            openCount.incrementAndGet();
        } catch (Exception ex) {
            log.info("Cannot initialize external configuration reader for agent: "  + parentAgent.getAgentGuid() + ", class: " + className + ", reason: " + ex.getMessage(), ex);
            addIssueToAgent("initializeReader", ex);
        }
    }

    /** close external readers */
    protected void onClose() {
        createEvent("onClose");
        synchronized (configReaders) {
            configReaders.entrySet().forEach(r -> {
                r.getValue().close();
            });
        }
    }
}
