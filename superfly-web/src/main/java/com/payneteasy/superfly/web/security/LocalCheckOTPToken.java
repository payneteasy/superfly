package com.payneteasy.superfly.web.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

import com.payneteasy.superfly.security.authentication.EmptyAuthenticationToken;

/**
 * Request to check an OTP.
 *
 * @author Roman Puchkovskiy
 */
public class LocalCheckOTPToken extends EmptyAuthenticationToken {
    private String username;
    private Collection<GrantedAuthority> authorities;
    private String otp;

    public LocalCheckOTPToken(String username, String otp, Collection<GrantedAuthority> authorities) {
        super();
        this.username = username;
        this.otp = otp;
        this.authorities = authorities;
    }

    @Override
    public String getName() {
        return username;
    }

    @Override
    public Object getCredentials() {
        return otp;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return authorities;
    }


}
