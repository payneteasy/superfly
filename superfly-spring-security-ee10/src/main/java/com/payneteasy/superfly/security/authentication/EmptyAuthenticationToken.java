package com.payneteasy.superfly.security.authentication;

import java.io.Serial;
import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * Authentication implementation which actually does not auth to anything but
 * just used for something else (like value transport).
 *
 * @author Roman Puchkovskiy
 */
public class EmptyAuthenticationToken implements Authentication {
    @Serial
    private static final long serialVersionUID = 5670753128479212097L;

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptySet();
    }

    public Object getCredentials() {
        return null;
    }

    public Object getDetails() {
        return null;
    }

    public Object getPrincipal() {
        return null;
    }

    public boolean isAuthenticated() {
        return false;
    }

    public void setAuthenticated(boolean isAuthenticated)
            throws IllegalArgumentException {
    }

    public String getName() {
        return null;
    }

}
