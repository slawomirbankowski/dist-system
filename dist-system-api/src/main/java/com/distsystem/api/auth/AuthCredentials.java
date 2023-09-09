package com.distsystem.api.auth;

import java.util.Optional;

/** credentials to login user, account, client or application */
public class AuthCredentials {

    private String credentialType;
    private String accountName;
    private String accountSecret;

    public AuthCredentials(String domainName, String userLogin, String userPassword) {

    }
    public AuthCredentials(String clientId, String clientSecret) {

    }

}
