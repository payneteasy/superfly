package com.payneteasy.superfly.security.authentication;

import org.springframework.security.core.Authentication;

/**
 * {@link Authentication} which is a request to check a Single Sign-on
 * authentication.
 * 
 * @author Roman Puchkovskiy
 */
public class SSOAuthenticationRequest extends EmptyAuthenticationToken {
    private static final long serialVersionUID = 9204822041434318662L;

    private String subsystemToken;

    public SSOAuthenticationRequest(String subsystemToken) {
        this.subsystemToken = subsystemToken;
    }

    public String getSubsystemToken() {
        return subsystemToken;
    }
}
