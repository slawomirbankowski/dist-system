package com.distsystem.test.mustpass.agent;

import com.distsystem.DistFactory;
import com.distsystem.interfaces.Agent;
import com.distsystem.utils.DistUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

public class AgentRegisterJdbcSingleTest {
    private static final Logger log = LoggerFactory.getLogger(AgentRegisterJdbcSingleTest.class);

    @Test
    public void agentRegisterJdbcTest() {
        log.info("START ------ agent register JDBC test");
        Agent agent1 = DistFactory.buildEmptyFactory()
                .withUniverseName("GlobalAgent")
                .withWebApiPort(9999)
                .withRegistrationJdbc("${JDBC_URL}", "${JDBC_DRIVER}", "${JDBC_USER}", "${JDBC_PASS}")
                .withCommonProperties()
                .withCacheStoragePriorityQueue()
                .withCacheStorageWeakHashMap()
                .withServerSocketPort(9997)
                .withServerDatagramPort(9002)
                .withTimerStorageClean(1000)
                .withTimerRegistrationPeriod(1000)
                .withTimerServerPeriod(1000)
                .createAgentInstance();
        assertNotNull(agent1, "Created agent1 should not be null");

        DistUtils.sleep(3000);
        log.info("========-----> Agent1: " + agent1.getAgentInfo());
        int maxTime = 5;
        for (int t=0; t<maxTime; t++) {
            log.info("TIME IS RUNNING................................ minutes: " + t + " of " + maxTime);
            DistUtils.sleep(1000);
        }
        log.info("==================================================================================================//////////////////////////////////////////////////////////////////////////////////////////////////////////////========================");
        log.info("========--------> CLOSING TEST");
        agent1.close();

        assertTrue(agent1.isClosed(), "agent1 should be closed");
        log.info("END-----");
    }
}
