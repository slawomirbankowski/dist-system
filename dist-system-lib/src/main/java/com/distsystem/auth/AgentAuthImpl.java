package com.distsystem.auth;

import com.distsystem.api.*;
import com.distsystem.api.auth.AuthAccount;
import com.distsystem.api.auth.AuthCredentials;
import com.distsystem.api.auth.AuthLoginResult;
import com.distsystem.api.auth.AuthPriviligesSet;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.base.ServiceBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentAuth;
import com.distsystem.utils.DistUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/** */
public class AgentAuthImpl extends ServiceBase implements AgentAuth {

    /** creates new Auth */
    public AgentAuthImpl(Agent parentAgent) {
        super(parentAgent);

        log.info("--------> Created new auth with GUID: " + guid + ", CONFIG: " + getConfig().getConfigGuid() + ", properties: " + getConfig().getProperties().size());
    }

    /** login to Dist system with credentials - login and password, email and password, clientid and clientsecret*/
    public AuthLoginResult login(AuthCredentials credentials) {
        return new AuthLoginResult();
    }
    /** find account by name */
    public Optional<AuthAccount> findAccount(String accountName) {
        return Optional.empty();
    }
    /** find account by name */
    public List<AuthAccount> searchAccounts(String searchString) {
        return new LinkedList<AuthAccount>();
    }
    /** get set of priviliges for selected account */
    public AuthPriviligesSet getPrivileges(String accountName) {
        return new AuthPriviligesSet();
    }


    @Override
    public DistServiceType getServiceType() {
        return DistServiceType.auth;
    }
    @Override
    public DistMessage processMessage(DistMessage msg) {
        return null;
    }
    @Override
    public AgentWebApiResponse handleRequest(AgentWebApiRequest request) {
        return null;
    }
    @Override
    protected String createServiceUid() {
        return DistUtils.generateCacheGuid();
    }

    @Override
    protected void onClose() {
    }
}
