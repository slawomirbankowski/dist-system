package com.distsystem.test.custom.dao;

import com.distsystem.DistFactory;
import com.distsystem.api.DaoParams;
import com.distsystem.dao.DaoKafkaBase;
import com.distsystem.dao.DaoMongodbBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.utils.DistUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class DaoMongodbTest {
    private static final Logger log = LoggerFactory.getLogger(DaoMongodbTest.class);

    @Test
    @Tag("custom")
    public void mongodbDaoTest() {
        log.info("START ------ MongoDB DAO test");

        Agent agent = DistFactory.buildEmptyFactory()
                .createAgentInstance();
        DaoMongodbBase dao = new DaoMongodbBase(DaoParams.mongoParams("localhost", "27017"), agent);

        log.info("getUrl: " + dao.getUrl());
        log.info("isConnected: " + dao.isConnected());
        log.info("getDaoStructures: " + dao.getDaoStructures());


        DistUtils.sleep(2000);

        dao.close();

        agent.close();

        log.info("END-----");
    }
}
