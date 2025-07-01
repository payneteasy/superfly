package com.payneteasy.superfly.web.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

@Slf4j
public class SubsystemAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService subsystemDetailsService;

    public SubsystemAuthenticationProvider(UserDetailsService subsystemDetailsService) {
        this.subsystemDetailsService = subsystemDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        if (!supports(authentication.getClass())) {
            return null;
        }
        log.debug("Subsystem authentication request: {}", authentication);
        if (authentication.getPrincipal() == null) {
            log.debug("No subsystem principal found in request.");
            throw new BadCredentialsException("No subsystem principal found in request.");
        }
        if (authentication.getCredentials() == null) {
            log.debug("No subsystem token found in request.");
            throw new BadCredentialsException("No subsystem token found in request.");
        }

        String subsystemName = (String) authentication.getPrincipal();
        String credentials   = (String) authentication.getCredentials();

        UserDetails userDetails = subsystemDetailsService.loadUserByUsername(subsystemName);

        if (!userDetails.getPassword().equals(credentials)) {
            throw new BadCredentialsException("Invalid subsystem token");
        }

        return new SubsystemAuthenticationToken(
                userDetails,
                authentication.getCredentials(),
                userDetails.getAuthorities()
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SubsystemAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
