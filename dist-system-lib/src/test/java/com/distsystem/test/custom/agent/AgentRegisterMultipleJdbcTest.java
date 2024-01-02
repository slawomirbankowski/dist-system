package com.distsystem.test.custom.agent;

import com.distsystem.DistFactory;
import com.distsystem.api.DistCallbacks;
import com.distsystem.api.enums.DistCallbackType;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.interfaces.Agent;
import com.distsystem.utils.DistUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AgentRegisterMultipleJdbcTest {
    private static final Logger log = LoggerFactory.getLogger(AgentRegisterMultipleJdbcTest.class);

    @Test
    @Tag("custom")
    public void agentRegisterJdbcTest() {
        log.info("START ------ agent register JDBC test");

        List<Agent> agents = new LinkedList<>();
        for (int i=0; i<10; i++) {
            Agent agent = DistFactory.buildEmptyFactory()
                    .withUniverseName("GlobalAgent")
                    .withWebApiPort(9999-i*10)
                    .withRegistrationJdbc("${JDBC_URL}", "${JDBC_DRIVER}", "${JDBC_USER}", "${JDBC_PASS}")
                    .withServerSocketPort(9997-i*10)
                    .withServerDatagramPort(9996-i*10)
                    .withEnvironmentVariables()
                    .withSerializerDefault()
                    .withAgentName("Agent_" + i)
                    .withTimerStorageClean(30000)
                    .withTimerRegistrationPeriod(30000)
                    .withTimerServerPeriod(30000)
                    .createAgentInstance();
            agents.add(agent);
            DistUtils.sleep(4000);
        }
        DistUtils.sleep(4000);
        log.info("========--------> AGENTS COUNT: " + agents.size());
        DistUtils.sleep(4000);
        int maxTime = 3;
        for (int t=0; t<maxTime; t++) {
            log.info("TIME IS RUNNING................................ minutes: " + t + " of " + maxTime);
            agents.forEach(a -> {
                log.info("========-----> Agent: " + a.getAgentInfo());
            });
            DistUtils.sleep(60000);
        }
        log.info("==================================================================================================//////////////////////////////////////////////////////////////////////////////////////////////////////////////========================");
        log.info("========--------> CLOSING TEST");
        agents.forEach(a -> {
            log.info("========-----> Closing Agent: " + a.getAgentInfo());
            a.close();
        });
        log.info("END-----");
    }
}
