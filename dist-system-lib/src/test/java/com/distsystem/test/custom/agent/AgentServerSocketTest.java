package com.distsystem.test.custom.agent;

import com.distsystem.DistFactory;
import com.distsystem.api.CacheMode;
import com.distsystem.interfaces.Agent;
import com.distsystem.utils.DistUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

public class AgentServerSocketTest {
    private static final Logger log = LoggerFactory.getLogger(AgentServerSocketTest.class);

    @Test
    public void agentServerSocketTest() {
        log.info("START ------ agent server socket test");
        Agent agent1 = DistFactory.buildEmptyFactory()
                .withUniverseName("GlobalAgent")
                .withRegistrationJdbc("${JDBC_URL}", "${JDBC_DRIVER}", "${JDBC_USER}", "${JDBC_PASS}")
                .withServerSocketPort(9901)
                .withRegisterCleanAfter(CacheMode.TIME_FIVE_MINUTES, CacheMode.TIME_ONE_DAY)
                .withTimerRegistrationPeriod(1000)
                .withTimerServerPeriod(1000)
                .createAgentInstance();

        agent1.getReports();


        assertNotNull(agent1, "Created agent should not be null");

        log.info("======-----> Agents [1]: " + agent1.getRegistrations().getAgents().size() + ", servers: " + agent1.getServices().getServices().size());
        assertEquals(1, agent1.getServices().getServices().size(), "There should be 1 server");
        assertEquals(0, agent1.getRegistrations().getAgents().size(), "There should be 0 agents");
        assertEquals(1, agent1.getRegistrations().getRegistrationsCount(), "There should be 1 registration service");

        DistUtils.sleep(3000);
        log.info("======-----> Agents [2]: " + agent1.getRegistrations().getAgents().size() + ", servers: " + agent1.getServices().getServices().size());
        assertEquals(1, agent1.getServices().getServices().size(), "There should be 1 server");
        assertEquals(1, agent1.getRegistrations().getAgents().size(), "There should be 1 agent");
        assertEquals(1, agent1.getRegistrations().getRegistrationsCount(), "There should be 1 registration service");

        agent1.close();
        assertTrue(agent1.isClosed(), "agent1 should be closed");

        log.info("END-----");
    }
}
