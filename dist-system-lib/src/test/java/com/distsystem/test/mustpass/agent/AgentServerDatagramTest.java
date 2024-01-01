package com.distsystem.test.mustpass.agent;

import com.distsystem.DistFactory;
import com.distsystem.api.enums.DistClientType;
import com.distsystem.interfaces.Agent;
import com.distsystem.utils.DistUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

public class AgentServerDatagramTest {
    private static final Logger log = LoggerFactory.getLogger(AgentServerDatagramTest.class);

    @Test
    @Tag("custom")
    public void agentServerDatagramTest() {
        log.info("START ------ agent server datagram test");
        Agent agent1 = DistFactory.buildEmptyFactory()
                .withUniverseName("GlobalAgent")
                .withServerDatagramPort(9911)
                .createAgentInstance();
        assertNotNull(agent1, "Created agent should not be null");
        DistUtils.sleep(1000);
        log.info("======-----> Agent1 [1]: " + agent1.getRegistrations().getAgents().size() + ", servers: " + agent1.getServices().getServicesByType().size());

        assertEquals(27, agent1.getServices().getServicesByType().size(), "There should be 27 services");
        assertEquals(0, agent1.getRegistrations().getAgents().size(), "There should be 0 agents");
        assertEquals(1, agent1.getConnectors().getServersCount(), "There should be 1 server");

        var keys = agent1.getConnectors().getServerKeys();
        assertEquals(1, keys.size(), "There should be 1 server key");

        var datagramServerOrEmpty = agent1.getConnectors().getServerByKey(keys.get(0));
        assertTrue(datagramServerOrEmpty.isPresent(), "There should be datagram server");

        var datagramServer = datagramServerOrEmpty.get();
        assertEquals(DistClientType.datagram, datagramServer.getClientType(), "Server be datagram");
        assertEquals(9911, datagramServer.getPort(), "Port must be 9911");
        assertFalse(datagramServer.isClosed(), "Datagram server must be not closed");

        agent1.close();
        assertTrue(agent1.isClosed(), "agent1 should be closed");

        log.info("END-----");
    }
}
