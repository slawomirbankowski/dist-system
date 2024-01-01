package com.distsystem.base;

import com.distsystem.api.ServiceObjectParams;
import com.distsystem.api.auth.AuthAccount;
import com.distsystem.api.auth.AuthCredentials;
import com.distsystem.api.auth.AuthLoginResult;
import com.distsystem.api.auth.AuthPriviligesSet;
import com.distsystem.api.dtos.DistAgentAuthAccountRow;
import com.distsystem.api.dtos.DistAgentAuthDomainRow;
import com.distsystem.api.dtos.DistAgentAuthIdentityRow;
import com.distsystem.api.dtos.DistAgentAuthTokenParserRow;
import com.distsystem.interfaces.AgentComponent;
import com.distsystem.interfaces.Agentable;
import com.distsystem.utils.AdvancedMap;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class AuthStorageBase extends ServiceObjectBase implements AgentComponent {

    public AuthStorageBase(ServiceObjectParams params) {
        super(params);
    }

    /** count objects in this agentable object including this object */
    protected long countObjectsAgentable() {
        return 2L;
    }
    /** create account by name and attributes */
    public Optional<DistAgentAuthAccountRow> createAccount(String accountName, String domainName, Map<String, Object> attributes) {
        return Optional.empty();
    }
    /** find account by name */
    public Optional<DistAgentAuthAccountRow> findAccount(String accountName) {
        return Optional.empty();
    }
    /** find account by name */
    public List<DistAgentAuthAccountRow> searchAccounts(String searchString) {
        return new LinkedList<DistAgentAuthAccountRow>();
    }
    /** get all domains */
    public List<DistAgentAuthDomainRow> getDomains() {
        return new LinkedList<DistAgentAuthDomainRow>();
    }
    /** get all domains */
    public List<DistAgentAuthIdentityRow> getIdentities() {
        //
        return new LinkedList<DistAgentAuthIdentityRow>();
    }
    /** get all domains */
    public List<DistAgentAuthTokenParserRow> getTokenParsers() {
        //
        return new LinkedList<DistAgentAuthTokenParserRow>();
    }
    /** create status map from this auth storage */
    public AdvancedMap toStatusMap() {
        return AdvancedMap.create(this).append("authStorageClass", this.getClass().getName());
    }

    /** get set of priviliges for selected account */
    public AuthPriviligesSet getPrivileges(String accountName) {
        return new AuthPriviligesSet();
    }



}
