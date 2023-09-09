package com.distsystem.interfaces;

import java.util.Optional;

/** interface for resolver of values or keys */
public interface Resolver {
    /** get single value for a key */
    Optional<String> getValue(String key);
    /** connect agent to have more options to resolve names into values */
    void connectAgent(Agent agent);
}
