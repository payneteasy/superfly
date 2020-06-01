package com.payneteasy.superfly.security.authentication;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.payneteasy.superfly.api.AuthenticationRequestInfo;

/**
 * Extension of UsernamePasswordAuthenticationToken which also allows to
 * transfer AuthenticationRequestInfo instance.
 * 
 * @author Roman Puchkovskiy
 */
public class UsernamePasswordAuthRequestInfoAuthenticationToken extends
        UsernamePasswordAuthenticationToken {
    private static final long serialVersionUID = 9204822041434318662L;

    private AuthenticationRequestInfo authRequestInfo;
    private String secondFactory;

    public UsernamePasswordAuthRequestInfoAuthenticationToken(Object principal,
            Object credentials, AuthenticationRequestInfo authRequestInfo) {
        super(principal, credentials);
        this.authRequestInfo = authRequestInfo;
    }

    public UsernamePasswordAuthRequestInfoAuthenticationToken withSecondFactory(String secondFactory) {
        this.secondFactory = secondFactory;
        return this;
    }

    public String secondFactory() {
        return secondFactory;
    }

    public AuthenticationRequestInfo getAuthRequestInfo() {
        return authRequestInfo;
    }

}
