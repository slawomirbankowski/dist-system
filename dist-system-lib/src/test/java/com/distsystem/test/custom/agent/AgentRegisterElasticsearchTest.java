package com.distsystem.test.custom.agent;

import com.distsystem.DistFactory;
import com.distsystem.api.CacheMode;
import com.distsystem.api.enums.DistEnvironmentType;
import com.distsystem.interfaces.Agent;
import com.distsystem.utils.DistUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AgentRegisterElasticsearchTest {
    private static final Logger log = LoggerFactory.getLogger(AgentRegisterElasticsearchTest.class);

    @Test
    public void agentRegisterElasticsearchTest() {
        log.info("START ------ agent register Elasticsearch test");
        Agent agent1 = DistFactory.buildEmptyFactory()
                .withEnvironment(DistEnvironmentType.testing)
                .withEnvironmentVariables()
                .withUniverseName("GlobalAgent")
                .withWebApiPort(9999)
                .withRegistrationElasticsearch("${ELASTICSEARCH_URL}", "${ELASTICSEARCH_USER}", "${ELASTICSEARCH_PASS}")
                .withRegisterCleanAfter(CacheMode.TIME_FIVE_MINUTES, CacheMode.TIME_ONE_DAY)
                .withServerDatagramPort(9911)
                .withSerializer("java.lang.String=StringSerializer,default=ObjectStreamSerializer")
                .withTimerRegistrationPeriod(30000)
                .withTimerServerPeriod(30000)
                .createAgentInstance();

        Agent agent2 = DistFactory.buildEmptyFactory()
                .withEnvironment(DistEnvironmentType.testing)
                .withEnvironmentVariables()
                .withUniverseName("GlobalAgent")
                .withWebApiPort(9998)
                .withRegistrationElasticsearch("${ELASTICSEARCH_URL}", "${ELASTICSEARCH_USER}", "${ELASTICSEARCH_PASS}")
                .withRegisterCleanAfter(CacheMode.TIME_FIVE_MINUTES, CacheMode.TIME_ONE_DAY)
                .withServerDatagramPort(9912)
                .withSerializer("java.lang.String=StringSerializer,default=ObjectStreamSerializer")
                .withTimerRegistrationPeriod(30000)
                .withTimerServerPeriod(30000)
                .createAgentInstance();

        assertNotNull(agent1, "Created agent should not be null");
        assertNotNull(agent2, "Created agent should not be null");

        /*
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
        assertEquals(2, agent2.getAgentRegistrations().getAgents().size(), "There should be 2 agents");
        assertEquals(1, agent2.getAgentRegistrations().getRegistrationsCount(), "There should be 1 registration service");

*/
        int maxTime = 3;
        for (int t=0; t<maxTime; t++) {
            log.info("TIME IS RUNNING................................ minutes: " + t + " of " + maxTime);
            DistUtils.sleep(60000);
        }
        log.info("TIME IS UP................................");
        agent1.close();
        agent2.close();

        log.info("END-----");
    }
}
