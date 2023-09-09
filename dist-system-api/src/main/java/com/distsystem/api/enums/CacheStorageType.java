package com.distsystem.api.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** all possible storage types for cache */
public enum CacheStorageType {
    memory, // in memory hash maps, WeakHashMaps, PRiority Lists with maps
    hybrid, // hybrid solution with memory-external
    jdbc, // JDBC based storage writing object to relational databases
    disk, // disk - local or shared
    elasticsearch, // Elasticsearch
    redis, // Redis database
    mongodb, // Mongo DB database
    cassandra, // Cassandra to keep cache objects
    kafka,
    other; // other, unknown, custom implementation

    /** storage types */
    public static final Set<CacheStorageType> allStorages = Collections.unmodifiableSet(Arrays.stream(CacheStorageType.values()).collect(Collectors.toSet()));
    public static final Set<CacheStorageType> internalStorages = Collections.unmodifiableSet(Set.of(memory, hybrid));
    public static final Set<CacheStorageType> externalStorages = Collections.unmodifiableSet(Set.of(jdbc, disk, elasticsearch, redis, mongodb, cassandra, kafka));
    public static final Set<CacheStorageType> unknownStorages = Collections.unmodifiableSet(Set.of(other));
    public static final Set<CacheStorageType> sharedStorages = Collections.unmodifiableSet(Set.of(jdbc, elasticsearch, redis, mongodb, cassandra, kafka));

    /** parse storage types into */
    public static Set<CacheStorageType> parseStorages(String storagesLine) {
        return Arrays.stream(storagesLine.split(","))
                .flatMap(storageTypeName -> getStorageType(storageTypeName).stream())
                .collect(Collectors.toSet());
    }
    /** get type of storage by name or None */
    public static Optional<CacheStorageType> getStorageType(String name) {
        try {
            return Optional.of(CacheStorageType.valueOf(name));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }
}
