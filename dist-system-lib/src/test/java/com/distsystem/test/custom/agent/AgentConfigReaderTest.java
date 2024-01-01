package com.distsystem.test.custom.agent;

import com.distsystem.DistFactory;
import com.distsystem.interfaces.*;
import com.distsystem.utils.DistUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgentConfigReaderTest {
    private static final Logger log = LoggerFactory.getLogger(AgentConfigReaderTest.class);

    @Test
    @Tag("custom")
    public void agentConfigReaderTest() {
        log.info("START ------ agent config reader test");
        Agent agent = DistFactory.buildEmptyFactory()
                .withUniverseName("GlobalAgent")
                .withConfigReaderJdbc("${JDBC_URL}", "${JDBC_DRIVER}", "${JDBC_USER}", "${JDBC_PASS}")
                .withEnvironmentVariables()
                .withCommonProperties()
                .createAgentInstance();

        for (int i=0; i<6; i++) {
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
