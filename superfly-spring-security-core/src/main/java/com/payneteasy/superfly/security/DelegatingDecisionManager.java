package com.payneteasy.superfly.security;

import com.payneteasy.superfly.security.exception.InsufficientAuthenticationException;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;

import java.util.Collection;

public class DelegatingDecisionManager implements AccessDecisionManager {

    private AccessDecisionManager delegate;

    public DelegatingDecisionManager() {
    }

    public DelegatingDecisionManager(AccessDecisionManager delegate) {
        this.delegate = delegate;
    }

    public void setDelegate(AccessDecisionManager delegate) {
        this.delegate = delegate;
    }

    protected AccessDecisionManager getDelegate() {
        return delegate;
    }

    public void decide(Authentication authentication, Object object,
            Collection<ConfigAttribute> configAttributes)
            throws AccessDeniedException, InsufficientAuthenticationException {
        delegate.decide(authentication, object, configAttributes);
    }

    public boolean supports(ConfigAttribute attribute) {
        return delegate.supports(attribute);
    }

    public boolean supports(Class<?> clazz) {
        return delegate.supports(clazz);
    }

}
