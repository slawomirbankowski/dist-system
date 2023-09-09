package com.distsystem.interfaces;

import com.distsystem.api.auth.AuthAccount;
import com.distsystem.api.auth.AuthCredentials;
import com.distsystem.api.auth.AuthLoginResult;
import com.distsystem.api.auth.AuthPriviligesSet;

import java.util.List;
import java.util.Optional;

/** interface for Auth to provide Authorization in Agent system.
 * */
public interface AgentAuth extends DistService {

    /** login to Dist system with credentials - login and password, email and password, clientid and clientsecret*/
    AuthLoginResult login(AuthCredentials credentials);
    /** find account by name */
    Optional<AuthAccount> findAccount(String accountName);
    /** find account by name */
    List<AuthAccount> searchAccounts(String searchString);
    /** get set of priviliges for selected account */
    AuthPriviligesSet getPrivileges(String accountName);

}
