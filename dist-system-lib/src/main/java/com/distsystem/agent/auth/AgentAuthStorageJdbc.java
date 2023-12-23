package com.distsystem.agent.auth;

import com.distsystem.api.DaoParams;
import com.distsystem.api.DistConfig;
import com.distsystem.api.ServiceObjectParams;
import com.distsystem.api.auth.AuthPriviligesSet;
import com.distsystem.api.dtos.*;
import com.distsystem.api.enums.DistComponentType;
import com.distsystem.base.AuthStorageBase;
import com.distsystem.dao.DaoJdbcBase;
import com.distsystem.jdbc.JdbcDialect;
import com.distsystem.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** */
public class AgentAuthStorageJdbc extends AuthStorageBase {

    protected static final Logger log = LoggerFactory.getLogger(AgentAuthStorageJdbc.class);

    /** DAO with DBCP to database */
    private DaoJdbcBase dao;
    private JdbcDialect dialect;

    public AgentAuthStorageJdbc(ServiceObjectParams params) {
        super(params);
        componentReinitialize();
    }

    /** read configuration and re-initialize this component */
    public boolean componentReinitialize() {
        log.info(" ========================= Initializing AgentAuthStorageJdbc..................");
        String jdbcUrl = getConfigProperty(DistConfig.URL, "");
        var jdbcDriver = getConfigProperty(DistConfig.DRIVER, "");
        var jdbcUser = getConfigProperty(DistConfig.USER, "");
        var jdbcPass = getConfigProperty(DistConfig.PASS, "");
        var jdbcDialect = getConfigProperty(DistConfig.DIALECT, "default");
        // get dialect by driver class and dialect name
        dialect = JdbcDialect.getDialect(jdbcDriver, jdbcDialect);
        log.info(" ========================= Initializing AgentAuthStorageJdbc with URL: " + jdbcUrl + ", user: " + jdbcUser + ", dialect: " + dialect);
        var initConnections = getConfigPropertyAsInt(DistConfig.INIT_CONNECTIONS, 1);
        var maxActiveConnections = getConfigPropertyAsInt(DistConfig.MAX_ACTIVE_CONNECTIONS, 10);
        DaoParams daoParams = DaoParams.jdbcParams(jdbcUrl, jdbcDriver, jdbcUser, jdbcPass, initConnections, maxActiveConnections);
        dao = getAgent().getAgentDao().getOrCreateDaoOrError(DaoJdbcBase.class, daoParams);
        dao.usedByComponent(this);
        dao.checkAndCreate("distagentauthaccount", dialect.createDistAgentAuthAccount(), dialect.createDistAgentAuthAccountIndex());
        dao.checkAndCreate("distagentauthdomain", dialect.createDistAgentAuthDomain(), dialect.createDistAgentAuthDomainIndex());
        dao.checkAndCreate("distagentauthrole", dialect.createDistAgentAuthRole());
        dao.checkAndCreate("distagentauthkey", dialect.createDistAgentAuthKey());
        dao.checkAndCreate("distagentauthtokenparser", dialect.createDistAgentAuthTokenParser());
        return true;
    }
    /** get type of this component */
    public DistComponentType getComponentType() {
        return DistComponentType.authjdbc;
    }
    /** close this agentable object */
    protected void onClose() {
        if (dao != null) {
            dao.notUsedByComponent(this);
        }
    }
    /** create account by name and attributes */
    public Optional<DistAgentAuthAccountRow> createAccount(String accountName, String domainName, Map<String, Object> attributes) {
        DistAgentAuthAccountRow row = new DistAgentAuthAccountRow(accountName, domainName, JsonUtils.serialize(attributes));
        dao.executeAnyQuery(dialect.insertDistAgentAuthAccount(), row.toInsertRow());
        return findAccount(accountName);
    }
    /** find account by name */
    public Optional<DistAgentAuthAccountRow> findAccount(String accountName) {
        return dao.executeSelectQuery(dialect.selectDistAgentAuthAccountForName(), new Object[] { accountName }, DistAgentAuthAccountRow::fromMap)
                .stream().findFirst();
    }
    /** find account by name */
    public List<DistAgentAuthAccountRow> searchAccounts(String searchString) {
        return dao.executeSelectQuery(dialect.selectDistAgentAuthAccount(), new Object[] { }, DistAgentAuthAccountRow::fromMap);
    }
    /** get all roles */
    public List<DistAgentAuthRoleRow> getRoles() {
        return dao.executeSelectQuery(dialect.selectDistAgentAuthRole(), new Object[0], DistAgentAuthRoleRow::fromMap);
    }

    /** get all domains */
    public List<DistAgentAuthDomainRow> getDomains() {
        return dao.executeSelectQuery(dialect.selectDistAgentAuthDomain(), new Object[0], DistAgentAuthDomainRow::fromMap);
    }
    /** get all identity providers */
    public List<DistAgentAuthIdentityRow> getIdentities() {
        return dao.executeSelectQuery(dialect.selectDistAgentAuthIdentity(), new Object[0], DistAgentAuthIdentityRow::fromMap);
    }

    /** get all token parsers */
    public List<DistAgentAuthTokenParserRow> getTokenParsers() {
        //
        return new LinkedList<DistAgentAuthTokenParserRow>();
    }
    /** get set of priviliges for selected account */
    public AuthPriviligesSet getPrivileges(String accountName) {
        return new AuthPriviligesSet();
    }


}
