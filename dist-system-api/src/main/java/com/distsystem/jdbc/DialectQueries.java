package com.distsystem.jdbc;

/** all queries for JDBC tables */
public enum DialectQueries {
    dialectName,
    selectAllTables,
    selectTable,
    selectCacheTables,
    createDistCacheItemTable,
    createCacheItemIndex,
    selectFindCacheItems,
    selectCacheItemsByKey,
    insertUpdateCacheItem,
    deleteOldCacheItemsTemplate,

    selectAgentRegisterTable,
    createAgentRegister,
    createAgentRegisterIndex,
    selectAgentRegisters,
    selectActiveAgentRegisters,
    updateAgentRegister,
    pingAgentRegister,
    insertAgentRegister,
    removeAgentRegister,
    checkAgentRegisters,
    updateInactiveAgentRegisters,
    deleteInactiveAgentRegisters,

    createAgentConfig,
    createAgentConfigIndex,
    selectAgentConfig,
    deleteAgentConfig,
    insertAgentConfig,

    createAgentServer,
    createAgentServerIndex,
    selectAgentServers,
    selectAgentServersActive,
    selectAgentServersForAgent,
    insertAgentServer,
    deleteAgentServer,
    pingAgentServer,
    checkAgentServer,
    deleteAgentServers,

    createAgentIssue,
    insertAgentIssue,
    selectAgentIssue,

    createAgentService,
    createAgentServiceIndex,
    insertAgentService,
    selectAgentService,
    deleteAgentService


    }