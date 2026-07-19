package com.payneteasy.superfly.web.obtainer;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.payneteasy.superfly.service.impl.remote.SubsystemIdentifierObtainer;

/**
 * Obtains subsystem identifier: uses hint when provided, otherwise falls back
 * to the authenticated principal in the SecurityContext (set by SubsystemAuthenticationFilter
 * via X-Subsystem-Name header).
 */
@Component
public class AuthenticationPrincipalSubsystemIdentifierObtainer implements
        SubsystemIdentifierObtainer {

    public String obtainSubsystemIdentifier(String hint) {
        if (hint != null) {
            return hint;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
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

}
