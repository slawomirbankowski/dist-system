package com.distsystem.interfaces;

import java.util.List;
import java.util.Optional;

/** interface for resolver of values or keys */
public interface Resolver {
    /** get single value for a key */
    Optional<String> getValue(String key);
    /** get all known keys */
    List<String> getKnownKeys();
}
