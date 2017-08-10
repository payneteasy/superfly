package com.payneteasy.superfly.security.spring.internal;

import java.io.Serializable;
import java.util.Set;

public class SecurityContext implements Serializable {


    private final String      username;
    private final Set<String> actions;

    public SecurityContext(String aUsername, Set<String> aActions) {
        username = aUsername;
        actions  = aActions;
    }

    public boolean hasSecureAction(String aSecureAction) {
        return actions.contains(aSecureAction);
    }

    public String getUsername() {
        return username;
    }

    public Set<String> getActions() {
        return actions;
    }

    @Override
    public String toString() {
        return "SecurityContext{" +
                "username='" + username + '\'' +
                ", actions=" + actions +
                '}';
    }
}
