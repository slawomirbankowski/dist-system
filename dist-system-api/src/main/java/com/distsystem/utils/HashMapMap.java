package com.distsystem.utils;

import java.util.*;
import java.util.stream.Collectors;

/** type to store map of maps with easy operations on all maps and elements */
public class HashMapMap<T, K, V> extends java.util.concurrent.ConcurrentHashMap<T, Map<K, V>> {

    public void add(T key1, K key2, V value) {
        synchronized (this) {
            Map<K, V> values = this.computeIfAbsent(key1, k -> new HashMap<>());
            values.put(key2, value);
        }
    }
    /** get Map for key or empty if there is no map for this key */
    public Map<K, V> getOrEmpty(T key1) {
        return getOrDefault(key1, new java.util.concurrent.ConcurrentHashMap<>());
    }
    /** get all values */
    public List<V> getValues(T key1) {
        synchronized (this) {
            return getOrEmpty(key1).values().stream().collect(Collectors.toList());
        }
    }
    /** get all values for all keys */
    public List<V> getAllValues() {
        synchronized (this) {
            return this.values().stream().flatMap(x -> x.values().stream()).collect(Collectors.toList());
        }
    }
    /** get value for key1, key2 OR empty */
    public Optional<V> getValue(T key1, K key2) {
        Map<K, V> m = this.get(key1);
        if (m == null) {
            return Optional.empty();
        }
        V value = m.get(key2);
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(value);
    }
    /** returns total size - sum of all sizes */
    public int totalSize() {
        return this.values().stream().mapToInt(x -> x.size()).sum();
    }

}
