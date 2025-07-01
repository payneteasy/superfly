package com.payneteasy.superfly.web.security;

import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.security.exception.InsufficientAuthenticationException;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.security.exception.SubsystemNotAllowedHostException;
import com.payneteasy.superfly.web.security.exception.SubsystemNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;

import java.net.URI;

public class SubsystemAuthenticationConverter implements AuthenticationConverter {
    private final SubsystemService subsystemService;

    public SubsystemAuthenticationConverter(SubsystemService subsystemService) {
        this.subsystemService = subsystemService;
    }


    @Override
    public Authentication convert(HttpServletRequest request) {
        String subsystemName  = request.getHeader("X-Subsystem-Name");
        String subsystemToken = request.getHeader("X-Subsystem-Token");

        if (subsystemName == null || subsystemToken == null) {
            return null;
        }

        UISubsystem subsystem = subsystemService.getSubsystemByName(subsystemName);
        if (subsystem == null) {
            throw new SubsystemNotFoundException("Subsystem '" + subsystemName + "' not found");
        }

        if (!subsystem.getSubsystemToken().equals(subsystemToken)) {
            throw new SubsystemNotAllowedHostException("Not allowed for your subsystem");
        }
        // checkRequestHost(request, subsystemName);

        return new SubsystemAuthenticationToken(subsystemName, subsystemToken);
    }

    private void checkRequestHost(HttpServletRequest request, UISubsystem subsystem) {

        String landingUrl = subsystem.getLandingUrl();
        String host       = getClientHost(request);

        if (!isHostAllowed(landingUrl, host)) {
            throw new SubsystemNotAllowedHostException("Host not allowed for your subsystem");
        }
    }

    private String getClientHost(HttpServletRequest request) {
        String host = request.getHeader("X-Forwarded-Host");
        return host != null ? host : request.getHeader("Host");
    }

    private boolean isHostAllowed(String landingUrl, String host) {
        if (landingUrl == null || host == null) {
            return false;
        }
        try {
            URI landingUri = new URI(landingUrl);
            URI refererUri = new URI(host);
            return landingUri.getHost().equals(refererUri.getHost());
        } catch (Exception e) {
            return false;
        }
    }
}
