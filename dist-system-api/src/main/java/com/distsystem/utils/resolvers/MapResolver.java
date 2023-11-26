package com.distsystem.utils.resolvers;

import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.Resolver;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Key-Value resolver with defined map */
public class MapResolver implements Resolver {
    Map<String, String> map;
    public MapResolver(Map<String, String> map) {
        this.map = map;
    }
    /** get single value for a key */
    public Optional<String> getValue(String key) {
        String value = map.get(key);
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(value);
    }
    /** get all known keys */
    public List<String> getKnownKeys() {
        return map.keySet().stream().toList();
    }
}
