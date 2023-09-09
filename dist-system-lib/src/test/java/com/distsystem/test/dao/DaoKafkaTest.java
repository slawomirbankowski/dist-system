package com.distsystem.test.dao;

import com.distsystem.DistFactory;
import com.distsystem.api.DaoParams;
import com.distsystem.dao.DaoKafkaBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.utils.DistUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class DaoKafkaTest {
    private static final Logger log = LoggerFactory.getLogger(DaoKafkaTest.class);

    @Test
    public void kafkaDaoTest() {
        log.info("START ------ Kafka DAO test");

        Agent agent = DistFactory.buildEmptyFactory()
                .createAgentInstance();

        DaoKafkaBase dao = new DaoKafkaBase(DaoParams.kafkaParams("localhost:9092", 1, (short) 0x1), agent);
        dao.deleteTopics(Set.of("dist-agent-register-tmp", "dist-agent-register", "dist-agent-ping", "dist-agent-server", "dist-agent-service", "dist-agent-issue"));

        String testTopicName = "dist-agent-register-tmp";
        var topics = dao.getTopics();
        log.info("Got topics: " + topics.size());
        topics.stream().forEach(t -> log.info("---> TOPIC:" + t));

        boolean created = dao.createTopic(testTopicName);
        log.info("Created topic: " + created);

        var topicsAfter = dao.getTopics();
        log.info("Got topics after: " + topicsAfter.size());
        topicsAfter.stream().forEach(t -> log.info("---> TOPIC:" + t));

        String consumerGroup = "kafka-test-dao";
        dao.createKafkaConsumer(testTopicName, consumerGroup, (topic , msg) -> {
            log.info("=======> RECEIVE MESSAGE: " + msg.key() + ", topic: " + msg.topic() + ", value: " + msg.value() + ", offset: " + msg.offset());
            return true;
        });

        for (int i=0; i<50; i++) {
            String key = "agent" + i;
            log.info("=======> SEND MESSAGE: " + key);
            dao.send(testTopicName, key, "agent" + i + "value");
            DistUtils.sleep(10);
        }
        DistUtils.sleep(2000);

        dao.close();

        agent.close();

        log.info("END-----");
    }
}
