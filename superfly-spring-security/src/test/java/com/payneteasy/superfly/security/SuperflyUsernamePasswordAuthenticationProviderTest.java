package com.payneteasy.superfly.security;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import org.easymock.EasyMock;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.security.authentication.EmptyAuthenticationToken;
import com.payneteasy.superfly.security.authentication.UsernamePasswordAuthRequestInfoAuthenticationToken;

public class SuperflyUsernamePasswordAuthenticationProviderTest extends
		AbstractSuperflyAuthenticationProviderTest {
	
	private SuperflyUsernamePasswordAuthenticationProvider provider;
	private SSOService ssoService;
	
	public void setUp() {
		ssoService = EasyMock.createMock(SSOService.class);
		provider = new SuperflyUsernamePasswordAuthenticationProvider();
		provider.setSsoService(ssoService);
	}
	
	public void testSupports() {
		assertTrue(provider.supports(UsernamePasswordAuthRequestInfoAuthenticationToken.class));
		assertFalse(provider.supports(UsernamePasswordAuthenticationToken.class));
	}
	
	public void testSuccess() {
		expect(ssoService.authenticate("pete", "secret", null))
				.andReturn(createSSOUser());
		replay(ssoService);
		Authentication auth = provider.authenticate(new UsernamePasswordAuthRequestInfoAuthenticationToken("pete", "secret", null));
		assertNotNull(auth);
	}
	
	public void testBadCredentials() {
		expect(ssoService.authenticate("pete", "bad password", null)).andReturn(null);
		replay(ssoService);
		try {
			provider.authenticate(new UsernamePasswordAuthRequestInfoAuthenticationToken("pete", "bad password", null));
			fail();
		} catch (BadCredentialsException e) {
			// expected
		}
	}
	
	public void testUnsupportedAuthentication() {
		assertNull(provider.authenticate(new EmptyAuthenticationToken()));
	}
	
	public void testNullCredentials() {
		try {
			provider.authenticate(new UsernamePasswordAuthRequestInfoAuthenticationToken("user", null, null));
			fail();
		} catch (BadCredentialsException e) {
			// expected
		}
	}
	
	public void testNoRoles() {
		expect(ssoService.authenticate("pete", "secret", null)).andReturn(createSSOUserWithNoRoles());
		replay(ssoService);
		try {
			provider.authenticate(new UsernamePasswordAuthRequestInfoAuthenticationToken("pete", "secret", null));
			fail();
		} catch (BadCredentialsException e) {
			// expected
		}
	}
}
