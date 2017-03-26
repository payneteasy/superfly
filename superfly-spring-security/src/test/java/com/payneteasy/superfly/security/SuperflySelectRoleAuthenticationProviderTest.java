package com.payneteasy.superfly.security;

import com.payneteasy.superfly.security.authentication.EmptyAuthenticationToken;
import com.payneteasy.superfly.security.authentication.SSOUserAndSelectedRoleAuthenticationToken;
import com.payneteasy.superfly.security.authentication.SSOUserAuthenticationToken;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.Assert.*;

public class SuperflySelectRoleAuthenticationProviderTest extends
        AbstractSuperflyAuthenticationProviderTest {
    private AuthenticationProvider provider;

    @Before
    public void setUp() {
        provider = new SuperflySelectRoleAuthenticationProvider();
    }

    @Test
    public void testSupports() {
        assertTrue(provider.supports(SSOUserAndSelectedRoleAuthenticationToken.class));
        assertFalse(provider.supports(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    public void testSuccess() {
        Authentication auth = provider.authenticate(new SSOUserAndSelectedRoleAuthenticationToken(createSSOUserWithOneRole(), createSSORole()));
        assertNotNull(auth);
        assertTrue(auth instanceof SSOUserAuthenticationToken);
    }

    @Test
    public void testUnsupportedAuthentication() {
        assertNull(provider.authenticate(new EmptyAuthenticationToken()));
    }
}
