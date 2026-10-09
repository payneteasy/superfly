package com.payneteasy.superfly.web.security;

import com.payneteasy.superfly.utils.SubsystemTokenHasher;
import com.payneteasy.superfly.web.security.exception.SubsystemNotAllowedHostException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


@Slf4j
public class SubsystemAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService subsystemDetailsService;
    private final UserDetailsChecker userDetailsChecker = new AccountStatusUserDetailsChecker();

    public SubsystemAuthenticationProvider(UserDetailsService subsystemDetailsService) {
        this.subsystemDetailsService = subsystemDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        if (!supports(authentication.getClass())) {
            return null;
        }
        log.debug("Subsystem authentication request for principal={}", sanitize(authentication.getPrincipal()));

        if (authentication.getPrincipal() == null) {
            log.warn("Subsystem auth rejected: no principal in request");
            throw new BadCredentialsException("No subsystem principal found in request.");
        }
        if (authentication.getCredentials() == null) {
            log.warn("Subsystem auth rejected: no token in request");
            throw new BadCredentialsException("No subsystem token found in request.");
        }

        String subsystemName = (String) authentication.getPrincipal();
        String credentials   = (String) authentication.getCredentials();

        if (credentials.isEmpty()) {
            log.warn("Subsystem auth rejected: empty token in request");
            throw new BadCredentialsException("No subsystem token found in request.");
        }

        log.debug("Loading subsystem details for subsystem={}", sanitize(subsystemName));
        UserDetails userDetails;
        try {
            userDetails = subsystemDetailsService.loadUserByUsername(subsystemName);
        } catch (UsernameNotFoundException | SubsystemNotAllowedHostException e) {
            // Намеренно не различаем "не найден" и "неверный токен" — предотвращаем enumeration subsystem-ов
            log.warn("Subsystem auth rejected: invalid token for subsystem={}", sanitize(subsystemName));
            throw new BadCredentialsException("Invalid subsystem token");
        }

        userDetailsChecker.check(userDetails);

        String storedToken = userDetails.getPassword();
        if (!SubsystemTokenHasher.matches(credentials, storedToken)) {
            log.warn("Subsystem auth rejected: invalid token for subsystem={}", sanitize(subsystemName));
            throw new BadCredentialsException("Invalid subsystem token");
        }

        log.info("Subsystem auth granted for subsystem={}", sanitize(subsystemName));
        return new SubsystemAuthenticationToken(
                subsystemName,
                null,
                userDetails.getAuthorities()
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SubsystemAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private static String sanitize(Object value) {
        if (value == null) return null;
        return value.toString().replaceAll("[\\r\\n\\t]", "_");
    }
}
