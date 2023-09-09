package com.distsystem.utils;

import com.distsystem.interfaces.Resolver;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** Resolver for properties to change value
 * each resolver can replace key in source configuration value into value.
 * So, configuration given like
 * config_value = 'some ${key1} and ${key2} or ${key3}'
 * Resolver1: key1=value1,
 * Resolver2: key2=value2, key3=value3
 * Final config_value = 'some value1 and value2 or value3'
 *  */
public class ResolverManager {

    /** list of resolvers for given value */
    private List<Resolver> resolvers = new LinkedList<>();

    public ResolverManager() {
    }
    /** add resolver */
    public ResolverManager addResolver(Resolver r) {
        resolvers.add(r);
        return this;
    }
    /** resolve key */
    private List<String> resolveKeyAll(String key) {
        return resolvers.stream().flatMap(res -> res.getValue(key).stream()).collect(Collectors.toList());
    }
    /** resolve key */
    private Optional<String> resolveKeyFirst(String key) {
        return resolvers.stream().flatMap(res -> res.getValue(key).stream()).findFirst();
    }
    /** resolve key */
    private String resolveKey(String key) {
        return resolveKeyFirst(key).orElseGet(() -> key);
    }

    /** resolve string with all value resolvers */
    public String resolve(String value) {
        if (value == null) {
            return null;
        }
        return resolve(value, 0);
    }
    /** resolve string with all value resolvers */
    private String resolve(String value, int depth) {
        if (depth >= 5) {
            return "";
        } else {
            StringBuilder outText = new StringBuilder();
            StringBuilder key = new StringBuilder();
            int pos = 0;
            boolean isKey = false;
            while (pos < value.length()) {
                char currentChar = value.charAt(pos);
                if (currentChar == '$') {
                } else if (pos > 0 && value.charAt(pos - 1) == '$' && value.charAt(pos) == '{') {
                    isKey = true;
                } else if (pos > 0 && value.charAt(pos) == '}') {
                    isKey = false;
                    String keyText = key.toString();
                    String valueRaw = resolveKey(keyText);
                    String valueResolved = resolve(valueRaw, depth + 1);
                    outText.append(valueResolved);
                    key = new StringBuilder();
                } else if (isKey) {
                    key.append(currentChar);
                } else {
                    outText.append(value.charAt(pos));
                }
                pos = pos + 1;
            }
            return outText.toString();
        }
    }
    /** create empty manager for key-value resolvers */
    public static ResolverManager createEmptyManager() {
        return new ResolverManager();
    }
    public static ResolverManager createManager(Resolver... resolvers) {
        ResolverManager m = new ResolverManager();
        Arrays.stream(resolvers).forEach(r -> {
            m.addResolver(r);
        });
        return m;
    }
}
