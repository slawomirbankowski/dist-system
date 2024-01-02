package com.distsystem.test.custom.agent;

import com.distsystem.DistFactory;
import com.distsystem.api.DistCallbacks;
import com.distsystem.api.enums.DistCallbackType;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.api.info.AgentInfo;
import com.distsystem.interfaces.Agent;
import com.distsystem.utils.DistUtils;
import com.distsystem.utils.JsonUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class AgentRegisterSimpleJdbcTest {
    private static final Logger log = LoggerFactory.getLogger(AgentRegisterSimpleJdbcTest.class);

    @Test
    @Tag("custom")
    public void agentRegisterJdbcSimpleTest() {
        log.info("START ------ agent register simple JDBC test");

        Agent agent1 = DistFactory.buildEmptyFactory()
                .withUniverseName("GlobalAgent")
                .withEnvironmentVariables()
                .withAgentNameGenerated()
                .withAgentParentApplication("Some Application")
                .withWebApiDefaultPort()
                .withServerSocketDefaultPort()
                .withServerDatagramPortDefaultValue()
                .withAgentTags(Set.of("app1", "app2", "app3", "app4"))
                .withCacheStorageHashMap()
                .withCacheStoragePriorityQueue()
                .withRegistrationJdbc("${JDBC_URL}", "${JDBC_DRIVER}", "${JDBC_USER}", "${JDBC_PASS}")
                .withTimerStorageClean(30000)
                .withTimerRegistrationPeriod(30000)
                .withTimerServerPeriod(30000)
                .withMaxEvents(10000)
                .withMaxIssues(10000)
                .createAgentInstance();

        DistUtils.sleep(4000);

        int maxTime = 30;
        for (int t=0; t<maxTime; t++) {
            log.info("TIME IS RUNNING................................ minutes: " + t + " of " + maxTime);
            AgentInfo info = agent1.getAgentInfo();
            log.info("========-----> Agent1: " + JsonUtils.serialize(info));
            DistUtils.sleep(60000);
        }

        log.info("==================================================================================================//////////////////////////////////////////////////////////////////////////////////////////////////////////////========================");
        log.info("========--------> CLOSING TEST");
        agent1.close();
        assertTrue(agent1.isClosed(), "agent1 should be closed");
        log.info("END-----");
    }
}
