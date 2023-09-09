package com.distsystem.utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class HashMapList<T, V> extends HashMap<T, List<V>> {

    public void add(T key, V value) {
        synchronized (this) {
            List<V> values = this.get(key);
            if (values == null) {
                values = new LinkedList<>();
            }
            values.add(value);
        }
    }
    /** */
    public Optional<V> getFirstOrEmpty(T key) {
        List<V> values = this.get(key);
        if (values == null || values.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(values.get(0));
    }

}
