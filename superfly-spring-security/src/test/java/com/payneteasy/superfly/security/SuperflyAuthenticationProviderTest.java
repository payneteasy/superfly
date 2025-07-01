package com.payneteasy.superfly.security;

import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.api.request.AuthenticateRequest;
import com.payneteasy.superfly.security.authentication.SSOUserAndSelectedRoleAuthenticationToken;
import com.payneteasy.superfly.security.authentication.SSOUserTransportAuthenticationToken;
import com.payneteasy.superfly.security.authentication.UsernamePasswordAuthRequestInfoAuthenticationToken;
import com.payneteasy.superfly.security.exception.StepTwoException;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.BadCredentialsException;

import static org.easymock.EasyMock.anyObject;
import static org.junit.Assert.*;

public class SuperflyAuthenticationProviderTest extends AbstractSuperflyAuthenticationProviderTest {

    private SuperflyAuthenticationProvider provider;
    private SSOService                     ssoService;

    @Before
    public void setUp() {
        ssoService = EasyMock.createMock(SSOService.class);
        provider = new SuperflyAuthenticationProvider();
        provider.setSsoService(ssoService);
    }

    @Test
    public void testStep1Success() {
        EasyMock.expect(ssoService.authenticate(anyObject(AuthenticateRequest.class)))
                .andReturn(createSSOUserWithOneRole());
        EasyMock.replay(ssoService);
        assertNotNull(provider.authenticate(
                new UsernamePasswordAuthRequestInfoAuthenticationToken(
                        "pete",
                        "secret",
                        null
                ))
        );
        EasyMock.verify(ssoService);
    }

    @Test
    public void testStep1_5Success() {
        try {
            provider.authenticate(new SSOUserTransportAuthenticationToken(createSSOUserWithOneRole()));
            fail();
        } catch (StepTwoException e) {
            // expected
        }
    }

    @Test
    public void testStep2Success() {
        assertNotNull(provider.authenticate(new SSOUserAndSelectedRoleAuthenticationToken(createSSOUserWithOneRole(),
                                                                                          createSSORole()
        )));
    }

    @Test
    public void testBadPassword() {
        try {
            provider.authenticate(new UsernamePasswordAuthRequestInfoAuthenticationToken("cory",
                                                                                         "whatisthepassword",
                                                                                         null
            ));
            fail();
        } catch (BadCredentialsException e) {
            // expected
        }
    }

    @Test
    public void testNoRoles() {
        try {
            provider.authenticate(new UsernamePasswordAuthRequestInfoAuthenticationToken("cory",
                                                                                         "whatisthepassword",
                                                                                         null
            ));
            fail();
        } catch (BadCredentialsException e) {
            // expected
        }
    }

    @Test
    public void testDisabled() {
        provider.setEnabled(false);
        assertNull(provider.authenticate(new UsernamePasswordAuthRequestInfoAuthenticationToken("pete",
                                                                                                "secret",
                                                                                                null
        )));
    }
}
