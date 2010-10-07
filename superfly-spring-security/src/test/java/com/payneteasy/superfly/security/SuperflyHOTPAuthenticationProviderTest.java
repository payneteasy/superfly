package com.payneteasy.superfly.security;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;

import org.easymock.EasyMock;
import org.springframework.security.access.intercept.RunAsUserToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.security.authentication.CheckHOTPToken;
import com.payneteasy.superfly.security.authentication.EmptyAuthenticationToken;
import com.payneteasy.superfly.security.authentication.HOTPCheckedToken;
import com.payneteasy.superfly.security.authentication.SSOUserAuthenticationToken;
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
		Authentication auth = provider.authenticate(createBeforeHotpAuth(1, "123456"));
		assertNotNull(auth);
		assertTrue(auth instanceof HOTPCheckedToken);
	}
	
	public void testSuccessAndFinalAuthentication() {
		provider.setFinishWithSuperflyFinalAuthentication(true);
		// this is for case when user has exactly one role
		expect(ssoService.authenticateUsingHOTP("pete", "123456"))
				.andReturn(true);
		replay(ssoService);
		Authentication auth = provider.authenticate(createBeforeHotpAuth(1, "123456"));
		assertNotNull(auth);
		assertTrue(auth instanceof SSOUserAuthenticationToken);

		reset(ssoService);
		// this is for case when user has more than one role
		expect(ssoService.authenticateUsingHOTP("pete", "123456"))
				.andReturn(true);
		replay(ssoService);
		auth = provider.authenticate(createBeforeHotpAuth(2, "123456"));
		assertNotNull(auth);
		assertTrue(auth instanceof HOTPCheckedToken);
	}
	
	public void testBadCredentials() {
		expect(ssoService.authenticateUsingHOTP("pete", "123456")).andReturn(false);
		replay(ssoService);
		try {
			provider.authenticate(createBeforeHotpAuth(1, "123456"));
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
			provider.authenticate(createBeforeHotpAuth(1, null));
			fail();
		} catch (BadCredentialsException e) {
			// expected
		}
	}

	protected CheckHOTPToken createBeforeHotpAuth(int roleCount, String hotp) {
		return new CheckHOTPToken(createSSOUser(roleCount), hotp);
	}
}
