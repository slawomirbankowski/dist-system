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
    deleteAgentService,

    createConfigInit,
    selectConfigInit,

    // auth - account
    createDistAgentAuthAccount,
    createDistAgentAuthAccountIndex,
    insertDistAgentAuthAccount,
    selectDistAgentAuthAccount,
    selectDistAgentAuthAccountForName,
    deleteDistAgentAuthAccount,
    disableDistAgentAuthAccount,
    // auth - domain
    createDistAgentAuthDomain,
    createDistAgentAuthDomainIndex,
    insertDistAgentAuthDomain,
    selectDistAgentAuthDomain,
    // auth - identity
    createDistAgentAuthIdentity,
    insertDistAgentAuthIdentity,
    selectDistAgentAuthIdentity,
    // auth - role
    createDistAgentAuthRole,
    insertDistAgentAuthRole,
    selectDistAgentAuthRole,
    // auth - key
    createDistAgentAuthKey,
    insertDistAgentAuthKey,
    selectDistAgentAuthKey,
    selectDistAgentAuthKeyForName,
    // auth - token parser
    createDistAgentAuthTokenParser,
    insertDistAgentAuthTokenParser,
    selectDistAgentAuthTokenParser


}