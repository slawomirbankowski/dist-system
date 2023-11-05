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
import com.distsystem.utils.DistWebApiProcessor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** */
public class AgentAuthImpl extends ServiceBase implements AgentAuth {

    /** processor with functions to handle Web API requests */
    private final DistWebApiProcessor webApiProcessor = new DistWebApiProcessor(getServiceType())
            .addHandlerGet("ping", createTextHandler(param -> "pong"))
            .addHandlerGet("token", createTextHandler(param -> "pong"));

    /** creates new Auth */
    public AgentAuthImpl(Agent parentAgent) {
        super(parentAgent);
        parentAgent.getAgentServices().registerService(this);
        log.info("--------> Created new auth with GUID: " + guid + ", CONFIG: " + getConfig().getConfigGuid() + ", properties: " + getConfig().getProperties().size());
    }

    /** read configuration and re-initialize this component */
    public boolean reinitialize() {
        // TODO: implement reinitialization
        return true;
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

    /** get custom map of info about service */
    @Override
    public Map<String, String> getServiceInfoCustomMap() {
        return Map.of();
    }

    /** update configuration of this Service */
    public void updateConfig(DistConfig newCfg) {
        // TODO: update configuration of this service
    }
    @Override
    protected void onClose() {
    }
}
