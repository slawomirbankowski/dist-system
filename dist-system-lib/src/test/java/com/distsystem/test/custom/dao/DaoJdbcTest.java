package com.distsystem.test.custom.dao;

import com.distsystem.DistFactory;
import com.distsystem.dao.DaoJdbcBase;
import com.distsystem.interfaces.Agent;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DaoJdbcTest {
    private static final Logger log = LoggerFactory.getLogger(DaoJdbcTest.class);

    @Test
    public void daoJdbcTest() {
        log.info("START ------ clean test");

        Agent agent = DistFactory.buildEmptyFactory()
                .createAgentInstance();

        DaoJdbcBase dao = new DaoJdbcBase("jdbc:postgresql://localhost:5432/cache01",
                "org.postgresql.Driver",
                "cache_user",
                "${JDBC_PASS}", 2, 5, agent);

        //boolean isConn = dao.isConnected();
        //dao.getIdleConnections();
        //dao.getActiveConnections();
        //dao.executeAnyQuery("create table distcacheitem_tmp(cachekey varchar(4000), cachevalue text, inserteddate timestamp default (now()) )");
        //dao.executeAnyQuery("create index idx_distcacheitem_tmp_cachekey on cacheitems_tmp(cachekey)");
        //dao.executeInsert(new CacheRowJdbc("key222", "value222"), "cacheitems_tmp");

        //dao.isConnected();
        //dao.executeUpdateQuery("insert into distcacheitems_tmp(cachekey,cachevalue,inserteddate) values (?,?,?)", new Object[] {"key1", "value111", new java.util.Date()});
        //dao.executeSelectQuery("select * from cacheitems");
        //dao.executeInsert()
        //log.debug("Connected:" + isConn);
        //assertTrue(, "Connected");
        dao.close();
        log.info("END-----");
    }

}
