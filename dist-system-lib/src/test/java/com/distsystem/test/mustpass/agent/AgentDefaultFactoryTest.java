package com.distsystem.test.mustpass.agent;

import com.distsystem.DistFactory;
import com.distsystem.api.DistConfig;
import com.distsystem.interfaces.Agent;
import com.distsystem.utils.DistUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AgentDefaultFactoryTest {
    private static final Logger log = LoggerFactory.getLogger(AgentDefaultFactoryTest.class);

    @Test
    @Tag("mustpass")
    public void agentDefaultFactoryTest() {
        log.info("START ------ agent default factory test");
        Agent agent1 = DistFactory.buildDefaultFactory()
                .createAgentInstance();
        assertNotNull(agent1, "Created agent1 should not be null");
        assertNotNull(agent1.getAgentInfo(), "Info should not be null");
        assertNotNull(agent1.getConnectors(), "Connectors should not be null");
        assertNotNull(agent1.getAgentDao(), "Dao should not be null");
        assertNotNull(agent1.getAgentSecret(), "Secret should not be null");
        assertNotNull(agent1.getAgentSimpleInfo(), "SimpleInfo should not be null");
        assertNotNull(agent1.getApi(), "Api should not be null");
        assertNotNull(agent1.getAuth(), "Auth should not be null");
        assertNotNull(agent1.getCache(), "Cache should not be null");
        assertNotNull(agent1.getConfig(), "Config should not be null");
        assertNotNull(agent1.getConfig().getProperties(), "Config properties should not be null");
        assertNotNull(agent1.getConfig().getProperty(DistConfig.AGENT_NAME), "AgentName config property should not be null");
        assertNotNull(agent1.getEvents(), "Events should not be null");
        assertNotNull(agent1.getFlow(), "Flow should not be null");
        assertNotNull(agent1.getIssues(), "Issues should not be null");
        assertNotNull(agent1.getMeasure(), "Measure should not be null");
        assertNotNull(agent1.getMemory(), "Memory should not be null");
        assertNotNull(agent1.getMl(), "ML should not be null");
        assertNotNull(agent1.getMonitor(), "Monitor should not be null");
        assertNotNull(agent1.getNotification(), "Notification should not be null");
        assertNotNull(agent1.getObjects(), "Objects should not be null");
        assertNotNull(agent1.getReceiver(), "Info should not be null");
        assertNotNull(agent1.getRegistrations(), "Registrations should not be null");
        assertNotNull(agent1.getReports(), "Reports should not be null");
        assertNotNull(agent1.getSchedule(), "Schedule should not be null");
        assertNotNull(agent1.getSecurity(), "Security should not be null");
        assertNotNull(agent1.getSemaphores(), "Semaphores should not be null");
        assertNotNull(agent1.getServices(), "Services should not be null");
        assertNotNull(agent1.getSpace(), "Space should not be null");
        assertNotNull(agent1.getStorages(), "Storages should not be null");
        assertNotNull(agent1.getThreads(), "Threads should not be null");
        assertNotNull(agent1.getTimers(), "Timers should not be null");
        assertNotNull(agent1.getVersion(), "Version should not be null");
        DistUtils.sleep(200);
        log.info("========-----> Agent1: " + agent1.getAgentInfo());
        int maxTime = 3;
        for (int t=0; t<maxTime; t++) {
            log.info("TIME IS RUNNING................................ seconds: " + t + " of " + maxTime);
            DistUtils.sleep(1000);
        }
        log.info("==================================================================================================//////////////////////////////////////////////////////////////////////////////////////////////////////////////========================");
        log.info("========--------> CLOSING TEST");
        agent1.close();
        assertTrue(agent1.isClosed(), "agent1 should be closed");
        log.info("END-----");
    }
}
