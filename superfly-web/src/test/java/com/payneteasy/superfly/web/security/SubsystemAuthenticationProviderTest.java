package com.payneteasy.superfly.web.security;

import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.SubsystemService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class SubsystemAuthenticationProviderTest {

    private UserDetailsService          userDetailsService;
    private SubsystemAuthenticationProvider provider;

    @Before
    public void setUp() {
        userDetailsService = createMock(UserDetailsService.class);
        provider = new SubsystemAuthenticationProvider(userDetailsService);
    }

    @Test(expected = BadCredentialsException.class)
    public void testNullPrincipalThrows() {
        replay(userDetailsService);
        provider.authenticate(new SubsystemAuthenticationToken(null, "token"));
    }

    @Test(expected = BadCredentialsException.class)
    public void testNullCredentialsThrows() {
        replay(userDetailsService);
        provider.authenticate(new SubsystemAuthenticationToken("subsystem", null));
    }

    @Test(expected = BadCredentialsException.class)
    public void testInvalidTokenThrows() {
        UserDetails details = userWith("subsystem", "correct-token");
        expect(userDetailsService.loadUserByUsername("subsystem")).andReturn(details);
        replay(userDetailsService);

        provider.authenticate(new SubsystemAuthenticationToken("subsystem", "wrong-token"));
    }

    @Test
    public void testValidTokenGrantsAuthenticatedToken() {
        UserDetails details = userWith("subsystem", "valid-token");
        expect(userDetailsService.loadUserByUsername("subsystem")).andReturn(details);
        replay(userDetailsService);

        Authentication result = provider.authenticate(new SubsystemAuthenticationToken("subsystem", "valid-token"));

        verify(userDetailsService);
        assertNotNull(result);
        assertTrue(result.isAuthenticated());
    }

    @Test
    public void testPrincipalIsSubsystemNameNotUserDetails() {
        UserDetails details = userWith("subsystem", "valid-token");
        expect(userDetailsService.loadUserByUsername("subsystem")).andReturn(details);
        replay(userDetailsService);

        Authentication result = provider.authenticate(new SubsystemAuthenticationToken("subsystem", "valid-token"));

        verify(userDetailsService);
        assertFalse("Principal must not expose UserDetails (password leak risk)", result.getPrincipal() instanceof UserDetails);
        assertEquals("Principal must be the subsystem name string", "subsystem", result.getPrincipal());
    }

    @Test
    public void testCredentialsClearedAfterAuthentication() {
        UserDetails details = userWith("subsystem", "valid-token");
        expect(userDetailsService.loadUserByUsername("subsystem")).andReturn(details);
        replay(userDetailsService);

        Authentication result = provider.authenticate(new SubsystemAuthenticationToken("subsystem", "valid-token"));

        verify(userDetailsService);
        assertNull("Credentials must be null after authentication to prevent token retention", result.getCredentials());
    }

    @Test
    public void testUnsupportedTypeReturnsNull() {
        replay(userDetailsService);
        Authentication other = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("u", "p");
        assertNull(provider.authenticate(other));
    }

    @Test
    public void testUnknownAndTokenlessSubsystemsAreIndistinguishableFromWrongToken() {
        // Реальный SubsystemUserDetailsService: он бросает SubsystemNotAllowedHostException,
        // а не UsernameNotFoundException, и текст исключения содержит имя подсистемы.
        SubsystemService subsystemService = createMock(SubsystemService.class);
        LoggerSink loggerSink = niceMock(LoggerSink.class);
        UISubsystem tokenless = new UISubsystem();
        tokenless.setName("tokenless");
        UISubsystem known = new UISubsystem();
        known.setName("known");
        known.setSubsystemToken("valid-token");
        expect(subsystemService.getSubsystemByName("unknown")).andReturn(null);
        expect(subsystemService.getSubsystemByName("tokenless")).andReturn(tokenless);
        expect(subsystemService.getSubsystemByName("known")).andReturn(known);
        replay(subsystemService, loggerSink);
        SubsystemAuthenticationProvider realProvider =
                new SubsystemAuthenticationProvider(new SubsystemUserDetailsService(subsystemService, loggerSink));

        String unknown = failureMessage(realProvider, "unknown");
        String noToken = failureMessage(realProvider, "tokenless");
        String wrongToken = failureMessage(realProvider, "known");

        assertEquals(wrongToken, unknown);
        assertEquals(wrongToken, noToken);
    }

    private static String failureMessage(SubsystemAuthenticationProvider provider, String subsystemName) {
        try {
            provider.authenticate(new SubsystemAuthenticationToken(subsystemName, "wrong-token"));
            fail("authentication must fail for " + subsystemName);
            return null;
        } catch (BadCredentialsException e) {
            return e.getMessage();
        }
    }

    private static UserDetails userWith(String username, String password) {
        return new User(username, password, List.of(new SimpleGrantedAuthority("ROLE_SUBSYSTEM")));
    }
}
