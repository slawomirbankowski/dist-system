package com.distsystem.base;

import com.distsystem.api.auth.AuthAccount;
import com.distsystem.api.auth.AuthCredentials;
import com.distsystem.api.auth.AuthLoginResult;
import com.distsystem.api.auth.AuthPriviligesSet;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class AuthIdentityBase {
    /** login to Dist system with credentials - login and password, email and password, clientid and clientsecret*/
    public AuthLoginResult login(AuthCredentials credentials) {
        return null;
    }

}
