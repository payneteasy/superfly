package com.payneteasy.superfly.security;

import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.security.authentication.CheckHOTPToken;
import com.payneteasy.superfly.security.authentication.EmptyAuthenticationToken;
import com.payneteasy.superfly.security.authentication.HOTPCheckedToken;
import com.payneteasy.superfly.security.authentication.UsernamePasswordAuthRequestInfoAuthenticationToken;
import com.payneteasy.superfly.security.exception.BadOTPValueException;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.intercept.RunAsUserToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.*;

public class SuperflyHOTPAuthenticationProviderTest extends
		AbstractSuperflyAuthenticationProviderTest {
	
	private SuperflyHOTPAuthenticationProvider provider;
	private SSOService ssoService;

    @Before
	public void setUp() {
		ssoService = EasyMock.createMock(SSOService.class);
		provider = new SuperflyHOTPAuthenticationProvider();
		provider.setSsoService(ssoService);
	}

    @Test
	public void testSupports() {
        assertTrue(provider.supports(CheckHOTPToken.class));
        assertFalse(provider.supports(UsernamePasswordAuthenticationToken.class));
		assertFalse(provider.supports(UsernamePasswordAuthRequestInfoAuthenticationToken.class));
		
		provider.setSupportedAuthenticationClass(RunAsUserToken.class);
		assertTrue(provider.supports(RunAsUserToken.class));
		assertFalse(provider.supports(CheckHOTPToken.class));
	}

    @Test
	public void testSuccess() {
		expect(ssoService.authenticateUsingHOTP("pete", "123456"))
				.andReturn(true);
		replay(ssoService);
		Authentication auth = provider.authenticate(createBeforeHotpAuth(1, "123456"));
        assertNotNull(auth);
		assertTrue(auth instanceof HOTPCheckedToken);
	}

    @Test
	public void testBadCredentials() {
		expect(ssoService.authenticateUsingHOTP("pete", "123456")).andReturn(false);
		replay(ssoService);
		try {
			provider.authenticate(createBeforeHotpAuth(1, "123456"));
            fail();
		} catch (BadOTPValueException e) {
			// expected
		}
	}

    @Test
	public void testUnsupportedAuthentication() {
		assertNull(provider.authenticate(new EmptyAuthenticationToken()));
	}

    @Test
	public void testNullCredentials() {
		try {
			provider.authenticate(createBeforeHotpAuth(1, null));
			fail();
		} catch (BadOTPValueException e) {
			// expected
		}
	}

	protected CheckHOTPToken createBeforeHotpAuth(int roleCount, String hotp) {
		return new CheckHOTPToken(createSSOUser(roleCount), hotp);
	}
}
