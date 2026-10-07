package com.payneteasy.superfly.security;

import com.payneteasy.superfly.api.CheckOtpResult;
import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.api.exceptions.SsoDecryptException;
import com.payneteasy.superfly.api.request.CheckOtpRequest;
import com.payneteasy.superfly.security.authentication.CheckOTPToken;
import com.payneteasy.superfly.security.authentication.EmptyAuthenticationToken;
import com.payneteasy.superfly.security.authentication.SSOUserTransportAuthenticationToken;
import com.payneteasy.superfly.security.authentication.UsernamePasswordAuthRequestInfoAuthenticationToken;
import com.payneteasy.superfly.security.exception.BadOTPValueException;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.intercept.RunAsUserToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.junit.Assert.*;

public class SuperflyOTPAuthenticationProviderTest extends
        AbstractSuperflyAuthenticationProviderTest {

    private SuperflyOTPAuthenticationProvider provider;

    @Before
    public void setUp() {
        SSOService ssoService = EasyMock.createMock(SSOService.class);
        provider = new SuperflyOTPAuthenticationProvider();
        provider.setSsoService(ssoService);
    }

    @Test
    public void testSupports() {
        assertTrue(provider.supports(CheckOTPToken.class));
        assertFalse(provider.supports(UsernamePasswordAuthenticationToken.class));
        assertFalse(provider.supports(UsernamePasswordAuthRequestInfoAuthenticationToken.class));

        provider.setSupportedAuthenticationClass(RunAsUserToken.class);
        assertTrue(provider.supports(RunAsUserToken.class));
        assertFalse(provider.supports(CheckOTPToken.class));
    }

    @Test
    public void testSuccess() {
//        expect(ssoService.authenticateUsingHOTP("pete", "123456"))
//                .andReturn(true);
//        replay(ssoService);
//        Authentication auth = provider.authenticate(createBeforeHotpAuth(1, "123456"));
//        assertNotNull(auth);
//        assertTrue(auth instanceof HOTPCheckedToken);
    }

    @Test
    public void testBadCredentials() {
//        expect(ssoService.authenticateUsingHOTP("pete", "123456")).andReturn(false);
//        replay(ssoService);
//        try {
//            provider.authenticate(createBeforeHotpAuth(1, "123456"));
//            fail();
//        } catch (BadOTPValueException e) {
//            // expected
//        }
    }

    @Test
    public void testUnsupportedAuthentication() {
        assertNull(provider.authenticate(new EmptyAuthenticationToken()));
    }

    @Test
    public void testNullCredentials() {
        try {
            provider.authenticate(createBeforeHotpAuth());
            fail();
        } catch (BadOTPValueException e) {
            // expected
        }
    }

    @Test
    public void testFailureCarriesCheckStatus() {
        for (CheckOtpResult.Status status : CheckOtpResult.Status.values()) {
            if (status == CheckOtpResult.Status.SUCCESS) {
                continue;
            }
            SSOService ssoService = EasyMock.createMock(SSOService.class);
            EasyMock.expect(ssoService.checkOtp(EasyMock.anyObject(CheckOtpRequest.class)))
                    .andReturn(new CheckOtpResult(status));
            EasyMock.replay(ssoService);
            SuperflyOTPAuthenticationProvider p = new SuperflyOTPAuthenticationProvider();
            p.setSsoService(ssoService);
            try {
                p.authenticate(new CheckOTPToken(createSSOUser(1), "123456"));
                fail(status.name());
            } catch (BadOTPValueException e) {
                assertEquals(status, e.getStatus());
                assertEquals("Invalid OTP secret", e.getMessage());
            }
        }
    }

    @Test
    public void testNullCredentialsHasNoStatus() {
        try {
            provider.authenticate(createBeforeHotpAuth());
            fail();
        } catch (BadOTPValueException e) {
            assertNull(e.getStatus());
            assertEquals("Null OTP secret", e.getMessage());
        }
    }

    @Test
    public void testDecryptErrorHasNoStatus() {
        SSOService ssoService = EasyMock.createMock(SSOService.class);
        EasyMock.expect(ssoService.checkOtp(EasyMock.anyObject(CheckOtpRequest.class)))
                .andThrow(new SsoDecryptException("decrypt error", new RuntimeException("cause")));
        EasyMock.replay(ssoService);
        SuperflyOTPAuthenticationProvider p = new SuperflyOTPAuthenticationProvider();
        p.setSsoService(ssoService);
        try {
            p.authenticate(new CheckOTPToken(createSSOUser(1), "123456"));
            fail();
        } catch (BadOTPValueException e) {
            assertNull(e.getStatus());
            assertEquals("decrypt error", e.getMessage());
        }
    }

    protected CheckOTPToken createBeforeHotpAuth() {
        return new CheckOTPToken(createSSOUser(1), null);
    }
}
