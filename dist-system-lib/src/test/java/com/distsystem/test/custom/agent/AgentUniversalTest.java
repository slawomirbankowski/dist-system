package com.distsystem.test.custom.agent;

import com.distsystem.DistFactory;
import com.distsystem.api.enums.DistEnvironmentType;
import com.distsystem.interfaces.Agent;
import com.distsystem.utils.DistUtils;
import com.distsystem.utils.resolvers.MapResolver;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AgentUniversalTest {
    private static final Logger log = LoggerFactory.getLogger(AgentUniversalTest.class);

    @Test
    public void agentUniversalJdbcTest() {
        log.info("START ------ agent register JDBC test");

        for (Map.Entry<String, String> e : System.getenv().entrySet()) {
            log.info("ENVIRONMENT PROPERTY: " + e.getKey() + " = " + e.getValue());
        }

        Agent agent1 = DistFactory.buildEmptyFactory()
                .withEnvironment(DistEnvironmentType.development, "dev") // set environment type and name
                .withEnvironmentVariables() // add all environment variables from OS
                .withCommandLineArguments(new String[0]) // add command-line arguments
                .withUniverseName("GlobalAgent") // Universe name - to be used globally - friendly name
                .withAgentName("MyUniqueAgentName") // this Agent's name
                .withAgentParentApplication("Some Application") // name of parent application
                .withScript("") // script with properties
                .withCommonProperties() // common properties like AGENT_HOST_NAME, AGENT_HOST_ADDRESS
                .withProperty("AGENT_SOME_CUSTOM_TO_BE_USED", "some_value")
                .withMap(Map.of("AGENT_SOME_CUSTOM_TO_BE_USED2", "some_value2"))
                .withJson("{}")
                .withPropertiesFile("./agent.properties")
                .withResolver(new MapResolver(Map.of()))
                .withAgentTags(Set.of("very", "nice", "agent"))
                .withWebApiPort(9999)
                // .withWebApiDefaultPort() - default port of Agent Web API
                .withSerializerDefault()
                .withSerializer("") // add custom serializer for classes
                .withDaoJdbc("MAIN", "${JDBC_URL}", "${JDBC_DRIVER}", "${JDBC_USER}", "${JDBC_PASS}")
                .withDaoElastic("EL", "${ELASTICSEARCH_URL}", "${ELASTICSEARCH_USER}", "${ELASTICSEARCH_PASS}")
                .withDaoKafka("KA", "", "", 1, (short)1)
                .withRegistrationJdbc("${JDBC_URL}", "${JDBC_DRIVER}", "${JDBC_USER}", "${JDBC_PASS}")
                .withRegistrationElasticsearch("${ELASTICSEARCH_URL}", "${ELASTICSEARCH_USER}", "${ELASTICSEARCH_PASS}")
                .withRegistrationKafka("localhost:9092", "dist-agent-7-", 1, (short)1)
                .withRegistrationMongodb("${MONGO_HOST}", 8081)
                .withRegisterCleanAfterDefault()
                // .withRegisterCleanAfter(86000000, 86000000)
                .withAuthStorageJdbc("jdbc:postgresql://localhost:5432/cache01", "org.postgresql.Driver",
                        "cache_user", "${JDBC_PASS}")
                .withServerSocketPort(9997)
                .withServerHttpPort(9998)
                // .withServerDatagramPort(9996)
                .withServerDatagramPortDefaultValue()
                .withServerKafka("localhost:9092", "dist-agent-7-server")
                .withCacheStorageHashMap()
                .withCacheStoragePriorityQueue()
                .withCacheStorageWeakHashMap()
                .withCacheObjectTimeToLive(40000)
                .withCacheMaxObjectsAndItems(1000, 100000)
                .withMaxEvents(10000)
                .withMaxIssues(10000)
                .withTimerStorageClean(30000)
                .withTimerRegistrationPeriod(30000)
                .withTimerServerPeriod(30000)
                .withConfigReaderJdbc("jdbc:postgresql://localhost:5432/cache01", "org.postgresql.Driver",
                        "cache_user", "${JDBC_PASS}")
                //.withDaoKafka("KAFKA", "localhost:9092", (short)1, (short)1)
                //.withRegistrationMongodb("localhost", 8081)
                .createAgentInstance();

        assertNotNull(agent1, "Created agent1 should not be null");

        DistUtils.sleep(200);

        log.info("========-----> Agent1: " + agent1.getAgentInfo());

        int maxTime = 30;
        for (int t=0; t<maxTime; t++) {
            log.info("TIME IS RUNNING................................ minutes: " + t + " of " + maxTime);
            DistUtils.sleep(60000);
        }

        log.info("==================================================================================================//////////////////////////////////////////////////////////////////////////////////////////////////////////////========================");
        log.info("========--------> CLOSING TEST");
        agent1.close();

        assertTrue(agent1.isClosed(), "agent1 should be closed");
        log.info("END-----");
    }
}
