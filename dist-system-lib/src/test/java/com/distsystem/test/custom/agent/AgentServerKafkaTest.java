package com.distsystem.test.custom.agent;

import com.distsystem.DistFactory;
import com.distsystem.api.*;
import com.distsystem.api.enums.DistCallbackType;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.Receiver;
import com.distsystem.utils.DistUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

public class AgentServerKafkaTest {
    private static final Logger log = LoggerFactory.getLogger(AgentServerKafkaTest.class);

    @Test
    public void agentKafkaServerTest() {
        log.info("START ------ agent server Kafka test");
        Agent agent1 = DistFactory.buildEmptyFactory()
                .withUniverseName("GlobalAgent")
                .withRegistrationJdbc("${JDBC_URL}", "${JDBC_DRIVER}", "${JDBC_USER}", "${JDBC_PASS}")
                .withRegisterCleanAfter(CacheMode.TIME_FIVE_MINUTES, CacheMode.TIME_ONE_DAY)
                .withServerKafka("localhost:9092", "dist-agent-5-")
                .withSerializer("java.lang.String=StringSerializer,default=ObjectStreamSerializer")
                .withTimerRegistrationPeriod(1000)
                .withTimerServerPeriod(1000)
                .createAgentInstance();

        Agent agent2 = DistFactory.buildEmptyFactory()
                .withUniverseName("GlobalAgent")
                .withRegistrationJdbc("${JDBC_URL}", "${JDBC_DRIVER}", "${JDBC_USER}", "${JDBC_PASS}")
                .withRegisterCleanAfter(CacheMode.TIME_FIVE_MINUTES, CacheMode.TIME_ONE_DAY)
                .withServerKafka("localhost:9092", "dist-agent-5-")
                .withSerializer("java.lang.String=StringSerializer,default=ObjectStreamSerializer")
                .withTimerRegistrationPeriod(1000)
                .withTimerServerPeriod(1000)
                .createAgentInstance();

        assertNotNull(agent1, "Created agent should not be null");
        assertNotNull(agent2, "Created agent should not be null");

        DistUtils.sleep(3000);

        Receiver receiver = agent1.getReceiver();
        receiver.registerReceiverMethod("custom", msg -> {
            log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! Receive message: " + msg.getMessage());
            return msg.response("custom_pong", DistMessageStatus.ok);
        });

        agent1.sendMessageBroadcast(DistServiceType.agent, DistServiceType.agent, "ping", "ping", DistCallbacks.createEmpty().addCallback(DistCallbackType.onResponse, x -> { System.out.println("GOT PONG"); return true; } ));
        var msgPing = DistMessageBuilder.empty()
                .fromService(agent1.getCache())
                .toDestination(agent2.getAgentGuid(), DistServiceType.agent, "custom")
                .withObject("ping")
                .build();
        agent1.sendMessage(msgPing);

        log.info("Sent messages, now waiting to receive......");

        DistUtils.sleep(6000);

        agent1.close();
        agent2.close();

        assertTrue(agent1.isClosed(), "agent1 should be closed");
        assertTrue(agent2.isClosed(), "agent2 should be closed");

        log.info("END-----");
    }
}
