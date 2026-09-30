package com.payneteasy.superfly.security;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.*;

import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.api.request.AuthenticateRequest;
import com.payneteasy.superfly.api.request.CheckOtpRequest;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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

    @Before
    public void setUp() {
        ssoService = EasyMock.createMock(SSOService.class);
        provider = new SuperflyUsernamePasswordAuthenticationProvider();
        provider.setSsoService(ssoService);
    }

    @Test
    public void testSupports() {
        assertTrue(provider.supports(UsernamePasswordAuthRequestInfoAuthenticationToken.class));
        assertFalse(provider.supports(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    public void testSuccess() {
        SSOUser ssoUserWithOneRole = createSSOUserWithOneRole();
        expect(ssoService.authenticate(new AuthenticateRequest("pete", "secret", null)))
                .andReturn(ssoUserWithOneRole);
        expect(ssoService.checkOtp(new CheckOtpRequest(ssoUserWithOneRole, null))).andReturn(true);
        replay(ssoService);
        Authentication auth = provider.authenticate(createPasswordAuthentication());
        assertNotNull(auth);
    }

    @Test
    public void testSuccessWithGoogleAuthOtp() {
        SSOUser ssoUserWithOneRole = createSSOUserWithOneRole();
        ssoUserWithOneRole.setOtpType(OTPType.GOOGLE_AUTH);
        expect(ssoService.authenticate(new AuthenticateRequest("pete", "secret", null)))
                .andReturn(ssoUserWithOneRole);
        expect(ssoService.checkOtp(new CheckOtpRequest(ssoUserWithOneRole, "123456"))).andReturn(true);
        replay(ssoService);
        Authentication auth = provider.authenticate(createPasswordAuthentication().withSecondFactory("123456"));
        assertNotNull(auth);
    }

    private UsernamePasswordAuthRequestInfoAuthenticationToken createPasswordAuthentication() {
        return new UsernamePasswordAuthRequestInfoAuthenticationToken("pete", "secret", null);
    }

    @Test
    public void testBadCredentials() {
        expect(ssoService.authenticate(new AuthenticateRequest("pete", "bad password", null))).andReturn(null);
        replay(ssoService);
        try {
            provider.authenticate(new UsernamePasswordAuthRequestInfoAuthenticationToken("pete", "bad password", null));
            Assert.fail();
        } catch (BadCredentialsException e) {
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
            provider.authenticate(new UsernamePasswordAuthRequestInfoAuthenticationToken("user", null, null));
            fail();
        } catch (BadCredentialsException e) {
            // expected
        }
    }

    @Test
    public void testNoRoles() {
        expect(ssoService.authenticate(new AuthenticateRequest("pete", "secret", null))).andReturn(createSSOUserWithNoRoles());
        replay(ssoService);
        try {
            provider.authenticate(createPasswordAuthentication());
            fail();
        } catch (BadCredentialsException e) {
            // expected
        }
    }
}
