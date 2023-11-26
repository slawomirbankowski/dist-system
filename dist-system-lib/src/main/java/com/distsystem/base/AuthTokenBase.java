package com.distsystem.base;

import com.distsystem.api.auth.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class AuthTokenBase {
    /** login to Dist system with credentials - login and password, email and password, clientid and clientsecret*/
    public AuthContext parseToken(String token) {
        return null;
    }

}
