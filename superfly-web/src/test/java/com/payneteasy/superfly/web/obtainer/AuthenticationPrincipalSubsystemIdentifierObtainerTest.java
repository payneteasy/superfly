package com.payneteasy.superfly.web.obtainer;

import com.payneteasy.superfly.api.exceptions.SsoAuthException;
import com.payneteasy.superfly.web.security.SubsystemAuthenticationToken;
import org.junit.After;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AuthenticationPrincipalSubsystemIdentifierObtainerTest {

    private final AuthenticationPrincipalSubsystemIdentifierObtainer obtainer =
            new AuthenticationPrincipalSubsystemIdentifierObtainer();

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void nullHintFallsBackToAuthenticatedSubsystem() {
        authenticateSubsystem("billing");
        assertEquals("billing", obtainer.obtainSubsystemIdentifier(null));
    }

    @Test
    public void matchingHintIsReturned() {
        authenticateSubsystem("billing");
        assertEquals("billing", obtainer.obtainSubsystemIdentifier("billing"));
    }

    @Test
    public void foreignHintIsRejectedForSubsystem() {
        authenticateSubsystem("billing");
        try {
            obtainer.obtainSubsystemIdentifier("other");
            fail("expected SsoAuthException");
        } catch (SsoAuthException e) {
            assertTrue(!e.getMessage().contains("other") && !e.getMessage().contains("billing"));
        }
    }

    @Test
    public void foreignHintWithLineBreaksIsRejected() {
        authenticateSubsystem("billing");
        try {
            obtainer.obtainSubsystemIdentifier("x\r\nINFO forged");
            fail("expected SsoAuthException");
        } catch (SsoAuthException expected) {
            // ok
        }
    }

    @Test
    public void localUiUserMayPassAnyHint() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new User("admin", "n/a", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))),
                null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        assertEquals("any-subsystem", obtainer.obtainSubsystemIdentifier("any-subsystem"));
        assertEquals("admin", obtainer.obtainSubsystemIdentifier(null));
    }

    private static void authenticateSubsystem(String name) {
        SecurityContextHolder.getContext().setAuthentication(new SubsystemAuthenticationToken(
                name, null, List.of(new SimpleGrantedAuthority("ROLE_SUBSYSTEM"))));
    }
}
