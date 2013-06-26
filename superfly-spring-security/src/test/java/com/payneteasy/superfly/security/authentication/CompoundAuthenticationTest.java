package com.payneteasy.superfly.security.authentication;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import static org.junit.Assert.*;

public class CompoundAuthenticationTest {
	
	private CompoundAuthentication auth;

    @Before
	public void setUp() {
		auth = new CompoundAuthentication(null);
	}

    @Test
	public void testNotAuthenticated() {
        Assert.assertFalse(auth.isAuthenticated());
	}

    @Test
	public void testEmpty() {
        assertNotNull(auth.getReadyAuthentications());
        assertEquals(0, auth.getReadyAuthentications().length);
		
        assertNull(auth.getLatestReadyAuthentication());
		assertNull(auth.getFirstReadyAuthentication());
	}

    @Test
	public void testNotEmpty() {
		auth.addReadyAuthentication(new UsernamePasswordAuthenticationToken("user", "password"));
		auth.addReadyAuthentication(new PreAuthenticatedAuthenticationToken("user", "credentials"));
		
		assertEquals(2, auth.getReadyAuthentications().length);
        assertTrue(auth.getReadyAuthentications()[0] instanceof UsernamePasswordAuthenticationToken);
		assertTrue(auth.getReadyAuthentications()[1] instanceof PreAuthenticatedAuthenticationToken);
		
		assertTrue(auth.getFirstReadyAuthentication() instanceof UsernamePasswordAuthenticationToken);
		assertTrue(auth.getLatestReadyAuthentication() instanceof PreAuthenticatedAuthenticationToken);
	}
}
