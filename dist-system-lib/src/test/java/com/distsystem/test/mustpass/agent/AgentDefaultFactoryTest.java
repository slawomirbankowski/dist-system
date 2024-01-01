package com.distsystem.test.mustpass.agent;

import com.distsystem.DistFactory;
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
        DistUtils.sleep(200);
        log.info("========-----> Agent1: " + agent1.getAgentInfo());
        int maxTime = 3;
        for (int t=0; t<maxTime; t++) {
            log.info("TIME IS RUNNING................................ minutes: " + t + " of " + maxTime);
            DistUtils.sleep(100);
        }
        log.info("==================================================================================================//////////////////////////////////////////////////////////////////////////////////////////////////////////////========================");
        log.info("========--------> CLOSING TEST");
        agent1.close();
        assertTrue(agent1.isClosed(), "agent1 should be closed");
        log.info("END-----");
    }
}
