package com.distsystem.interfaces;

import com.distsystem.api.auth.*;
import com.distsystem.api.dtos.DistAgentAuthAccountRow;

import java.util.List;
import java.util.Optional;

/** interface for Auth to provide Authorization in Agent system.
 * */
public interface AgentAuth extends DistService {

    /** login to Dist system with credentials - login and password, email and password, clientid and clientsecret*/
    AuthLoginResults login(AuthCredentials credentials);
    /** find account by name */
    Optional<DistAgentAuthAccountRow> findAccount(String accountName);
    /** find account by name */
    List<DistAgentAuthAccountRow> searchAccounts(String searchString);
    /** get set of priviliges for selected account */
    AuthPriviligesSet getPrivileges(String accountName);

}
