package com.payneteasy.superfly.web.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

import com.payneteasy.superfly.security.authentication.EmptyAuthenticationToken;

/**
 * Represents final authentication which gives access to the system.
 *
 * @author Roman Puchkovskiy
 */
public class FullAuthentication extends EmptyAuthenticationToken {
    private final String                                 username;
    private final Collection<? extends GrantedAuthority> authorities;

    public FullAuthentication(String username,
            Collection<? extends GrantedAuthority> authorities) {
        super();
        this.username = username;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return username;
    }

    @Override
    public Object getPrincipal() {
        return username;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }
}
