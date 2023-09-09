package com.distsystem.api.auth;

import java.util.Optional;

/** */
public class AuthLoginResult {
    /** final authentication result */
    private boolean isAuth;
    /** name of account that has been authenticated */
    private Optional<String> accountName;

}
