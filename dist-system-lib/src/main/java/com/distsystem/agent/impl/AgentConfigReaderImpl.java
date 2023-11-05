package com.distsystem.agent.impl;

import com.distsystem.agent.AgentInstance;
import com.distsystem.api.DistConfig;
import com.distsystem.api.DistConfigBucket;
import com.distsystem.api.DistMessage;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.api.info.AgentConfigReaderInfo;
import com.distsystem.base.ConfigReaderBase;
import com.distsystem.base.ServiceBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentConfigReader;
import com.distsystem.utils.DistWebApiProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/** external configuration reader from different sources */
public class AgentConfigReaderImpl extends ServiceBase implements AgentConfigReader {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(AgentConfigReaderImpl.class);
    /** all external readers */
    private final java.util.concurrent.ConcurrentHashMap<String, ConfigReaderBase> configReaders = new java.util.concurrent.ConcurrentHashMap<>();
    /** read counter */
    private final AtomicLong readCount = new AtomicLong();
    /** reader object counter */
    private final AtomicLong readerObjectCount = new AtomicLong();

    /** */
    public AgentConfigReaderImpl(Agent parentAgent) {
        super(parentAgent);
        parentAgent.getAgentServices().registerService(this);
        parentAgent.getConfig().registerConfigGroup(DistConfig.AGENT_CONFIGREADER_OBJECT);
        parentAgent.getConfig().addListener(DistConfig.AGENT_CONFIGREADER_TIMER, cfg -> {
            return true;
        });
    }

    /** get type of service: cache, measure, report, flow, space, ... */
    public DistServiceType getServiceType() {
        return DistServiceType.configreader;
    }
    /** process message, returns message with status */
    public DistMessage processMessage(DistMessage msg) {
        return msg.notSupported();
    }

    /** update configuration of this Service to add registrations, services, servers, ... */
    public void updateConfig(DistConfig newCfg) {
    }

    /** additional web API endpoints */
    protected DistWebApiProcessor additionalWebApiProcessor() {
        return new DistWebApiProcessor(getServiceType())
                .addHandlerGet("info", (m, req) -> req.responseOkJsonSerialize(getInfo()))
                .addHandlerGet("keys", (m, req) -> req.responseOkJsonSerialize(getReaderKeys()))
                .addHandlerGet("readers", (m, req) -> req.responseOkJsonSerialize(configReaders.values().stream().map(r -> r.getInfoMap()).toList()));
    }

    /** get info object for this reader */
    public AgentConfigReaderInfo getInfo() {
        return new AgentConfigReaderInfo(readCount.get(), readerObjectCount.get(), getReadersCount(), getReaderKeys());
    }
    /** */
    public void initializeConfigBucket(DistConfigBucket bucket) {

    }
    @Override
    public boolean reinitialize() {
        log.info("Initialization for ConfigReader to read from external sources for agent: " + parentAgent.getAgentGuid());
        parentAgent.getAgentTimers().cancelTimer("AgentConfigReader");
        parentAgent.getAgentTimers().setUpTimer("AgentConfigReader", 60000L, 60000L, x -> {
            readConfig();
            return true;
        });


        // TODO: create configuration readers
        if (parentAgent.getConfig().hasProperty(DistConfig.AGENT_CONFIGREADER_OBJECT_HTTP_URL)) {
            initializeReader("com.distsystem.agent.impl.configreaders.ConfigReaderHttp");
        }
        if (parentAgent.getConfig().hasProperty(DistConfig.AGENT_CONFIGREADER_OBJECT_JDBC_URL)) {
            initializeReader("com.distsystem.agent.impl.configreaders.ConfigReaderJdbc");
        }

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
        synchronized (configReaders) {
            configReaders.values().forEach(cr -> {
                readCount.incrementAndGet();
                try {
                    cr.readConfiguration();
                } catch (Exception ex) {
                    log.warn("Cannot read configuration from external config reader, agent: " + parentAgent.getAgentGuid() + ", reader: " + cr.toString());
                    addIssueToAgent("readConfig", ex);
                }
            });
        }
    }
    /**  */
    private void initializeReader(String readerClass) {
        try {
            log.info("Try to initialize external configuration reader for agent: " + parentAgent.getAgentGuid() + ", class: " + readerClass);
            ConfigReaderBase reader = (ConfigReaderBase)Class.forName(readerClass)
                    .getConstructor(AgentInstance.class)
                    .newInstance(parentAgent);
            synchronized (configReaders) {
                readerObjectCount.incrementAndGet();
                try {
                    reader.readConfiguration();
                } catch (Exception ex) {
                    log.warn("Cannot read configuration from external config reader, agent: " + parentAgent.getAgentGuid() + ", reader: " + reader.toString());
                    addIssueToAgent("initializeReader", ex);
                }
                configReaders.put(readerClass, reader);
            }
        } catch (Exception ex) {
            log.info("Cannot initialize external configuration reader for agent: "  + parentAgent.getAgentGuid() + ", class: " + readerClass + ", reason: " + ex.getMessage(), ex);
            addIssueToAgent("initializeReader", ex);
        }
    }

    /** close external readers */
    protected void onClose() {
        synchronized (configReaders) {
            configReaders.entrySet().forEach(r -> {
                r.getValue().close();
            });
        }
    }
}
