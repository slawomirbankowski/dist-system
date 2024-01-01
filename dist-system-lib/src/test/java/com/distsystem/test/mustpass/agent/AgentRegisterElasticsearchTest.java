package com.distsystem.test.mustpass.agent;

import com.distsystem.DistFactory;
import com.distsystem.api.CacheMode;
import com.distsystem.api.enums.DistEnvironmentType;
import com.distsystem.interfaces.Agent;
import com.distsystem.utils.DistUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AgentRegisterElasticsearchTest {
    private static final Logger log = LoggerFactory.getLogger(AgentRegisterElasticsearchTest.class);

    @Test
    @Tag("mustpass")
    public void agentRegisterElasticsearchTest() {
        log.info("START ------ agent register Elasticsearch test");
        Agent agent1 = DistFactory.buildEmptyFactory()
                .withEnvironment(DistEnvironmentType.testing)
                .withEnvironmentVariables()
                .withRegistrationElasticsearch("${ELASTICSEARCH_URL}", "${ELASTICSEARCH_USER}", "${ELASTICSEARCH_PASS}")
                .withRegisterCleanAfter(CacheMode.TIME_FIVE_MINUTES, CacheMode.TIME_ONE_DAY)
                .withWebApiDefaultPort()
                .withServerDatagramPortDefaultValue()
                .withServerSocketDefaultPort()
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
                .withServerDatagramPort(9992)
                .withSerializer("java.lang.String=StringSerializer,default=ObjectStreamSerializer")
                .withTimerRegistrationPeriod(30000)
                .withTimerServerPeriod(30000)
                .createAgentInstance();

        assertNotNull(agent1, "Created agent should not be null");
        assertNotNull(agent2, "Created agent should not be null");

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
