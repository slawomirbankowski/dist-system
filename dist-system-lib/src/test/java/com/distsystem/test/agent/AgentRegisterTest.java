package com.distsystem.test.agent;

import com.distsystem.DistFactory;
import com.distsystem.api.*;
import com.distsystem.api.enums.DistCallbackType;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.interfaces.Agent;
import com.distsystem.utils.DistUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

public class AgentRegisterTest {
    private static final Logger log = LoggerFactory.getLogger(AgentRegisterTest.class);

    @Test
    public void agentRegisterTest() {
        log.info("START ------ agent register test");

        Agent agent1 = DistFactory.buildEmptyFactory()
                .withUniverseName("GlobalAgent")
                .withRegistrationJdbc("jdbc:postgresql://localhost:5432/cache01", "org.postgresql.Driver",
                        "cache_user", "${JDBC_PASS}")
                .withServerSocketPort(9901)
                .withTimerStorageClean(1000)
                .withTimerRegistrationPeriod(1000)
                .withTimerServerPeriod(1000)
                .createAgentInstance();

        Agent agent2 = DistFactory.buildEmptyFactory()
                .withUniverseName("GlobalAgent")
                .withRegistrationJdbc("jdbc:postgresql://localhost:5432/cache01", "org.postgresql.Driver",
                        "cache_user", "${JDBC_PASS}")
                .withServerSocketPort(9902)
                .withTimerStorageClean(1000)
                .withTimerRegistrationPeriod(1000)
                .withTimerServerPeriod(1000)
                .createAgentInstance();

        assertNotNull(agent1, "Created agent1 should not be null");
        assertNotNull(agent2, "Created agent2 should not be null");

        assertEquals(0, agent1.getRegistrations().getAgents().size(), "There should be 0 agents known by agent1");
        assertEquals(0, agent2.getRegistrations().getAgents().size(), "There should be 0 agents known by agent2");

        DistUtils.sleep(3000);

        log.info("========-----> Agent1: " + agent1.getAgentInfo());
        log.info("========-----> Agent2: " + agent2.getAgentInfo());

        assertEquals(2, agent1.getRegistrations().getAgents().size(), "There should be 2 agents known by agent1");
        assertEquals(2, agent2.getRegistrations().getAgents().size(), "There should be 2 agents known by agent2");

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
        DistUtils.sleep(120000);


        log.info("==================================================================================================//////////////////////////////////////////////////////////////////////////////////////////////////////////////========================");
        log.info("========--------> CLOSING TEST");
        agent1.close();
        agent2.close();
        assertTrue(agent1.isClosed(), "agent1 should be closed");
        assertTrue(agent2.isClosed(), "agent2 should be closed");
        log.info("END-----");
    }
}
