package com.payneteasy.superfly.security_2_0.authentication;

import org.springframework.security.providers.UsernamePasswordAuthenticationToken;

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

    public UsernamePasswordAuthRequestInfoAuthenticationToken(Object principal,
            Object credentials, AuthenticationRequestInfo authRequestInfo) {
        super(principal, credentials);
        this.authRequestInfo = authRequestInfo;
    }

    public AuthenticationRequestInfo getAuthRequestInfo() {
        return authRequestInfo;
    }

}
