package com.distsystem.test.agent;

import com.distsystem.DistFactory;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.Cache;
import com.distsystem.utils.DistUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AgentWebApiTest {
    private static final Logger log = LoggerFactory.getLogger(AgentWebApiTest.class);

    @Test
    public void agentWebApiTest() {
        log.info("START ------ agent Web API test");
        Cache cache1 = DistFactory.buildEmptyFactory()
                .withUniverseName("GlobalAgent")
                .withWebApiPort(9999)
                .withServerSocketPort(9001)
                .withCacheStorageHashMap()
                .withTimerStorageClean(10000)
                .withTimerServerPeriod(10000)
                .withTimerRegistrationPeriod(10000)
                .withCacheStorageJdbc("jdbc:postgresql://localhost:5432/cache01", "org.postgresql.Driver",
                        "cache_user", "${JDBC_PASS}")
                .withRegistrationJdbc("jdbc:postgresql://localhost:5432/cache01", "org.postgresql.Driver",
                        "cache_user", "${JDBC_PASS}")
                .createCacheInstance();

        Agent agent1 = cache1.getAgent();

        Cache cache2 = DistFactory.buildEmptyFactory()
                .withUniverseName("GlobalAgent")
                .withWebApiPort(9998)
                .withTimerStorageClean(10000)
                .withTimerServerPeriod(10000)
                .withTimerRegistrationPeriod(10000)
                .withServerSocketPort(9002)
                .withCacheStorageHashMap()
                .withCacheStorageJdbc("jdbc:postgresql://localhost:5432/cache01", "org.postgresql.Driver",
                        "cache_user", "${JDBC_PASS}")
                .withRegistrationJdbc("jdbc:postgresql://localhost:5432/cache01", "org.postgresql.Driver",
                        "cache_user", "${JDBC_PASS}")
                .createCacheInstance();

        Agent agent2 = cache2.getAgent();

        assertNotNull(agent1, "Created agent1 should not be null");
        assertNotNull(agent2, "Created agent2 should not be null");

        for (int i=0; i<6; i++) {
            log.info("SLEEPING................................");
            DistUtils.sleep(30000);
            log.info("========-------------------------------------------------------------------------------------========================");
            log.info("========-----> Agent1: " + agent1.getAgentInfo());
            log.info("========-----> Agent2: " + agent2.getAgentInfo());
            log.info("========-------------------------------------------------------------------------------------========================");
        }

        log.info("==================================================================================================//////////////////////////////////////////////////////////////////////////////////////////////////////////////========================");
        log.info("========--------> CLOSING TEST");
        agent1.close();
        agent2.close();
        assertTrue(agent1.isClosed(), "agent1 should be closed");
        log.info("END-----");
    }
}
