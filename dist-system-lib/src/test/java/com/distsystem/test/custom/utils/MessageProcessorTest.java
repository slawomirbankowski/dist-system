package com.distsystem.test.custom.utils;

import com.distsystem.api.DistMessage;
import com.distsystem.api.DistMessageStatus;
import com.distsystem.utils.DistMessageProcessor;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

public class MessageProcessorTest {
    private static final Logger log = LoggerFactory.getLogger(MessageProcessorTest.class);

    @Test
    public void messageProcessorTest() {
        log.info("START ------ clean test");
        DistMessageProcessor processor = new DistMessageProcessor();
        assertNotNull(processor, "Processor should be not null");
        SampleTestService srv = new SampleTestService();
        assertNotNull(srv, "Service should be not null");
        processor.addMethods(srv);
        assertEquals(processor.getMethodsCount(), 4, "There should be 4 methods registered");
        processor.addMethod("sampleMethod", (mth, msg) -> {
            return msg.response("", DistMessageStatus.ok);
        });
        assertEquals(processor.getMethodsCount(), 5, "There should be 5 methods registered");
        DistMessage msg = DistMessage.createEmpty();
        processor.process("sampleMethod", msg);
        processor.process("sampleMethod", msg);
        processor.process("sampleMethod", msg);
        processor.process("sampleMethod", msg);
        processor.process("sampleMethod", msg);
        processor.process("sampleMethod", msg);
        assertEquals(processor.getReceivedMessagesCount(), 6, "There should be 6 received messages");

        log.info("END-----");
    }
}

class SampleTestService {

    public DistMessage methodOne(DistMessage msg) {
        return msg;
    }
    protected DistMessage methodTwo(DistMessage msg) {
        return msg;
    }
    private DistMessage methodThree(DistMessage msg) {
        return msg;
    }
    DistMessage methodFour(DistMessage msg) {
        return msg;
    }
}