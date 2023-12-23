package com.distsystem.test.custom.dao;

import com.distsystem.DistFactory;
import com.distsystem.api.DaoParams;
import com.distsystem.dao.DaoElasticsearchBase;
import com.distsystem.dao.DaoJdbcBase;
import com.distsystem.dao.DaoKafkaBase;
import com.distsystem.interfaces.Agent;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

public class DaoTest {
    private static final Logger log = LoggerFactory.getLogger(DaoTest.class);

    @Test
    public void daoTest() {
        log.info("START ------ DAO test");

        Agent agent = DistFactory.buildEmptyFactory()
                .createAgentInstance();
        String jdbcUrl = "jdbc:postgresql://localhost:5432/cache01";
        String jdbcDriver = "org.postgresql.Driver";
        String jdbcUser = "cache_user";
        String jdbcPass = "${JDBC_PASS}";
        int initConnections = 1;
        int maxActiveConnections = 5;
        log.info("Creating parameters for DAO");
        DaoParams jdbcParams = DaoParams.jdbcParams(jdbcUrl, jdbcDriver, jdbcUser, jdbcPass, initConnections, maxActiveConnections);
        log.info("Creating DAO for JDBC");
        DaoJdbcBase jdbcDao = agent.getAgentDao().getOrCreateDaoOrError(DaoJdbcBase.class, jdbcParams);
        assertNotNull(jdbcDao, "Created DAO should not be null");
        log.info("JDBC DAO2:" + jdbcDao);
        assertTrue(jdbcDao.isConnected(), "Created DAO should be connected");
        assertTrue(jdbcDao.getGuid().length() > 0, "Global Unique ID should not be empty");
        assertEquals(1, agent.getAgentDao().getDaosCount(), "There should be 1 DAO");

        log.info("Get DAO for JDBC");
        DaoJdbcBase jdbcDao2 = agent.getAgentDao().getOrCreateDaoOrError(DaoJdbcBase.class, jdbcParams);
        log.info("DAO2:" + jdbcDao2);
        assertTrue(jdbcDao2.isConnected(), "Created DAO should be connected");
        assertEquals(1, agent.getAgentDao().getDaosCount(), "There should be still 1 DAO");
        log.info("JDBC tables: " + jdbcDao2.getDaoStructures());

        String elasticUrl =  "https://localhost:9200";
        String elasticUser = "elastic";
        String elasticPass = "elastic";
        DaoElasticsearchBase elasticDao = agent.getAgentDao().getOrCreateDaoOrError(DaoElasticsearchBase.class, DaoParams.elasticsearchParams(elasticUrl, elasticUser, elasticPass));
        assertEquals(2, agent.getAgentDao().getDaosCount(), "There should be 2 DAO objects defined");
        assertTrue(elasticDao.isConnected(), "Elasticsearch should be connected");
        log.info("Elasticsearch indices: " + elasticDao.getIndicesNames());

        log.info("Get DAO for Kafka");
        DaoParams kafkaParams = DaoParams.kafkaParams("localhost:9092", 1, (short)1);
        DaoKafkaBase kafkaDao = agent.getAgentDao().getOrCreateDaoOrError(DaoKafkaBase.class, kafkaParams);
        log.info("Kafka topics: " + kafkaDao.getTopics());

        kafkaDao.createKafkaConsumer("dist-agent-issue", "dao-test", (topic, record) -> {
            log.info("Receive message");
            return true;
        });
        agent.close();

        log.info("END-----");
    }
}
