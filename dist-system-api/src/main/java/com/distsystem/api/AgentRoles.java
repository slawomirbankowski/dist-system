package com.distsystem.api;

import java.util.HashSet;
import java.util.Set;

public class AgentRoles {

    public static String anonymousRole = "anonymous";
    public static String agentRole = "agent";

    public static Set<String> anonymousRoles =Set.of(anonymousRole);
    public static Set<String> normalUserRoles = Set.of(agentRole);

}
