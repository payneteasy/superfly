package com.payneteasy.superfly.security;

import com.payneteasy.superfly.api.CheckOtpResult;
import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.api.SSOAction;
import com.payneteasy.superfly.api.SSORole;
import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.api.exceptions.SsoConnectionException;
import com.payneteasy.superfly.api.exceptions.SsoDecryptException;
import com.payneteasy.superfly.api.request.AuthenticateRequest;
import com.payneteasy.superfly.api.request.CheckOtpRequest;
import com.payneteasy.superfly.security.authentication.UsernamePasswordAuthRequestInfoAuthenticationToken;
import com.payneteasy.superfly.security.exception.BadOTPValueException;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.Authentication;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

/**
 * A code sent together with the password is checked by the server; when the server cannot check it against the key
 * of the user, the code is refused like a wrong one instead of failing the whole request.
 */
public class SuperflyUsernamePasswordAuthenticationProviderCodeCheckTest {

    private SSOService ssoService;
    private SuperflyUsernamePasswordAuthenticationProvider provider;
    private SSOUser user;

    @Before
    public void setUp() {
        ssoService = EasyMock.createMock(SSOService.class);
        provider = new SuperflyUsernamePasswordAuthenticationProvider();
        provider.setSsoService(ssoService);
        Map<SSORole, SSOAction[]> actions = Collections.singletonMap(new SSORole("role"), new SSOAction[]{});
        user = new SSOUser("pete", actions, Collections.<String, String>emptyMap());
        user.setOtpType(OTPType.GOOGLE_AUTH);
        EasyMock.expect(ssoService.authenticate(new AuthenticateRequest("pete", "secret", null))).andReturn(user);
    }

    private Authentication authenticateWithCode() {
        return provider.authenticate(
                new UsernamePasswordAuthRequestInfoAuthenticationToken("pete", "secret", null).withSecondFactory("123456"));
    }

    @Test
    public void failedDecryptionOfTheKeyIsABadCode() {
        SsoDecryptException failure = new SsoDecryptException("GA master key for pete is null");
        EasyMock.expect(ssoService.checkOtp(new CheckOtpRequest(user, "123456"))).andThrow(failure);
        EasyMock.replay(ssoService);

        try {
            authenticateWithCode();
            fail("the code must be refused");
        } catch (BadOTPValueException e) {
            assertNull(e.getStatus());
            assertEquals("Otp check failed!", e.getMessage());
            assertSame(failure, e.getCause());
        }
    }

    @Test
    public void otherFailuresOfTheServerAreNotACodeProblem() {
        SsoConnectionException failure = new SsoConnectionException("no connection", new RuntimeException("cause"));
        EasyMock.expect(ssoService.checkOtp(new CheckOtpRequest(user, "123456"))).andThrow(failure);
        EasyMock.replay(ssoService);

        try {
            authenticateWithCode();
            fail("an outage must not look like a wrong code");
        } catch (SsoConnectionException e) {
            assertSame(failure, e);
        }
    }

    @Test
    public void refusedCodeCarriesItsStatus() {
        EasyMock.expect(ssoService.checkOtp(new CheckOtpRequest(user, "123456")))
                .andReturn(new CheckOtpResult(CheckOtpResult.Status.INVALID));
        EasyMock.replay(ssoService);

        try {
            authenticateWithCode();
            fail("the code must be refused");
        } catch (BadOTPValueException e) {
            assertEquals(CheckOtpResult.Status.INVALID, e.getStatus());
            assertEquals("Otp check failed!", e.getMessage());
        }
    }

    @Test
    public void acceptedCodeAuthenticatesTheUser() {
        EasyMock.expect(ssoService.checkOtp(new CheckOtpRequest(user, "123456")))
                .andReturn(new CheckOtpResult(CheckOtpResult.Status.SUCCESS));
        EasyMock.replay(ssoService);

        assertNotNull(authenticateWithCode());
    }
}
