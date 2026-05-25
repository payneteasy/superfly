package com.payneteasy.superfly.web.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class SubsystemAuthenticationFilter extends AuthenticationFilter {

    public SubsystemAuthenticationFilter(
            RequestMatcher requiresAuthenticationRequestMatcher,
            AuthenticationManager authenticationManager
    ) {
        super(authenticationManager, new SubsystemAuthenticationConverter());
        setRequestMatcher(requiresAuthenticationRequestMatcher);
    }
}
