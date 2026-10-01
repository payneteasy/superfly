package com.payneteasy.superfly.web.obtainer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.payneteasy.superfly.api.exceptions.SsoAuthException;
import com.payneteasy.superfly.service.impl.remote.SubsystemIdentifierObtainer;
import com.payneteasy.superfly.web.security.SecurityUtils;

/**
 * Obtains subsystem identifier: uses hint when provided, otherwise falls back
 * to the authenticated principal in the SecurityContext (set by SubsystemAuthenticationFilter
 * via X-Subsystem-Name header).
 * <p>
 * When the caller is an authenticated subsystem (ROLE_SUBSYSTEM), a hint naming another
 * subsystem is rejected: a subsystem must not act on behalf of a foreign one.
 * Other principals (e.g. local UI users) may pass any hint.
 */
@Slf4j
@Component
public class AuthenticationPrincipalSubsystemIdentifierObtainer implements
        SubsystemIdentifierObtainer {

    private static final String ROLE_SUBSYSTEM = "ROLE_SUBSYSTEM";

    public String obtainSubsystemIdentifier(String hint) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (hint != null) {
            if (authentication != null && SecurityUtils.isUserInRole(ROLE_SUBSYSTEM)) {
                String authenticated = getUsername(authentication);
                if (!hint.equals(authenticated)) {
                    log.warn("Subsystem {} tried to use foreign subsystem identifier {}",
                            sanitize(authenticated), sanitize(hint));
                    throw new SsoAuthException("Subsystem identifier does not match the authenticated subsystem");
                }
            }
            return hint;
        }
        return getUsername(authentication);
    }

    private String getUsername(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof String) {
            return (String) principal;
        } else if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    // hint comes from the request body: strip line breaks to prevent log injection
    private static String sanitize(String value) {
        return value == null ? null : value.replaceAll("[\\r\\n\\t]", "_");
    }

}
