package com.distsystem.utils;

import java.time.LocalDateTime;
import java.util.Map;

/** base class for serializers to serialize data for external cache storages
 * Serialization could be used to save CacheObjects in external storages like JDBC, Redis, Elasticsearch,
 * also it could be used to transfer messages with any Object from Agent to Agent.
 * */
public abstract class DistSerializerBase {

    /** global unique ID */
    private final String guid = DistUtils.generateCustomGuid("SER");
    /** created date */
    private final LocalDateTime createdDate = LocalDateTime.now();
    /** get information about this serializer */
    public Map<String, String> getInfo() {
        return Map.of("className", this.getClass().getName(), "created", createdDate.toString(), "guid", guid);
    }

    /** count objects  */
    public long countObjects() {
        return 2L;
    }
}
