package com.payneteasy.superfly.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.payneteasy.superfly.security.authentication.EmptyAuthenticationToken;
import com.payneteasy.superfly.security.authentication.SSOUserAndSelectedRoleAuthenticationToken;
import com.payneteasy.superfly.security.authentication.SSOUserAuthenticationToken;

public class SuperflySelectRoleAuthenticationProviderTest extends
		AbstractSuperflyAuthenticationProviderTest {
	private AuthenticationProvider provider;
	
	public void setUp() {
		SuperflySelectRoleAuthenticationProvider theProvider = new SuperflySelectRoleAuthenticationProvider();
		provider = theProvider;
	}
	
	public void testSupports() {
		assertTrue(provider.supports(SSOUserAndSelectedRoleAuthenticationToken.class));
		assertFalse(provider.supports(UsernamePasswordAuthenticationToken.class));
	}
	
	public void testSuccess() {
		Authentication auth = provider.authenticate(new SSOUserAndSelectedRoleAuthenticationToken(createSSOUserWithOneRole(), createSSORole()));
		assertNotNull(auth);
		assertTrue(auth instanceof SSOUserAuthenticationToken);
	}
	
	public void testUnsupportedAuthentication() {
		assertNull(provider.authenticate(new EmptyAuthenticationToken()));
	}
}
