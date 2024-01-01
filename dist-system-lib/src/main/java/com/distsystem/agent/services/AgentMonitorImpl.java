package com.distsystem.agent.services;

import com.distsystem.api.DistConfig;
import com.distsystem.api.DistConfigBucket;
import com.distsystem.api.ServiceObjectParams;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.base.AuthStorageBase;
import com.distsystem.base.MonitorBase;
import com.distsystem.base.ServiceBase;
import com.distsystem.interfaces.*;
import com.distsystem.utils.AdvancedMap;
import com.distsystem.utils.DistWebApiProcessor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** service for monitors */
public class AgentMonitorImpl extends ServiceBase implements AgentMonitor {

    /** all initialized monitors */
    private List<MonitorBase> monitors = new LinkedList<>();

    /** creates new monitors service */
    public AgentMonitorImpl(Agent parentAgent) {
        super(parentAgent);
        parentAgent.getServices().registerService(this);
    }

    /** count objects in this agentable object including this object */
    public long countObjectsService() {
        // TODO: calculate counts of objects
        return 2L;
    }

    @Override
    public DistServiceType getServiceType() {
        return DistServiceType.monitor;
    }
    /** get description of this service */
    public String getServiceDescription() {
        return "Monitoring objects for services, servers, applications, networking, databases, and so on...";
    }

    /** update configuration of this Service */
    public void updateConfig(DistConfig newCfg) {
        // TODO: update configuration of this service
    }

    /** additional web API endpoints */
    protected DistWebApiProcessor additionalWebApiProcessor() {
        return new DistWebApiProcessor(getServiceType())
                .addHandlerGet("monitor-count", (m, req) -> req.responseOkText(""+monitors.size()))
                .addHandlerGet("monitor-names", (m, req) -> req.responseOkJsonSerialize(getMonitorNames()))
                .addHandlerGet("monitor-guids", (m, req) -> req.responseOkJsonSerialize(getMonitorGuids()))
                .addHandlerPost("monitor", (m, req) -> req.responseOkJsonSerialize(createMonitor(req.getContentAsJsonAdvancedMap())))
                .addHandlerGet("info", (m, req) -> req.responseOkJsonSerialize(getInfo()));
    }
    /** create monitor object from Map */
    public AdvancedMap createMonitor(AdvancedMap monitorParams) {
        String configType = "HTTP";
        String configInstance = "monitorName";
        Map<String, String> configValues = Map.of();
        log.info("Create new monitor from parameters, agent:  " + getParentAgentGuid() + ", monitorType: " + configType + ", monitorName: " + configInstance + ", current monitors: " + monitors.size());
        DistConfigBucket bucket = DistConfigBucket.createBucket(this, "OBJECT", configType, configInstance, configValues);
        return createMonitor(bucket);
    }
    /** create monitor object from bucket */
    public AdvancedMap createMonitor(DistConfigBucket bucket) {
        AdvancedMap status = AdvancedMap.create(this);
        String readerClass = DistConfig.AGENT_MONITOR_OBJECT_CLASS_MAP.get(bucket.getKey().getConfigType());
        log.info("Create new Monitor for type: " + bucket.getKey().getConfigType() + ", class: " + readerClass);
        createEvent("createMonitor");
        try {

            String monitorName = bucket.getKey().getConfigInstance();


            openCount.incrementAndGet();
            var params = ServiceObjectParams.create(parentAgent, this, readerClass, bucket);
            log.info("Try to initialize Monitor for agent: " + parentAgent.getAgentGuid() + ", class: " + readerClass + ", bucket key: " + bucket.getKey() + ", params: " + params);
            MonitorBase monitorObj = (MonitorBase)Class.forName(readerClass)
                    .getConstructor(ServiceObjectParams.class)
                    .newInstance(params);
            monitors.add(monitorObj);
            return status.join(monitorObj.toStatusMap());
        } catch (Exception ex) {
            log.info("Cannot initialize auth storage for agent: "  + parentAgent.getAgentGuid() + ", class: " + readerClass + ", reason: " + ex.getMessage(), ex);
            addIssueToAgent("createAuthStorage", ex);
            return status.exception(ex);
        }
    }
    /** get names of monitors */
    public List<String> getMonitorNames() {
        return monitors.stream().map(m -> m.getMonitorName()).toList();
    }
    /** get GUIDs of monitors */
    public List<String> getMonitorGuids() {
        return monitors.stream().map(m -> m.getGuid()).toList();
    }
    /** get monitor by name */
    public Optional<AgentMonitorObject> getMonitorByName(String monitorName) {
        return monitors.stream().filter(m -> m.getMonitorName().equals(monitorName)).map(x -> (AgentMonitorObject)x).findFirst();
    }
    /** get monitor by GUID */
    public Optional<AgentMonitorObject> getMonitorByGuid(String guid) {
        return monitors.stream().filter(m -> m.getGuid().equals(guid)).map(x -> (AgentMonitorObject)x).findFirst();
    }


    @Override
    protected void onClose() {
    }

    /** read configuration and re-initialize this component */
    protected boolean onReinitialize() {
        // nothing to be done here
        return true;
    }
}
