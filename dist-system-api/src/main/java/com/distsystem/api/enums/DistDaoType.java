package com.distsystem.api.enums;

/** types of DAO classes */
public enum DistDaoType {
    jdbc("com.distsystem.dao.DaoJdbcBase"),
    kafka("com.distsystem.dao.DaoKafkaBase"),
    elasticsearch("com.distsystem.dao.DaoElasticsearchBase"),
    redis("com.distsystem.dao."),
    mongodb("com.distsystem.dao.");

    /** */
    private final String className;

    /** */
    public String getClassName() {
        return className;
    }
    DistDaoType(String className) {
        this.className = className;
    }
}
