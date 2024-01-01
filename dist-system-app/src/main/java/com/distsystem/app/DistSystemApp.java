package com.distsystem.app;

import com.distsystem.DistFactory;
import com.distsystem.api.enums.DistEnvironmentType;
import com.distsystem.interfaces.Agent;
import com.distsystem.utils.DistUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class DistSystemApp {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(DistSystemApp.class);
    private static String[] commandLineArguments;

    /** */
    public static String[] getCommandLineArguments() {
        return commandLineArguments;
    }
    /** start DistSystem */
    public static void main(String[] args) {
        commandLineArguments = args;
        log.info("STARTING DistSystem REST application on host: " + DistUtils.getCurrentHostName() + "/" + DistUtils.getCurrentHostAddress() + ", GUID: " + DistUtils.getGuid());
        Agent agent = DistFactory.buildEmptyFactory()
                .withCommonProperties()
                .withEnvironment(DistEnvironmentType.production) // set environment type and name
                .withUniverseNameDefault() // Universe name - to be used globally - friendly name
                .withAgentNameGenerated() // this Agent's name
                .withAgentTags(Set.of("dist", "system", "agent"))
                .withSerializerDefault()
                .withRegisterCleanAfterDefault()
                .withCacheStorageHashMap()
                .withCacheStoragePriorityQueue()
                .withCacheObjectTimeToLive(60000)
                .withCacheMaxObjectsAndItems(10000, 100000)
                .withMaxEvents(100000)
                .withMaxIssues(10000)
                .withTimerStorageClean(60000)
                .withTimerRegistrationPeriod(60000)
                .withTimerServerPeriod(60000)
                .withWebApiDefaultPort() // port: 9999
                .withServerHttpPortDefault() // port: 9998
                .withServerSocketDefaultPort() // port: 9997
                .withServerDatagramPortDefaultValue() // port: 9996
                .withEnvironmentVariables()
                .withCommandLineArguments(args)
                .createAgentInstance();
        log.info("New agent initialized: " + agent.getAgentGuid() + ", now DistSystemApp would be waiting till agent will be killed");
        agent.waitTillKill();
        log.info("DistSystem has been killed with agent: " + agent.getAgentGuid() + ", closing DistSystemApp now, good bye!");
    }

}
