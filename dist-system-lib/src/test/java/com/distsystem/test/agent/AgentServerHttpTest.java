package com.distsystem.test.agent;

import com.distsystem.DistFactory;
import com.distsystem.api.*;
import com.distsystem.interfaces.Agent;
import com.distsystem.utils.DistUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/** HTTP server in Agent to communicate with other Agents */
public class AgentServerHttpTest {

    private static final Logger log = LoggerFactory.getLogger(AgentServerHttpTest.class);

    @Test
    public void agentServerHttpTest() {
        log.info("START ------ agent server HTTP test");
        Agent agent1 = DistFactory.buildEmptyFactory()
                .withUniverseName("GlobalAgent")
                .withRegistrationJdbc("jdbc:postgresql://localhost:5432/cache01", "org.postgresql.Driver",
                        "cache_user", "${JDBC_PASS}")
                .withServerHttpPort(9911)
                .withSerializer("java.lang.String=StringSerializer,default=ObjectStreamSerializer")
                .withRegisterCleanAfter(CacheMode.TIME_FIVE_MINUTES, CacheMode.TIME_ONE_DAY)
                .withTimerRegistrationPeriod(1000)
                .withTimerServerPeriod(1000)
                .createAgentInstance();

        Agent agent2 = DistFactory.buildEmptyFactory()
                .withUniverseName("GlobalAgent")
                .withRegistrationJdbc("jdbc:postgresql://localhost:5432/cache01", "org.postgresql.Driver",
                        "cache_user", "${JDBC_PASS}")
                .withServerHttpPort(9912)
                .withSerializer("java.lang.String=StringSerializer,default=ObjectStreamSerializer")
                .withRegisterCleanAfter(CacheMode.TIME_FIVE_MINUTES, CacheMode.TIME_ONE_DAY)
                .withTimerRegistrationPeriod(1000)
                .withTimerServerPeriod(1000)
                .createAgentInstance();

        assertNotNull(agent1, "Created agent1 should not be null");
        assertNotNull(agent2, "Created agent2 should not be null");

        log.info("======-----> Agent1 [1]: " + agent1.getAgentRegistrations().getAgents().size() + ", servers: " + agent1.getAgentServices().getServices().size());
        assertEquals(1, agent1.getAgentServices().getServices().size(), "There should be 1 server");
        assertEquals(0, agent1.getAgentRegistrations().getAgents().size(), "There should be 0 agents");
        assertEquals(1, agent1.getAgentRegistrations().getRegistrationsCount(), "There should be 1 registration service");
        log.info("======-----> Agent2 [1]: " + agent2.getAgentRegistrations().getAgents().size() + ", servers: " + agent2.getAgentServices().getServices().size());
        assertEquals(1, agent2.getAgentServices().getServices().size(), "There should be 1 server");
        assertEquals(0, agent2.getAgentRegistrations().getAgents().size(), "There should be 0 agents");
        assertEquals(1, agent2.getAgentRegistrations().getRegistrationsCount(), "There should be 1 registration service");

//com.distsystem.api.enums.DistServiceType fromService, DistServiceType toService, String method, Object message, DistCallbacks

        DistUtils.sleep(3000);
        log.info("======-----> Agent1 [2]: " + agent1.getAgentRegistrations().getAgents().size() + ", servers: " + agent1.getAgentServices().getServices().size());
        assertEquals(1, agent1.getAgentServices().getServices().size(), "There should be 1 server");
        assertEquals(2, agent1.getAgentRegistrations().getAgents().size(), "There should be 2 agents");
        assertEquals(1, agent1.getAgentRegistrations().getRegistrationsCount(), "There should be 1 registration service");
        log.info("======-----> Agent2 [2]: " + agent2.getAgentRegistrations().getAgents().size() + ", servers: " + agent2.getAgentServices().getServices().size());
        assertEquals(1, agent2.getAgentServices().getServices().size(), "There should be 1 server");
        assertEquals(2, agent2.getAgentRegistrations().getAgents().size(), "There should be s agent");
        assertEquals(1, agent2.getAgentRegistrations().getRegistrationsCount(), "There should be 1 registration service");

        agent1.close();
        agent2.close();
        assertTrue(agent1.isClosed(), "agent1 should be closed");
        assertTrue(agent2.isClosed(), "agent2 should be closed");

        log.info("END-----");
    }
}
