package com.distsystem.test.custom.agent;

import com.distsystem.DistFactory;
import com.distsystem.api.CacheMode;
import com.distsystem.api.enums.DistCallbackType;
import com.distsystem.api.DistCallbacks;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.interfaces.Agent;
import com.distsystem.utils.DistUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

public class AgentServerDatagramTest {
    private static final Logger log = LoggerFactory.getLogger(AgentServerDatagramTest.class);

    @Test
    public void agentServerDatagramTest() {
        log.info("START ------ agent server datagram test");
        Agent agent1 = DistFactory.buildEmptyFactory()
                .withUniverseName("GlobalAgent")
                .withRegistrationJdbc("${JDBC_URL}", "${JDBC_DRIVER}", "${JDBC_USER}", "${JDBC_PASS}")
                .withRegisterCleanAfter(CacheMode.TIME_FIVE_MINUTES, CacheMode.TIME_ONE_DAY)
                .withServerDatagramPort(9911)
                .withSerializer("java.lang.String=StringSerializer,default=ObjectStreamSerializer")
                .withTimerRegistrationPeriod(1000)
                .withTimerServerPeriod(1000)
                .createAgentInstance();

        Agent agent2 = DistFactory.buildEmptyFactory()
                .withUniverseName("GlobalAgent")
                .withRegistrationJdbc("${JDBC_URL}", "${JDBC_DRIVER}", "${JDBC_USER}", "${JDBC_PASS}")
                .withRegisterCleanAfter(CacheMode.TIME_FIVE_MINUTES, CacheMode.TIME_ONE_DAY)
                .withServerDatagramPort(9912)
                .withSerializer("java.lang.String=StringSerializer,default=ObjectStreamSerializer")
                .withTimerRegistrationPeriod(1000)
                .withTimerServerPeriod(1000)
                .createAgentInstance();

        assertNotNull(agent1, "Created agent should not be null");
        assertNotNull(agent2, "Created agent should not be null");

        log.info("======-----> Agent1 [1]: " + agent1.getRegistrations().getAgents().size() + ", servers: " + agent1.getServices().getServices().size());
        assertEquals(1, agent1.getServices().getServices().size(), "There should be 1 server");
        assertEquals(0, agent1.getRegistrations().getAgents().size(), "There should be 0 agents");
        assertEquals(1, agent1.getRegistrations().getRegistrationsCount(), "There should be 1 registration service");
        log.info("======-----> Agent2 [1]: " + agent2.getRegistrations().getAgents().size() + ", servers: " + agent2.getServices().getServices().size());
        assertEquals(1, agent2.getServices().getServices().size(), "There should be 1 server");
        assertEquals(0, agent2.getRegistrations().getAgents().size(), "There should be 0 agents");
        assertEquals(1, agent2.getRegistrations().getRegistrationsCount(), "There should be 1 registration service");

//com.distsystem.api.enums.DistServiceType fromService, DistServiceType toService, String method, Object message, DistCallbacks

        DistUtils.sleep(3000);
        log.info("======-----> Agent1 [2]: " + agent1.getRegistrations().getAgents().size() + ", servers: " + agent1.getServices().getServices().size());
        assertEquals(1, agent1.getServices().getServices().size(), "There should be 1 server");
        assertEquals(2, agent1.getRegistrations().getAgents().size(), "There should be 2 agents");
        assertEquals(1, agent1.getRegistrations().getRegistrationsCount(), "There should be 1 registration service");
        log.info("======-----> Agent2 [2]: " + agent2.getRegistrations().getAgents().size() + ", servers: " + agent2.getServices().getServices().size());
        assertEquals(1, agent2.getServices().getServices().size(), "There should be 1 server");
        assertEquals(2, agent2.getRegistrations().getAgents().size(), "There should be 2 agents");
        assertEquals(1, agent2.getRegistrations().getRegistrationsCount(), "There should be 1 registration service");

        DistUtils.sleep(3000);

        agent1.sendMessageBroadcast(DistServiceType.agent, DistServiceType.agent, "ping", "ping", DistCallbacks.createEmpty().addCallback(DistCallbackType.onResponse, x -> { System.out.println("GOT PONG"); return true; } ));

        agent1.close();
        assertTrue(agent1.isClosed(), "agent1 should be closed");

        log.info("END-----");
    }
}
