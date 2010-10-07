package com.payneteasy.superfly.security;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;

import org.easymock.EasyMock;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.security.authentication.EmptyAuthenticationToken;
import com.payneteasy.superfly.security.authentication.SSOUserAuthenticationToken;
import com.payneteasy.superfly.security.authentication.UsernamePasswordAuthRequestInfoAuthenticationToken;
import com.payneteasy.superfly.security.authentication.UsernamePasswordCheckedToken;

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
				.andReturn(createSSOUserWithOneRole());
		replay(ssoService);
		Authentication auth = provider.authenticate(createPasswordAuthentication());
		assertNotNull(auth);
	}

	private UsernamePasswordAuthRequestInfoAuthenticationToken createPasswordAuthentication() {
		return new UsernamePasswordAuthRequestInfoAuthenticationToken("pete", "secret", null);
	}
	
	public void testSuccessAndFinalAuthentication() {
		provider.setFinishWithSuperflyFinalAuthentication(true);
		// this is for case when user has exactly one role
		expect(ssoService.authenticate("pete", "secret", null))
				.andReturn(createSSOUserWithOneRole());
		replay(ssoService);
		Authentication auth = provider.authenticate(createPasswordAuthentication());
		assertNotNull(auth);
		assertTrue(auth instanceof SSOUserAuthenticationToken);

		reset(ssoService);
		// this is for case when user has more than one role
		expect(ssoService.authenticate("pete", "secret", null))
				.andReturn(createSSOUser(2));
		replay(ssoService);
		auth = provider.authenticate(createPasswordAuthentication());
		assertNotNull(auth);
		assertTrue(auth instanceof UsernamePasswordCheckedToken);
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
			provider.authenticate(createPasswordAuthentication());
			fail();
		} catch (BadCredentialsException e) {
			// expected
		}
	}
}
