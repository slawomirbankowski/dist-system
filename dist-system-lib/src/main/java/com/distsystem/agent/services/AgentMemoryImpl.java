package com.distsystem.agent.services;

import com.distsystem.api.DistConfig;
import com.distsystem.api.dtos.DistAgentMemoryRow;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.base.ServiceBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentMeasure;
import com.distsystem.utils.DistUtils;
import com.distsystem.utils.DistWebApiProcessor;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicLong;

/** service for distributes measures */
public class AgentMemoryImpl extends ServiceBase implements AgentMeasure {

    private AtomicLong memorySeq = new AtomicLong();
    /** creates new memory service */
    public AgentMemoryImpl(Agent parentAgent) {
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
        return DistServiceType.memory;
    }
    /** get description of this service */
    public String getServiceDescription() {
        return "Check memory consumption by agent";
    }

    /** additional web API endpoints */
    protected DistWebApiProcessor additionalWebApiProcessor() {
        return new DistWebApiProcessor(getServiceType())
                .addHandlerPost("memories", (m, req) -> req.responseOkJsonSerialize(new LinkedList<String>()));
    }
    public DistAgentMemoryRow createMemoryRow() {
        Runtime rt = java.lang.Runtime.getRuntime();
        long freeMemory = rt.freeMemory();
        long totalMemory = rt.totalMemory();
        long maxMemory = rt.maxMemory();
        return new DistAgentMemoryRow(parentAgent.getAgentGuid(),
                System.currentTimeMillis()- parentAgent.getAgentStartTime(), memorySeq.incrementAndGet(),
                maxMemory, totalMemory, freeMemory, totalMemory-freeMemory, parentAgent.countAgentObjects());
    }
    /** update configuration of this Service */
    public void updateConfig(DistConfig newCfg) {
        // TODO: update configuration of this service
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
