package com.payneteasy.superfly.web.security;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;

public class SubsystemAuthenticationConverter implements AuthenticationConverter {

    private static final Logger log = LoggerFactory.getLogger(SubsystemAuthenticationConverter.class);

    @Override
    public Authentication convert(HttpServletRequest request) {
        String subsystemName  = request.getHeader("X-Subsystem-Name");
        String subsystemToken = request.getHeader("X-Subsystem-Token");

        if (subsystemName == null || subsystemName.isBlank()
                || subsystemToken == null || subsystemToken.isBlank()) {
            log.debug("Subsystem headers missing or blank, skipping subsystem auth");
            return null;
        }

        if (containsControlChars(subsystemName) || containsControlChars(subsystemToken)) {
            log.warn("Subsystem headers contain control characters, rejecting");
            return null;
        }

        log.debug("Subsystem auth requested for subsystem={}", subsystemName);
        return new SubsystemAuthenticationToken(subsystemName, subsystemToken);
    }

    private static boolean containsControlChars(String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\r' || c == '\n' || c == '\t' || c < 0x20) {
                return true;
            }
        }
        return false;
    }
}
