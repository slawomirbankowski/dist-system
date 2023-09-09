package com.distsystem.interfaces;

/** interface for serializers to serialize data for external cache storages
 * Serialization could be used to save CacheObjects in external storages like JDBC, Redis, Elasticsearch,
 * also it could be used to transfer messages with any Object from Agent to Agent.
 * */
public interface DistSerializer {
    /** serialize Object to byte[] */
    byte[] serialize(Object obj);
    /** deserialize byte[] to Object */
    Object deserialize(String objectClassName, byte[] b);

    /** serialize Object to String */
    String serializeToString(Object obj);
    /** deserialize Object from String */
    Object deserializeFromString(String objectClassName, String str);
}
