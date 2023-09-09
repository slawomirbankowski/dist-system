package com.distsystem.jdbc;

/** enumeration of tables stored in JDBC compliant database */
public enum JdbcTables {

    /** table to store registration of agent, each agent is registering to inform all agents that new agent is available */
    distagentregister,
    /** table to store configuration used by each agent */
    distagentconfig,
    /** table to store communication servers that are listening */
    distagentserver,
    /** table to store services kept by agent */
    distagentservice,
    /** table to store issues in agents, exceptions */
    distagentissue,

    /** table to store serialized cache objects */
    distcacheitem

}