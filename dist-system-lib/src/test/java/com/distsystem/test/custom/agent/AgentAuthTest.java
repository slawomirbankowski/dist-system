package com.distsystem.test.custom.agent;

import com.distsystem.DistFactory;
import com.distsystem.interfaces.Agent;
import com.distsystem.utils.DistUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AgentAuthTest {
    private static final Logger log = LoggerFactory.getLogger(AgentAuthTest.class);

    @Test
    public void agentRegisterJdbcTest() {
        log.info("START ------ agent register JDBC test");

        Agent agent1 = DistFactory.buildEmptyFactory()
                .withWebApiPort(9999)
                .withAuthStorageJdbc("jdbc:postgresql://localhost:5432/cache01", "org.postgresql.Driver",
                        "cache_user", "${JDBC_PASS}")
                .withCommonProperties()
                .withCacheStoragePriorityQueue()
                .withCacheStorageWeakHashMap()
                .withServerSocketPort(9997)
                .withMaxEvents(10000)
                .withMaxIssues(10000)
                .withTimerStorageClean(30000)
                .withTimerRegistrationPeriod(30000)
                .withTimerServerPeriod(30000)
                .createAgentInstance();

        assertNotNull(agent1, "Created agent1 should not be null");

        DistUtils.sleep(200);

        log.info("========-----> Agent1: " + agent1.getAgentInfo());

        int maxTime = 30;
        for (int t=0; t<maxTime; t++) {
            log.info("TIME IS RUNNING................................ minutes: " + t + " of " + maxTime);
            DistUtils.sleep(60000);
        }

        log.info("==================================================================================================//////////////////////////////////////////////////////////////////////////////////////////////////////////////========================");
        log.info("========--------> CLOSING TEST");
        agent1.close();

        assertTrue(agent1.isClosed(), "agent1 should be closed");
        log.info("END-----");
    }
}
