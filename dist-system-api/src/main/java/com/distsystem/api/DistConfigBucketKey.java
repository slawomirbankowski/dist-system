package com.distsystem.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** key for configuration bucket that has service, type and instance:
 *  CONFIGREADER_OBJECT_JDBC_PRIMARY
 *  SEMAPHORE_JDBC_SECONDARY
 *  CACHE_JDBC_PRIMARY
 *  REGISTRATION_KAFKA_TETRIARY
 *  */
public class DistConfigBucketKey {
    private static final Logger log = LoggerFactory.getLogger(DistConfigBucketKey.class);

    /** name of service: CACHE, API, DAO, */
    private final String serviceName;
    /** name of configuration type: HTTP, JDBC, KAFKA, ELASTICSEARCH, MONGODB, ...*/
    private final String configType;
    /** name of configuration instance: PRIMARY, SECONDARY, TERTIARY
     * there might be many instances of the same type of configuration
     * */
    private final String configInstance;

    /** */
    public DistConfigBucketKey(String serviceName, String configType, String configInstance) {
        this.serviceName = serviceName;
        this.configType = configType;
        this.configInstance = configInstance;
    }
    /** get name of service for this bucket key */
    public String getServiceName() {
        return serviceName;
    }
    /** get configuration type: JDBC, KAFKA, ELASTICSEARCH, DNS, MONGODB, ... */
    public String getConfigType() {
        return configType;
    }
    /** get configuration instance - PRIMARY, SECONDARY, TETRIARY, ... */
    public String getConfigInstance() {
        return configInstance;
    }
    @Override
    public String toString() {
        return serviceName + "_" + configType + "_" + configInstance;
    }
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
    @Override
    public boolean equals(Object obj) {
        return toString().equals(obj.toString());
    }
}
