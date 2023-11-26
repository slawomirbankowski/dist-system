package com.distsystem.test.agent;

import com.distsystem.DistFactory;
import com.distsystem.api.DistConfig;
import com.distsystem.api.DistConfigGroup;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AgentConfigParserTest {
    private static final Logger log = LoggerFactory.getLogger(AgentConfigParserTest.class);

    @Test
    public void agentConfigReaderTest() {
        log.info("START ------ config parser");
        String configName = "AGENT_REGISTRATION_ELASTICSEARCH_URL";

        DistConfig cfg = DistFactory.buildDefaultFactory()
                .withCommonProperties()
                .withEnvironmentVariables()
                .withConfigReaderJdbc("jdbc", "driver1", "user1", "pass1")
                .withCacheStorageKafka("")
                .withCacheStorageJdbc("jdbc1", "driver2", "user2", "pass2")
                .withCacheStorageElasticsearch("elastc1", "userr1", "pass1")
                .withCacheStorageMongo("mongohost", 4444)
                .withCacheStorageKafka("kafka1", "topic1")
                .extractConfig();

        //log.info("PROPERTIES: " + cfg.toPropertiesString());
        //DistConfigGroup gr = cfg.registerConfigGroup(DistConfig.AGENT_CACHE_STORAGE);

       // gr.getBuckets();


        log.info("END-----");
    }
}
