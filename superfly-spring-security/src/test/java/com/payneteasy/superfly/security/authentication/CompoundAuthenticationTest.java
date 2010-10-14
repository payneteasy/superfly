package com.payneteasy.superfly.security.authentication;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import junit.framework.TestCase;

public class CompoundAuthenticationTest extends TestCase {
	
	private CompoundAuthentication auth;
	
	public void setUp() {
		auth = new CompoundAuthentication(null);
	}
		
	public void testNotAuthenticated() {
		assertFalse(auth.isAuthenticated());
	}

	public void testEmpty() {
		assertNotNull(auth.getReadyAuthentications());
		assertEquals(0, auth.getReadyAuthentications().length);
		
		assertNull(auth.getLatestReadyAuthentication());
		assertNull(auth.getFirstReadyAuthentication());
	}
	
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
