package com.distsystem.test.agent;

import com.distsystem.utils.ResolverManager;
import com.distsystem.utils.resolvers.EnvironmentResolver;
import com.distsystem.utils.resolvers.MapResolver;
import com.distsystem.utils.resolvers.MethodResolver;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class AgentResolverTest {
    private static final Logger log = LoggerFactory.getLogger(AgentResolverTest.class);

    @Test
    public void agentResolverTest() {
        log.info("START ------ agent resolver test");

        ResolverManager m1 = new ResolverManager();
        m1.addResolver(new MapResolver(Map.of("key1", "value1", "key2", "value2", "key3", "value3")));
        m1.addResolver(new MethodResolver(AgentResolverTest::getValue));
        m1.addResolver(new EnvironmentResolver());

        assertEquals("", m1.resolve(""), "Empty value should be replaced to empty value");
        assertEquals("aaa", m1.resolve("aaa"), "If no key provided - value should be the same");
        assertEquals("c3ilmuoi43uthim34uthxmik3tuhx4kmituhct", m1.resolve("c3ilmuoi43uthim34uthxmik3tuhx4kmituhct"), "If no key provided - value should be the same");
        assertEquals("value1", m1.resolve("${key1}"), "Simple key replacement should work key1->value1");
        assertEquals("value2", m1.resolve("${key2}"), "Simple key replacement should work key2->value2");
        assertEquals("value3", m1.resolve("${key3}"), "Simple key replacement should work key3->value3");
        assertEquals("value4", m1.resolve("${key4}"), "Simple key replacement should work key4->value4");
        assertEquals("value5", m1.resolve("${key5}"), "Simple key replacement should work key5->value5");
        assertEquals("value6", m1.resolve("${key6}"), "Simple key replacement should work key6->value6");
        assertEquals("aaa value3 bbb", m1.resolve("aaa ${key3} bbb"), "Key with values replacement should work ");
        assertEquals("aaavalue1bbbvalue2ccc", m1.resolve("aaa${key1}bbb${key2}ccc"), "");
        //assertEquals("aaavalue1bbbvalue2value4value5...{key6}", m1.resolve("aaa${key1}bbb${key2}${key4}${key5}...{key6}"), "");
        assertEquals("cache_user", m1.resolve("${JDBC_USER}"), "");

        log.info("END-----");
    }
    public static Optional<String> getValue(String key) {
        if (key.equals("key4")) {
            return Optional.of("value4");
        }
        if (key.equals("key5")) {
            return Optional.of("value5");
        }
        if (key.equals("key6")) {
            return Optional.of("value6");
        }
        return Optional.empty();
    }
}
