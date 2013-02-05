package com.payneteasy.superfly.security;

import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.security.authentication.EmptyAuthenticationToken;
import com.payneteasy.superfly.security.authentication.SSOAuthenticationRequest;
import com.payneteasy.superfly.security.authentication.UsernamePasswordAuthRequestInfoAuthenticationToken;
import org.easymock.EasyMock;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

public class SuperflySSOAuthenticationProviderTest extends
		AbstractSuperflyAuthenticationProviderTest {
	
	private SuperflySSOAuthenticationProvider provider;
	private SSOService ssoService;
	
	public void setUp() {
		ssoService = EasyMock.createMock(SSOService.class);
		provider = new SuperflySSOAuthenticationProvider();
		provider.setSsoService(ssoService);
	}
	
	public void testSupports() {
        assertTrue(provider.supports(SSOAuthenticationRequest.class));
		assertFalse(provider.supports(UsernamePasswordAuthRequestInfoAuthenticationToken.class));
		assertFalse(provider.supports(UsernamePasswordAuthenticationToken.class));
	}
	
	public void testSuccess() {
		expect(ssoService.exchangeSubsystemToken("token"))
				.andReturn(createSSOUserWithOneRole());
		replay(ssoService);
		Authentication auth = provider.authenticate(createSSOAuthenticationRequest());
		assertNotNull(auth);
	}

	private SSOAuthenticationRequest createSSOAuthenticationRequest() {
		return new SSOAuthenticationRequest("token");
	}
	
	public void testBadCredentials() {
		expect(ssoService.exchangeSubsystemToken("bad-token")).andReturn(null);
		replay(ssoService);
		try {
			provider.authenticate(new SSOAuthenticationRequest("bad-token"));
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
			provider.authenticate(new SSOAuthenticationRequest(null));
			fail();
		} catch (BadCredentialsException e) {
			// expected
		}
	}
	
	public void testNoRoles() {
		expect(ssoService.exchangeSubsystemToken("token")).andReturn(createSSOUserWithNoRoles());
		replay(ssoService);
		try {
			provider.authenticate(createSSOAuthenticationRequest());
			fail();
		} catch (BadCredentialsException e) {
			// expected
		}
	}
}
