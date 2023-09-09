package com.distsystem.utils.resolvers;

import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.Resolver;

import java.util.Optional;

public class AgentDaoJdbcResolver implements Resolver {
    /** get single value for a key */
    public Optional<String> getValue(String key) {
        return Optional.empty();
    }

    /** connect */
    public void connectAgent(Agent agent) {
    }
}
