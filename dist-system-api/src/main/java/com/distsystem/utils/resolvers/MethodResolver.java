package com.distsystem.utils.resolvers;

import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.Resolver;
import java.util.Optional;
import java.util.function.Function;

/** Key-Value resolver with function to be called to get value for given key */
public class MethodResolver implements Resolver {
    private Function<String, Optional<String>> method;
    public MethodResolver(Function<String, Optional<String>> method) {
        this.method = method;
    }
    /** get single value for a key */
    public Optional<String> getValue(String key) {
        return method.apply(key);
    }

    /** connect */
    public void connectAgent(Agent agent) {
    }
}
