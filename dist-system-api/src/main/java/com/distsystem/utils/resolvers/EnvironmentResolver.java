package com.distsystem.utils.resolvers;

import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.Resolver;

/** resolver from Map from Environment variables */
public class EnvironmentResolver extends MapResolver implements Resolver {
    public EnvironmentResolver() {
        super(System.getenv());
    }

    /** connect */
    public void connectAgent(Agent agent) {
    }
}
