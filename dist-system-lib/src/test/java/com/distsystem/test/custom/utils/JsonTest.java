package com.distsystem.test.custom.utils;

import com.distsystem.api.CacheObjectRequest;
import com.distsystem.utils.JsonUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class JsonTest {
    private static final Logger log = LoggerFactory.getLogger(JsonTest.class);

    @Test
    public void jsonSerializationTest() {
        log.info("START ------ agent Web API test");

        CacheObjectRequest cor1 = new CacheObjectRequest("key1", "value1", "priority=3,ttl=100000", Set.of("group1", "group2"));
        log.info("COR1=" + cor1);
        String json = JsonUtils.serialize(cor1);
        log.info("COR_JSON=" + json);
        CacheObjectRequest cor2 = JsonUtils.deserialize(json, CacheObjectRequest.class);
        assertEquals(cor1.toString(), cor2.toString(), "Before and after serialization value should be the same");
        log.info("COR2=" + cor2);


        //DistAgentRegisterRow agentRegisterRow = new DistAgentRegisterRow(LocalDateTime.now(), "AGENT_123", "localhost", "", 9999, LocalDateTime.now(), 1);

        //log.info("REG_OBJ=" + agentRegisterRow);
        //String jsonRegisterRow = JsonUtils.serialize(agentRegisterRow);
       // log.info("REG_JSON=" + jsonRegisterRow);
       // DistAgentRegisterRow agentRegisterRow2 = JsonUtils.deserialize(jsonRegisterRow, DistAgentRegisterRow.class);
       // log.info("REG_DESERIALIZED=" + agentRegisterRow2);

        log.info("END-- ---");
    }
}
