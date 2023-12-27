package com.distsystem.agent.services;

import com.distsystem.api.AgentEvent;
import com.distsystem.api.DistConfig;
import com.distsystem.api.dtos.DistAgentMemoryRow;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.api.info.AgentMemoryInfo;
import com.distsystem.api.info.AgentMemoryRowInfo;
import com.distsystem.base.ServiceBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentMeasure;
import com.distsystem.interfaces.AgentMemory;
import com.distsystem.utils.DistUtils;
import com.distsystem.utils.DistWebApiProcessor;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

/** service for storing and analyzing local memory used by Agent in JVM */
public class AgentMemoryImpl extends ServiceBase implements AgentMemory {

    /** memory sequence */
    private final AtomicLong memorySeq = new AtomicLong();
    /** queue of memory states */
    protected final Queue<AgentMemoryRowInfo> memoryStates = new LinkedList<>();
    protected AtomicLong countRow = new AtomicLong();
    protected AgentMemoryRowInfo minRow;
    protected AgentMemoryRowInfo maxRow;
    protected AgentMemoryRowInfo sumRow;
    protected AgentMemoryRowInfo lastRow;

    /** creates new memory service */
    public AgentMemoryImpl(Agent parentAgent) {
        super(parentAgent);
        parentAgent.getServices().registerService(this);
    }

    /** count objects in this agentable object including this object */
    public long countObjectsService() {
        // TODO: calculate counts of objects
        return 2L + 12L * memoryStates.size();
    }
    /** run after initialization */
    public void afterInitialization() {
    }

    /** run by agent every X seconds */
    public boolean onTimeMemoryCheck() {
        try {
            touch("onTimeMemoryCheck");
            checkCount.incrementAndGet();
            log.info("Timer memory check, seq: " + checkCount.get() +", working time: " + parentAgent.getAgentWorkingTime());
            createEvent("onTimeMemoryCheck");
            AgentMemoryRowInfo row = createMemoryRow().toInfo();
            memoryStates.add(row);
            while (memoryStates.size() > 100) {
                memoryStates.poll();
            }
            minRow.min(row);
            maxRow.max(row);
            sumRow.sum(row);
            lastRow = row;
            countRow.incrementAndGet();
            log.debug("AGENT MEMORY for guid: " + parentAgent.getAgentGuid() + "");
            return true;
        } catch (Exception ex) {
            log.warn("Cannot ping registrations, check agents or remove inactive agents, reason: " + ex.getMessage(), ex);
            addIssueToAgent("onTimeRegisterRefresh", ex);
            return false;
        }
    }
    @Override
    public DistServiceType getServiceType() {
        return DistServiceType.memory;
    }
    /** get description of this service */
    public String getServiceDescription() {
        return "Check memory consumption by Agent";
    }
    /** get detailed info about this service */
    public AgentMemoryInfo getInfo() {
        return new AgentMemoryInfo(memorySeq.get(), countRow.get(), minRow, maxRow, sumRow.produceAvg(countRow.get()), lastRow);
    }

    /** additional web API endpoints */
    protected DistWebApiProcessor additionalWebApiProcessor() {
        return new DistWebApiProcessor(getServiceType())
                .addHandlerGet("info", (m, req) -> req.responseOkJsonSerialize(getInfo()))
                .addHandlerGet("memory-now", (m, req) -> req.responseOkJsonSerialize(createMemoryRow()))
                .addHandlerGet("memory-states", (m, req) -> req.responseOkJsonSerialize(memoryStates))
                .addHandlerPost("gc", (m, req) -> req.responseOkJsonSerialize(runGc()));
    }
    /** get sequence of this memory in current agent */
    public long getMemorySeq() {
        return memorySeq.get();
    }
    /** create single row with memory stats */
    public DistAgentMemoryRow createMemoryRow() {
        createEvent("createMemoryRow");
        Runtime rt = java.lang.Runtime.getRuntime();
        long freeMemory = rt.freeMemory();
        long totalMemory = rt.totalMemory();
        long maxMemory = rt.maxMemory();
        memorySeq.incrementAndGet();
        return new DistAgentMemoryRow(parentAgent.getAgentGuid(),
                System.currentTimeMillis()- parentAgent.getAgentStartTime(), memorySeq.incrementAndGet(),
                maxMemory, totalMemory, freeMemory, totalMemory-freeMemory, parentAgent.countAgentObjects());
    }
    /** update configuration of this Service */
    public void updateConfig(DistConfig newCfg) {
        // TODO: update configuration of this service
    }
    /** run GC */
    public Map<String, Object> runGc() {
        createEvent("runGc");
        long startTime = System.currentTimeMillis();
        System.gc();
        long runTimeMs = System.currentTimeMillis() - startTime;
        return Map.of("date", LocalDateTime.now().toString(),
                "runTimeMs", ""+runTimeMs);
    }
    @Override
    protected void onClose() {
    }

    /** read configuration and re-initialize this component */
    protected boolean onReinitialize() {
        AgentMemoryRowInfo row = createMemoryRow().toInfo();
        countRow.incrementAndGet();
        minRow = row;
        maxRow = row;
        sumRow = row;
        lastRow = row;
        log.debug("Set up timer to save memory state, agent: " + getParentAgentGuid());
        parentAgent.getTimers().cancelTimer("MEMORY_STATE");
        parentAgent.getTimers().setUpTimer("MEMORY_STATE", DistConfig.AGENT_CACHE_TIMER_REGISTRATION_PERIOD, DistConfig.AGENT_CACHE_TIMER_REGISTRATION_PERIOD_DELAY_VALUE, x -> onTimeMemoryCheck());
        // nothing to be done here
        return true;
    }
}
