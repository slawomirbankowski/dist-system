package com.distsystem.app;

import com.distsystem.DistFactory;
import com.distsystem.api.enums.DistEnvironmentType;
import com.distsystem.interfaces.Agent;
import com.distsystem.utils.DistUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                .withEnvironment(DistEnvironmentType.production, "production")
                .withUniverseNameDefault()
                .withAgentNameGenerated()
                .withPropertiesFile("./agent.properties")
                .withSerializerDefault()
                .withWebApiDefaultPort()
                .withServerSocketDefaultPort()
                .withServerDatagramPortDefaultValue()
                .withServerHttpPortDefault()
                .withRegisterCleanAfterDefault()
                .withEnvironmentVariables()
                .withCommandLineArguments(args)
                .createAgentInstance();
        log.info("New agent initialized: " + agent.getAgentGuid() + ", now DistSystemApp would be waiting till agent will be killed");
        agent.waitTillKill();
        log.info("DistSystem has been killed with agent: " + agent.getAgentGuid() + ", closing DistSystemApp now, good bye!");
    }

}
