package com.distsystem.utils.resolvers;

import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.Resolver;

import java.util.List;
import java.util.Optional;

public class AgentDaoJdbcResolver implements Resolver {
    /** get single value for a key */
    public Optional<String> getValue(String key) {
        return Optional.empty();
    }
    /** get all known keys */
    public List<String> getKnownKeys() {
        return List.of();
    }
}
