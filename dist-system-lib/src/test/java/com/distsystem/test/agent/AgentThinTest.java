package com.distsystem.test.agent;

import com.distsystem.DistFactory;
import com.distsystem.interfaces.Agent;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

public class AgentThinTest {
    private static final Logger log = LoggerFactory.getLogger(AgentThinTest.class);

    /** simple test for very thin Agent that is not holding anything, not doing anything */
    @Test
    public void agentRegisterTest() {
        log.info("START ------ agent thin test");
        Agent agent = DistFactory.buildEmptyFactory()
                .createAgentInstance();
        assertNotNull(agent, "Created agent should not be null");
        assertEquals(0, agent.getRegistrations().getAgents().size(), "There should be 0 agents known by agent");
        log.info("Services: " + agent.getServices().getServiceInfos());
        assertEquals(1, agent.getServices().getServicesCount(), "There should be 0 services");
        log.info("" + agent.getConnectors().getInfo().toString());
        assertEquals(0, agent.getConnectors().getClientsCount(), "There should be 0 clients");
        assertEquals(0, agent.getIssues().getIssues().size(), "There should be 0 issues");
        assertEquals(0, agent.getThreads().getThreadsCount(), "There should be 0 threads");
        assertEquals(0, agent.getRegistrations().getRegistrationsCount(), "There should be 0 registrations");
        agent.close();
        assertTrue(agent.isClosed(), "agent should be closed");
        log.info("END-----");
    }
}
