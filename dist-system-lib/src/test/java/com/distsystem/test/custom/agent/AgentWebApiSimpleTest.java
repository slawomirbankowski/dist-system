package com.distsystem.test.custom.agent;

import com.distsystem.DistFactory;
import com.distsystem.api.enums.DistEnvironmentType;
import com.distsystem.interfaces.*;
import com.distsystem.utils.DistUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AgentWebApiSimpleTest {
    private static final Logger log = LoggerFactory.getLogger(AgentWebApiSimpleTest.class);

    @Test
    public void agentWebApiTest() {
        log.info("START ------ agent Web API test");
        Agent agent = DistFactory.buildEmptyFactory()
                .withUniverseName("GlobalAgent")
                .withEnvironment(DistEnvironmentType.testing, "test")
                .withWebApiDefaultPort()
                .withServerSocketDefaultPort()
                .withCacheStorageHashMap()
                .withTimerStorageClean(30000)
                .withTimerServerPeriod(30000)
                .withTimerRegistrationPeriod(30000)
                .withCacheStorageJdbc("${JDBC_URL}", "${JDBC_DRIVER}", "${JDBC_USER}", "${JDBC_PASS}")
                .withRegistrationJdbc("${JDBC_URL}", "${JDBC_DRIVER}", "${JDBC_USER}", "${JDBC_PASS}")
                .withConfigReaderJdbc("${JDBC_URL}", "${JDBC_DRIVER}", "${JDBC_USER}", "${JDBC_PASS}")
                .withEnvironmentVariables()
                .withCacheStorageHashMap()
                .withSerializerDefault()
                .createAgentInstance();

        Cache cache = agent.getCache();
        log.info("Cache: " + cache.getGuid());

        AgentSemaphores semaphores = agent.getSemaphores();
        log.info("AgentSemaphores: " + semaphores.getGuid());

        AgentReports reports = agent.getReports();
        log.info("AgentReports: " + reports.getGuid());

        Receiver receiver = agent.getReceiver();
        log.info("Receiver: " + receiver.getGuid());

        AgentFlow flow = agent.getFlow();
        log.info("AgentFlow: " + flow.getGuid());


        for (int i=0; i<10; i++) {
            log.info("SLEEPING................................");
            DistUtils.sleep(30000);
            log.info("========-------------------------------------------------------------------------------------========================");
            log.info("========-----> Agent: " + agent.getAgentInfo());
            log.info("========-------------------------------------------------------------------------------------========================");
        }

        log.info("==================================================================================================//////////////////////////////////////////////////////////////////////////////////////////////////////////////========================");
        log.info("========--------> CLOSING TEST");
        agent.close();
        log.info("END-----");
    }
}
