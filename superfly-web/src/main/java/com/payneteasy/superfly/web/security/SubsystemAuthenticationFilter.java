package com.payneteasy.superfly.web.security;

import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.security.handler.JsonAuthenticationFailureHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class SubsystemAuthenticationFilter extends AuthenticationFilter {

    public SubsystemAuthenticationFilter(
            RequestMatcher requiresAuthenticationRequestMatcher,
            AuthenticationManager authenticationManager,
            SubsystemService subsystemService
    ) {
        super(authenticationManager, new SubsystemAuthenticationConverter(subsystemService));
        setRequestMatcher(requiresAuthenticationRequestMatcher);
    }
}
