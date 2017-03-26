package com.payneteasy.superfly.web.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

import com.payneteasy.superfly.security.authentication.EmptyAuthenticationToken;

/**
 * Request to check an HOTP.
 *
 * @author Roman Puchkovskiy
 */
public class LocalCheckHOTPToken extends EmptyAuthenticationToken {
    private String username;
    private Collection<GrantedAuthority> authorities;
    private String hotp;

    public LocalCheckHOTPToken(String username, String hotp, Collection<GrantedAuthority> authorities) {
        super();
        this.username = username;
        this.hotp = hotp;
        this.authorities = authorities;
    }

    @Override
    public String getName() {
        return username;
    }

    @Override
    public Object getCredentials() {
        return hotp;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return authorities;
    }


}
