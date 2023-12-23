package com.distsystem.api.auth;

import java.util.Optional;

/** */
public class AuthLoginResult {
    /** final authentication result */
    private boolean logged;
    /** name of account that has been authenticated */
    private Optional<String> accountName;

    public boolean isLogged() {
        return logged;
    }
    public Optional<String> getAccountName() {
        return accountName;
    }
}
