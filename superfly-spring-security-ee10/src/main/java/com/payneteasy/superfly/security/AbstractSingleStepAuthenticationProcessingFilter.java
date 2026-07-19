package com.payneteasy.superfly.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import com.payneteasy.superfly.security.authentication.CompoundAuthentication;

public abstract class AbstractSingleStepAuthenticationProcessingFilter extends
        AbstractAuthenticationProcessingFilter {

    protected AbstractSingleStepAuthenticationProcessingFilter(String defaultFilterProcessesUrl) {
        super(defaultFilterProcessesUrl);
    }

    protected CompoundAuthentication getCompoundAuthenticationOrNewOne(Authentication authentication) {
        if (authentication instanceof CompoundAuthentication) {
            return (CompoundAuthentication) authentication;
        } else {
            return new CompoundAuthentication();
        }
    }

    protected Authentication extractLatestAuthOrSimpleAuth(Authentication authentication) {
        if (authentication instanceof CompoundAuthentication) {
            CompoundAuthentication compound = (CompoundAuthentication) authentication;
            return compound.getLatestReadyAuthentication();
        } else {
            return authentication;
        }
    }

}
