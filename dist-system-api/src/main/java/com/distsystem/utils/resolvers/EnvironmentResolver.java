package com.distsystem.utils.resolvers;

import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.Resolver;

import java.util.List;

/** resolver from Map from Environment variables */
public class EnvironmentResolver extends MapResolver implements Resolver {
    public EnvironmentResolver() {
        super(System.getenv());
    }
    /** get all known keys */
    public List<String> getKnownKeys() {
        return System.getenv().keySet().stream().toList();
    }

}
