package com.distsystem.test.agent;

import com.distsystem.DistFactory;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.Cache;
import com.distsystem.utils.DistUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

public class AgentReceiverSendingTest {
    private static final Logger log = LoggerFactory.getLogger(AgentReceiverSendingTest.class);

    @Test
    public void agentReceiverSendingTest() {
        log.info("START ------ agent receiver sending test");

        Agent agent1 = DistFactory.buildEmptyFactory()
                .withUniverseName("GlobalAgent")
                .withWebApiPort(9999)
                .withRegistrationJdbc("jdbc:postgresql://localhost:5432/cache01", "org.postgresql.Driver",
                        "cache_user", "${JDBC_PASS}")
                .withServerSocketPort(9901)
                .withTimerStorageClean(1000)
                .withTimerRegistrationPeriod(1000)
                .withTimerServerPeriod(1000)
                .createAgentInstance();

        Cache cache = agent1.getCache();
        cache.getAgent();
        agent1.getAgentInfo();

        Agent agent2 = DistFactory.buildEmptyFactory()
                .withUniverseName("GlobalAgent")
                .withWebApiPort(9998)
                .withRegistrationJdbc("jdbc:postgresql://localhost:5432/cache01", "org.postgresql.Driver",
                        "cache_user", "${JDBC_PASS}")
                .withServerSocketPort(9902)
                .withTimerStorageClean(1000)
                .withTimerRegistrationPeriod(1000)
                .withTimerServerPeriod(1000)
                .createAgentInstance();


        int maxTime = 3;
        for (int t=0; t<maxTime; t++) {
            log.info("TIME IS RUNNING................................ minutes: " + t + " of " + maxTime);
            DistUtils.sleep(60000);
        }

        log.info("==================================================================================================//////////////////////////////////////////////////////////////////////////////////////////////////////////////========================");
        log.info("========--------> CLOSING TEST");
        agent1.close();
        agent2.close();
        assertTrue(agent1.isClosed(), "agent1 should be closed");
        assertTrue(agent2.isClosed(), "agent2 should be closed");
        log.info("END-----");
    }
}
