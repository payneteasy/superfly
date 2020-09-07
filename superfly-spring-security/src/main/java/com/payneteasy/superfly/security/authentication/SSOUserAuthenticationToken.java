package com.payneteasy.superfly.security.authentication;

import com.payneteasy.superfly.api.SSORole;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.security.RoleSource;
import com.payneteasy.superfly.security.StringTransformer;
import com.payneteasy.superfly.security.SuperflyAuthenticationProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Authentication implementation which represents authentication result
 * produced by SuperflyAuthenticationProvider on its last stage.
 * 
 * @author Roman Puchkovskiy
 * @see SuperflyAuthenticationProvider
 */
public class SSOUserAuthenticationToken implements FastAuthentication {
    private static final long serialVersionUID = -8426277290421059196L;

    private SSOUser user;
    private SSORole role;
    private Object credentials;
    private Object details;
    private GrantedAuthority[] authorities;
    private boolean authenticated;
    private Set<String> authorityNames;

    public SSOUserAuthenticationToken(SSOUser user, SSORole role,
            Object credentials, Object details,
            StringTransformer[] transformers, RoleSource roleSource) {
        this.user = user;
        this.role = role;
        this.credentials = credentials;
        this.details = details;

        String[] roles = roleSource.getRoleNames(user, role);
        this.authorities = new GrantedAuthority[roles.length];
        this.authorityNames = new HashSet<String>(roles.length);
        for (int i = 0; i < roles.length; i++) {
            String name = roles[i];
            for (StringTransformer transformer : transformers) {
                name = transformer.transform(name);
            }
            this.authorities[i] = new SimpleGrantedAuthority(name);
            this.authorityNames.add(name);
        }

        this.authenticated = true;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return Arrays.asList(authorities);
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getDetails() {
        return details;
    }

    @Override
    public Object getPrincipal() {
        return user;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated)
            throws IllegalArgumentException {
        this.authenticated = isAuthenticated;
    }

    @Override
    public String getName() {
        return user.getName();
    }

    public SSOUser getUser() {
        return user;
    }

    public SSORole getRole() {
        return role;
    }

    @Override
    public boolean hasAuthority(String name) {
        return authorityNames.contains(name);
    }

}
