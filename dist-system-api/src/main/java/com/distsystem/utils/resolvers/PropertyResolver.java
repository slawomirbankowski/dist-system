package com.distsystem.utils.resolvers;

import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.Resolver;

import java.util.List;
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

    /** get all known keys */
    public List<String> getKnownKeys() {
        return pr.keySet().stream().map(x -> x.toString()).toList();
    }
}
