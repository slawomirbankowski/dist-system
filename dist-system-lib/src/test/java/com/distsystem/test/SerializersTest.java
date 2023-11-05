package com.distsystem.test;

import com.distsystem.api.enums.DistClientType;
import com.distsystem.api.enums.DistMessageType;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.api.info.*;
import com.distsystem.api.*;
import com.distsystem.interfaces.DistSerializer;
import com.distsystem.serializers.*;
import com.distsystem.test.model.BasicTestObject;
import com.distsystem.utils.DistUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SerializersTest {
    private static final Logger log = LoggerFactory.getLogger(SerializersTest.class);

    @Test
    public void serializerDefinitionTest() {
        log.info("START ------ serialization test for different objects and serializers");
        ComplexSerializer complexSerializer = ComplexSerializer.createComplexSerializer("java.lang.String=StringSerializer,default=ObjectStreamSerializer");
        assertTrue(complexSerializer != null, "Serializer should not be NULL");

        assertEquals(2, complexSerializer.getSerializerKeys().size(), "There should be two serializer keys");
        assertTrue(complexSerializer.getSerializerClasses().contains("com.distsystem.serializers.StringSerializer"), "");

        String agentGuid = "";
        String connectedAgentGuid = "";
        // String agentGuid, LocalDateTime createDate, boolean closed, int serversCount, List<String> servers,int clientsCount, List<String> clients,
        //                     int servicesCount, List<String> services, int registrationsCount, List<String> registrations,
        //                     int timerTasksCount, int threadsCount, int eventsCount, int issuesCount

        AgentConnectorsInfo connectors = new AgentConnectorsInfo(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        List<DistServiceInfo> services = new ArrayList<>();
        List<AgentRegistrationInfo> registrations = new LinkedList<>();
        AgentTimerInfo timers = new AgentTimerInfo("ClassName", 1L, 2, new ArrayList<>());
        DistThreadsInfo threads = new DistThreadsInfo(11, new ArrayList<>());

        var agentInfo = new AgentInfo(agentGuid, agentGuid, agentGuid, LocalDateTime.now(), false,
                Set.of(), List.of(),
                new AgentConfigReaderInfo(0L, 0L, 0L, new LinkedList<>()),
                new AgentMessageProcessorInfo(0, 0, 0),
                new AgentApisInfo(false, 0L, 0L, List.of()),
                connectors, services, new AgentRegistrationsInfo(List.of(), 0, List.of()), timers, threads,
                new AgentDaosInfo(List.of(), Set.of()),
                2, 44);
        // DistClientType clientType, String clientClassName, String url, boolean working, String clientGuid, Set<String> tags, long receivedMessages, long sentMessages
        var clientInfo = new ClientInfo(DistClientType.http, "ClientClassName", "serverUrl", true, "client_guid", Set.of("tag1"), 1, 1);
        AgentWelcomeMessage welcome = new AgentWelcomeMessage(agentInfo, clientInfo);
        DistMessage welcomeMsg = DistMessage.createMessage(DistMessageType.system, agentGuid, DistServiceType.agent, connectedAgentGuid, DistServiceType.agent, "welcome",  welcome);

        log.info("Welcome message: " + welcomeMsg);
        String line = complexSerializer.serializeToString(welcomeMsg);
        log.info("Welcome message serialized: " + line);

        log.info("END-----");
    }


    @Test
    public void serializerWelcomeMessageTest() {
        log.info("START ------ serialization test for different objects and serializers");
        ComplexSerializer complexSerializer = ComplexSerializer.createComplexSerializer("java.lang.String=StringSerializer,default=ObjectStreamSerializer");
        assertTrue(complexSerializer != null, "Serializer should not be NULL");

        assertEquals(2, complexSerializer.getSerializerKeys().size(), "There should be two serializer keys");
        assertTrue(complexSerializer.getSerializerClasses().contains("com.distsystem.serializers.StringSerializer"), "");

        log.info("Serializer keys: " + complexSerializer.getSerializerKeys());
        log.info("Serializer classes: " + complexSerializer.getSerializerClasses());
        log.info("------------------------------------------------------");
        log.info("END-----");
    }


    @Test
    public void serializerAllTest() {
        log.info("START ------ serialization test for different objects and serializers");
        Object[] testObjs = new Object[] {
                "abc",
                "aaaabbbbbbccccccccccccccccccccddddddddddddddddddeeeeeeeeeeeeeee",
                new BasicTestObject("ooooooooooooooooooooo", 1111),
                new AtomicLong(110001101),
                Map.of("kkkkkkkk", "vvvvvvvv"),
                new HashMap<String, String>(),
                List.of("elem1", "elem2", "elem3", "elem4", "elem5"),
                DistUtils.generateShortGuid(),
                DistUtils.randomTable(50, 10000)
        };
        DistSerializer[] serializers = new DistSerializer[] {
                new ObjectStreamSerializer(),
                new ObjectStreamCompressedSerializer(),
                new StringSerializer(),
                new Base64Serializer()
        };
        for (int s=0; s<serializers.length; s++) {
            for (int i=0; i<testObjs.length; i++) {
                Object obj = testObjs[i];
                DistSerializer serializer = serializers[s];
                log.info("------------------------------------------------------");
                log.info("OBJ[" + i + "].serializer=" + serializer.getClass().getSimpleName());
                log.info("OBJ[" + i + "].before=" + obj);
                try {
                    String objClassName = obj.getClass().getName();
                    log.info("OBJ[" + i + "].class=" + objClassName);
                    String serialized = serializer.serializeToString(obj);
                    log.info("OBJ[" + i + "].serialized=" + serialized);
                    Object objDes = serializer.deserializeFromString(objClassName, serialized);
                    log.info("OBJ[" + i + "].after=" + objDes);
                    log.info("OBJ[" + i + "].class=" + objDes.getClass().getName());
                    log.info("OBJ[" + i + "].equalsString=" + obj.toString().equals(objDes.toString()));
                    log.info("OBJ[" + i + "].equalsValue=" + obj.equals(objDes));
                    log.info("OBJ[" + i + "].equalsType=" + objClassName.equals(objDes.getClass().getName()));
                } catch (Exception ex) {
                    log.info("OBJ[" + i + "].exception=" + ex.getMessage());
                }
                log.info("------------------------------------------------------");
            }
        }

        DistSerializer ooSerializer = new ObjectStreamSerializer();
        for (int i=0; i<testObjs.length; i++) {
            Object obj = testObjs[i];
            CacheObject co = new CacheObject("key" + i, obj);
            String coStr = co.serializedFullCacheObjectToString(ooSerializer);
            Optional<CacheObject> coDeser = CacheObject.fromSerializedString(ooSerializer, coStr);
            log.info("Serialized" + co.getValue() + ", deserialized: " + coDeser.get().getValue());
            assertTrue(coDeser.isPresent(), "Deserialized object should be NON NULL");
            assertEquals(co.getValue().getClass().getName(), coDeser.get().getValue().getClass().getName(), "Deserialized object should have the same class");
        }

        log.info("------------------------------------------------------");
        log.info("END-----");
    }

    @Test
    public void serializerClassesTest() {
        log.info("START ------ serialization test for different objects and serializers");
        ComplexSerializer complexSerializer = ComplexSerializer.createComplexSerializer("java.lang.String=StringSerializer,default=ObjectStreamSerializer");
        log.info("------------------------------------------------------");
        log.info("END-----");
    }

}

