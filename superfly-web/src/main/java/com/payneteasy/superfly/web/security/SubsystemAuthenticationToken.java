package com.payneteasy.superfly.web.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;

import java.io.Serial;
import java.util.Collection;

public class SubsystemAuthenticationToken extends AbstractAuthenticationToken {
    @Serial
    private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

    private final Object principal;

    private final Object credentials;

    public SubsystemAuthenticationToken(Object aPrincipal, Object aCredentials) {
        super(null);
        this.principal = aPrincipal;
        this.credentials = aCredentials;
    }


    public SubsystemAuthenticationToken(Object aPrincipal, Object aCredentials, Collection<? extends GrantedAuthority> anAuthorities) {
        super(anAuthorities);
        this.principal = aPrincipal;
        this.credentials = aCredentials;
        setAuthenticated(true);
    }

    /**
     * Get the credentials
     */
    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    /**
     * Get the principal
     */
    @Override
    public Object getPrincipal() {
        return this.principal;
    }
}
