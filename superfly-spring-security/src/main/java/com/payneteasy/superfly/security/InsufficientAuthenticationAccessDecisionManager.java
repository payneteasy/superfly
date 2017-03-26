package com.payneteasy.superfly.security;

import java.util.Collection;

import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;

/**
 * {@link AccessDecisionManager} which first checks whether the current
 * {@link Authentication} is sufficient. If not, it throws an
 * {@link InsufficientAuthenticationException}, otherwise just delegates to
 * other {@link AccessDecisionManager}.
 * 
 * @author Roman Puchkovskiy
 */
public class InsufficientAuthenticationAccessDecisionManager extends
        DelegatingDecisionManager {

    private Class<?>[] insufficientAuthenticationClasses;

    public void setInsufficientAuthenticationClasses(Class<?>[] classes) {
        insufficientAuthenticationClasses = classes;
    }

    public InsufficientAuthenticationAccessDecisionManager() {
    }

    public InsufficientAuthenticationAccessDecisionManager(
            AccessDecisionManager delegate) {
        super(delegate);
    }

    @Override
    public void decide(Authentication authentication, Object object,
            Collection<ConfigAttribute> configAttributes)
            throws AccessDeniedException, InsufficientAuthenticationException {
        boolean insufficient = false;
        for (Class<?> clazz : insufficientAuthenticationClasses) {
            if (clazz.isAssignableFrom(authentication.getClass())) {
                insufficient = true;
                break;
            }
        }
        if (insufficient) {
            InsufficientAuthenticationException ex = new InsufficientAuthenticationException(authentication.getClass().getName());
            ex.setAuthentication(authentication);
            throw ex;
        }
        // sufficient authentication, just proceed
        getDelegate().decide(authentication, object, configAttributes);
    }

}
