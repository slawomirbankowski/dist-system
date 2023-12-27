package com.distsystem.agent.services;

import com.distsystem.api.*;
import com.distsystem.api.auth.*;
import com.distsystem.api.dtos.DistAgentAuthAccountRow;
import com.distsystem.api.enums.DistServiceType;
import com.distsystem.base.AuthIdentityBase;
import com.distsystem.base.AuthStorageBase;
import com.distsystem.base.AuthTokenBase;
import com.distsystem.base.ServiceBase;
import com.distsystem.interfaces.Agent;
import com.distsystem.interfaces.AgentAuth;
import com.distsystem.utils.DistUtils;
import com.distsystem.utils.DistWebApiProcessor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** service to manage accounts, domains, tokens (create or parse)
 * */
public class AgentAuthImpl extends ServiceBase implements AgentAuth {

    /** auth implementations  */
    private final java.util.concurrent.ConcurrentHashMap<String, AuthStorageBase> auths = new java.util.concurrent.ConcurrentHashMap<>();
    /** all identity providers */
    private final java.util.concurrent.ConcurrentHashMap<String, AuthIdentityBase> identities = new java.util.concurrent.ConcurrentHashMap<>();
    /** */
    private final java.util.concurrent.ConcurrentHashMap<String, DistAgentAuthAccountRow> accounts = new java.util.concurrent.ConcurrentHashMap<>();
    /** all identity providers */
    private final java.util.concurrent.ConcurrentHashMap<String, AuthTokenBase> tokenParsers = new java.util.concurrent.ConcurrentHashMap<>();
    /** */
    private final java.util.concurrent.ConcurrentHashMap<String, AuthPriviligesSet> priviliges = new java.util.concurrent.ConcurrentHashMap<>();

    /** creates new Auth */
    public AgentAuthImpl(Agent parentAgent) {
        super(parentAgent);
        parentAgent.getServices().registerService(this);
    }
    /** additional web API endpoints */
    protected DistWebApiProcessor additionalWebApiProcessor() {
        return new DistWebApiProcessor(getServiceType())
                .addHandlerGet("auth-storages", (m, req) -> req.responseOkJsonSerialize(auths.keySet().stream().toList()));
    }
    /** count objects in this agentable object including this object */
    public long countObjectsService() {
        return 2L;
    }

    /** read configuration and re-initialize this component */
    protected boolean onReinitialize() {
        // TODO: implement reinitialization
        registerConfigGroup(DistConfig.AGENT_AUTH_STORAGE);

        return true;
    }
    /** change values in configuration bucket */
    public DistStatusMap initializeConfigBucket(DistConfigBucket bucket) {
        // TODO: insert, update, delete of bucket
        return createAuthStorage(bucket);
    }
    /** create new auth from bucket */
    public DistStatusMap createAuthStorage(DistConfigBucket bucket) {
        DistStatusMap status = DistStatusMap.create(this);
        String readerClass = DistConfig.AGENT_AUTH_STORAGE_CLASS_MAP.get(bucket.getKey().getConfigType());
        log.info("Create new Auth storage for type: " + bucket.getKey().getConfigType() + ", class: " + readerClass);
        createEvent("createAuthStorage");
        try {
            openCount.incrementAndGet();
            var params = ServiceObjectParams.create(parentAgent, this, readerClass, bucket);
            log.info("Try to initialize auth storage for agent: " + parentAgent.getAgentGuid() + ", class: " + readerClass + ", bucket key: " + bucket.getKey() + ", params: " + params);
            AuthStorageBase authObj = (AuthStorageBase)Class.forName(readerClass)
                    .getConstructor(ServiceObjectParams.class)
                    .newInstance(params);
            auths.put(bucket.getKey().toString(), authObj);

            return status;
        } catch (Exception ex) {
            log.info("Cannot initialize auth storage for agent: "  + parentAgent.getAgentGuid() + ", class: " + readerClass + ", reason: " + ex.getMessage(), ex);
            addIssueToAgent("createAuthStorage", ex);
            return status.exception(ex);
        }
    }

    /** login to Dist system with credentials - login and password, email and password, clientid and clientsecret*/
    public AuthLoginResults login(AgentWebApiRequest request) {

        return login(new AuthCredentials("", "", ""));
    }
    /** login to Dist system with credentials - login and password, email and password, clientid and clientsecret*/
    public AuthLoginResults login(AuthCredentials credentials) {
        var results =  identities.values().stream().map(x -> x.login(credentials)).toList();

        return new AuthLoginResults();
    }
    /** create account by name and attributes */
    public Optional<DistAgentAuthAccountRow> createAccount(String accountName, String domainName, Map<String, Object> attributes) {
        Optional<DistAgentAuthAccountRow> existingAccount = findAccount(accountName);
        if (existingAccount.isEmpty()) {
            Optional<DistAgentAuthAccountRow> createdAccount = auths.values().stream().flatMap(x -> x.createAccount(accountName, domainName, attributes).stream()).findFirst();
            if (createdAccount.isPresent()) {
                accounts.put(accountName, createdAccount.get());
            }
            return createdAccount;
        } else {
            accounts.put(accountName, existingAccount.get());
            return existingAccount;
        }
    }
    /** find account by name */
    public Optional<DistAgentAuthAccountRow> findAccount(String accountName) {
        return auths.values().stream()
                .flatMap(stb -> stb.findAccount(accountName).stream())
                .findFirst();
    }
    /** find account by name */
    public List<DistAgentAuthAccountRow> searchAccounts(String searchString) {
        return auths.values().stream()
                .flatMap(stb -> stb.searchAccounts(searchString).stream())
                .toList();
    }
    /** get set of priviliges for selected account */
    public AuthPriviligesSet getPrivileges(String accountName) {
        return new AuthPriviligesSet();
    }

    @Override
    public DistServiceType getServiceType() {
        return DistServiceType.auth;
    }

    /** get description of this service */
    public String getServiceDescription() {
        return "Authentication and authorization for Agent services and external distributed clients.";
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
