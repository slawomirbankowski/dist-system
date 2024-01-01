package com.distsystem.test.custom.agent;

import com.distsystem.DistFactory;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.Cache;
import com.distsystem.utils.DistUtils;
import com.distsystem.utils.JsonUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

public class AgentReceiverSendingTest {
    private static final Logger log = LoggerFactory.getLogger(AgentReceiverSendingTest.class);

    @Test
    @Tag("custom")
    public void agentReceiverSendingTest() {
        log.info("START ------ agent receiver sending test");

        Agent agent1 = DistFactory.buildEmptyFactory()
                .withUniverseName("GlobalAgent")
                .withEnvironmentVariables()
                .withWebApiPort(9999)
                .withRegistrationJdbcFromEnv()
                .withServerSocketPort(9991)
                .withTimerStorageClean(30000)
                .withTimerRegistrationPeriod(30000)
                .withTimerServerPeriod(30000)
                .createAgentInstance();

        Agent agent2 = DistFactory.buildEmptyFactory()
                .withUniverseName("GlobalAgent")
                .withEnvironmentVariables()
                .withWebApiPort(9998)
                .withRegistrationJdbc("${JDBC_URL}", "${JDBC_DRIVER}", "${JDBC_USER}", "${JDBC_PASS}")
                .withServerSocketPort(9902)
                .withTimerStorageClean(30000)
                .withTimerRegistrationPeriod(30000)
                .withTimerServerPeriod(30000)
                .createAgentInstance();
        int maxTime = 3;
        for (int t=0; t<maxTime; t++) {
            log.info("TIME IS RUNNING...... minutes: " + t + " of " + maxTime);
            log.info(".............................. Agent1: " + JsonUtils.serialize(agent1.getAgentInfo()));
            log.info(".............................. Agent2: " + JsonUtils.serialize(agent2.getAgentInfo()));
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
