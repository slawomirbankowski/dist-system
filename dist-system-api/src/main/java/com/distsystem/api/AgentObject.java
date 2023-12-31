package com.distsystem.api;

import com.distsystem.api.info.AgentRegisteredInfo;
import com.distsystem.api.dtos.DistAgentRegisterRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/** representing management object for agent
 * It is representing remove Agent with simplified info, updates, registration keys.
 * */
public class AgentObject {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(AgentObject.class);
    /** create date of this object representing agent */
    private final LocalDateTime createDate = LocalDateTime.now();
    /** */
    private DistAgentRegisterRow agentRegisterRow;
    /** last updated date and time of this Agent */
    private LocalDateTime lastUpdated = LocalDateTime.now();
    /** number of updates of this Agent */
    private final AtomicLong updatesCount = new AtomicLong();
    /** set of registration keys as sources for this Agent */
    private final Set<String> registrationKeys = new HashSet<>();
    /** time of last ping from this agent */
    private LocalDateTime lastPingDate = LocalDateTime.now();

    public AgentObject(DistAgentRegisterRow agentRegisterRow) {
        this.agentRegisterRow = agentRegisterRow;
        log.debug("Create new AgentObject for GUID: " + agentRegisterRow.getAgentGuid() + ", hostIp: " + agentRegisterRow.getHostIp() + ", hostName: " + agentRegisterRow.getHostName() + ", active: " + agentRegisterRow.getIsActive());
    }
    /** get GUID for agent */
    public String getAgentGuid() {
        return agentRegisterRow.getAgentGuid();
    }
    /** unregister this agent */
    public void unregister() {
        // TODO: unregister this agent
    }
    /** */
    public void ping(AgentPing pingObj) {
        lastPingDate = pingObj.getCreateDate();
    }
    /** update existing agent with new information */
    public void update(DistAgentRegisterRow updateInfo, String registrationKey) {
        // TODO: update existing agent
        lastUpdated = LocalDateTime.now();
        agentRegisterRow = updateInfo;
        registrationKeys.add(registrationKey);
        updatesCount.incrementAndGet();
    }

    /** get information about registered remove Agent */
    public AgentRegisteredInfo getRegisteredInfo() {
        return new AgentRegisteredInfo(
                agentRegisterRow.getAgentGuid(), agentRegisterRow.getHostName(), agentRegisterRow.getHostIp(), agentRegisterRow.getPortNumber(),
                lastUpdated, updatesCount.get(),  lastPingDate, createDate, agentRegisterRow.getActive()
        );
    }

    /** */
    public DistAgentRegisterRow getSimplified() {
        return agentRegisterRow;
    }

    public boolean isActive() {
        return agentRegisterRow.getActive() == 1;
    }
    /** */
    public LocalDateTime getCreateDate() {
        return createDate;
    }
    /** */
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
}
