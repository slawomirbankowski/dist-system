package com.distsystem.test.cache;

import com.distsystem.jdbc.JdbcDialect;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CacheDialectTest {
    private static final Logger log = LoggerFactory.getLogger(CacheDialectTest.class);

    @Test
    public void dialectTest() {
        log.info("START ------ agent register test test");
        try {
            JdbcDialect defaultDialect = JdbcDialect.getDialect("", "");
            JdbcDialect postgres = JdbcDialect.getDialect("com.postgresql.Driver", "");

            assertNotNull(defaultDialect, "Default dialect should be non empty");
            assertNotNull(postgres, "Postgres dialect should be non empty");

            Properties props = new Properties();
            props.load(getClass().getClassLoader().getResourceAsStream("default.dialect"));
            log.info("Properties: " + props.size());

        } catch (Exception ex) {
            log.info("Cannot load properties file, reason: " + ex.getMessage());
            ex.printStackTrace();
        }
        log.info("END-----");
    }
}

