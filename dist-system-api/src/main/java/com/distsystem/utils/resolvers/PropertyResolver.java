package com.distsystem.utils.resolvers;

import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.Resolver;

import java.util.Optional;
import java.util.Properties;

/** Key-Value resolver with Property object that contains keys with values */
public class PropertyResolver implements Resolver {
    Properties pr;
    public PropertyResolver(Properties pr) {
        this.pr = pr;
    }
    /** get single value for a key */
    public Optional<String> getValue(String key) {
        String value = pr.getProperty(key);
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(value);
    }

    /** connect */
    public void connectAgent(Agent agent) {
    }
}
