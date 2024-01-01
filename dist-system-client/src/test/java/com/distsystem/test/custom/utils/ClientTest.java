package com.distsystem.test.custom.utils;

import com.distsystem.api.AgentWelcomeMessage;
import com.distsystem.api.CacheObject;
import com.distsystem.api.DistMessage;
import com.distsystem.api.DistServiceInfo;
import com.distsystem.api.enums.DistClientType;
import com.distsystem.api.enums.DistMessageType;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.api.info.*;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClientTest {
    private static final Logger log = LoggerFactory.getLogger(ClientTest.class);

    @Test
    public void clientTest() {
        log.info("START ------ client test");



        log.info("END-----");
    }


}

