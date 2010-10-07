package com.payneteasy.superfly.security;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import org.easymock.EasyMock;
import org.springframework.security.access.intercept.RunAsUserToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.security.authentication.CheckHOTPToken;
import com.payneteasy.superfly.security.authentication.EmptyAuthenticationToken;
import com.payneteasy.superfly.security.authentication.UsernamePasswordAuthRequestInfoAuthenticationToken;

public class SuperflyHOTPAuthenticationProviderTest extends
		AbstractSuperflyAuthenticationProviderTest {
	
	private SuperflyHOTPAuthenticationProvider provider;
	private SSOService ssoService;
	
	public void setUp() {
		ssoService = EasyMock.createMock(SSOService.class);
		provider = new SuperflyHOTPAuthenticationProvider();
		provider.setSsoService(ssoService);
	}
	
	public void testSupports() {
		assertTrue(provider.supports(CheckHOTPToken.class));
		assertFalse(provider.supports(UsernamePasswordAuthenticationToken.class));
		assertFalse(provider.supports(UsernamePasswordAuthRequestInfoAuthenticationToken.class));
		
		provider.setSupportedAuthenticationClass(RunAsUserToken.class);
		assertTrue(provider.supports(RunAsUserToken.class));
		assertFalse(provider.supports(CheckHOTPToken.class));
	}
	
	public void testSuccess() {
		expect(ssoService.authenticateUsingHOTP("pete", "123456"))
				.andReturn(true);
		replay(ssoService);
		Authentication auth = provider.authenticate(createBeforeHotpAuth("123456"));
		assertNotNull(auth);
	}
	
	public void testBadCredentials() {
		expect(ssoService.authenticateUsingHOTP("pete", "123456")).andReturn(false);
		replay(ssoService);
		try {
			provider.authenticate(createBeforeHotpAuth("123456"));
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
			provider.authenticate(createBeforeHotpAuth(null));
			fail();
		} catch (BadCredentialsException e) {
			// expected
		}
	}

	private CheckHOTPToken createBeforeHotpAuth(String hotp) {
		return new CheckHOTPToken(createSSOUser(), hotp);
	}
}
