package com.distsystem.test.custom.agent;

import com.distsystem.DistFactory;
import com.distsystem.api.enums.DistCallbackType;
import com.distsystem.api.DistCallbacks;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.interfaces.Agent;
import com.distsystem.utils.DistUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

public class AgentRegisterJdbcTest {
    private static final Logger log = LoggerFactory.getLogger(AgentRegisterJdbcTest.class);

    @Test
    @Tag("custom")
    public void agentRegisterJdbcTest() {
        log.info("START ------ agent register JDBC test");
        // 9999 API, 9998 HTTP, 9997 Socket, 9996 UDP,
        // 9989, 9988, 9987, 9984, 9986
        Agent agent1 = DistFactory.buildEmptyFactory()
                .withUniverseName("GlobalAgent")
                .withWebApiDefaultPort()
                .withSerializerDefault()
                .withRegistrationJdbc("${JDBC_URL}", "${JDBC_DRIVER}", "${JDBC_USER}", "${JDBC_PASS}")
                .withServerDatagramPort(9997)
                .withTimerStorageClean(30000)
                .withTimerRegistrationPeriod(30000)
                .withTimerServerPeriod(30000)
                .createAgentInstance();

        Agent agent2 = DistFactory.buildEmptyFactory()
                .withUniverseName("GlobalAgent")
                .withWebApiPort(9998)
                .withRegistrationJdbc("${JDBC_URL}", "${JDBC_DRIVER}", "${JDBC_USER}", "${JDBC_PASS}")
                .withServerDatagramPort(9987)
                .withSerializerDefault()
                .withTimerStorageClean(30000)
                .withTimerRegistrationPeriod(30000)
                .withTimerServerPeriod(30000)
                .createAgentInstance();

        assertNotNull(agent1, "Created agent1 should not be null");
        assertNotNull(agent2, "Created agent2 should not be null");

        DistUtils.sleep(4000);

        DistUtils.sleep(4000);

        int maxTime = 3;
        for (int t=0; t<maxTime; t++) {
            log.info("TIME IS RUNNING................................ minutes: " + t + " of " + maxTime);
            log.info("========-----> Agent1: " + agent1.getAgentInfo());
            log.info("========-----> Agent2: " + agent2.getAgentInfo());
            DistUtils.sleep(60000);
        }

        assertEquals(1, agent1.getRegistrations().getAgentsActiveCount(), "There should be 1 agent known by agent1");
        assertEquals(1, agent2.getRegistrations().getAgentsActiveCount(), "There should be 1 agent known by agent2");

        log.info("========-----> Agent1 client keys: " + agent1.getConnectors().getClientKeys());
        log.info("========-----> Agent2 client keys: " + agent2.getConnectors().getClientKeys());

        assertEquals(1, agent1.getConnectors().getClientsCount(), "There should be 1 client in agent1");
        assertEquals(1, agent2.getConnectors().getClientsCount(), "There should be 1 client in agent2");

        agent1.sendMessageBroadcast(DistServiceType.agent, DistServiceType.agent, "ping", "ping", DistCallbacks.createEmpty().addCallback(DistCallbackType.onResponse, x -> {
            log.info("RESPONSE GET for message: " + x.getMessageUid());
            return true;
        }));
        agent2.sendMessageBroadcast(DistServiceType.agent, DistServiceType.agent, "ping", "ping", DistCallbacks.createEmpty().addCallback(DistCallbackType.onResponse, x -> {
            log.info("RESPONSE GET for message: " + x.getMessageUid());
            return true;
        }));

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
