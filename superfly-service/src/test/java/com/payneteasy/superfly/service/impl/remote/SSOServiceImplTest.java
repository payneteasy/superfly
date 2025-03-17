package com.payneteasy.superfly.service.impl.remote;

import com.payneteasy.superfly.api.SSOAction;
import com.payneteasy.superfly.api.SSORole;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.api.UserDescription;
import com.payneteasy.superfly.api.request.*;
import com.payneteasy.superfly.service.InternalSSOService;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertSame;

public class SSOServiceImplTest {
    private SSOServiceImpl     ssoService;
    private InternalSSOService internalSSOService;

    @Before
    public void setUp() {
        internalSSOService = EasyMock.createMock(InternalSSOService.class);
        ssoService = new SSOServiceImpl(internalSSOService, null, null, null, null);
    }

    @Test
    public void testAuthenticateHOTP() {
        // success
//        expect(internalSSOService.authenticateHOTP(null, "pete", "123456")).andReturn(true);
//        replay(internalSSOService);
//        Assert.assertTrue(ssoService.authenticateUsingHOTP("pete", "123456"));
//        verify(internalSSOService);
//
//        EasyMock.reset(internalSSOService);
//
//        // failure
//        expect(internalSSOService.authenticateHOTP(null, "pete", "123456")).andReturn(false);
//        replay(internalSSOService);
//        Assert.assertFalse(ssoService.authenticateUsingHOTP("pete", "123456"));
//        verify(internalSSOService);
    }

    @Test
    public void testExchangeSubsystemToken() {
        SSOUser user = new SSOUser("pete", Collections.singletonMap(
                new SSORole("test-role"), new SSOAction[]{new SSOAction("test-action", false)}
        ), null);
        expect(internalSSOService.exchangeSubsystemToken("token"))
                .andReturn(user);
        replay(internalSSOService);

        ssoService.exchangeSubsystemToken(
                ExchangeSubsystemTokenRequest
                        .builder()
                        .subsystemToken("token")
                        .build()
        );

        verify(internalSSOService);
    }

    @Test
    public void testTouchSessions() {
        internalSSOService.touchSessions(Arrays.asList(1L, 2L, 3L));
        expectLastCall();
        replay(internalSSOService);
        ssoService.touchSessions(
                TouchSessionsRequest
                        .builder()
                        .sessionIds(Arrays.asList(1L, 2L, 3L))
                        .build()
        );
        verify(internalSSOService);
    }

    @Test
    public void testGetUserDescriptionNotExistingUser() {
        expect(internalSSOService.getUserDescription("no-such-user")).andReturn(null);
        replay(internalSSOService);
        UserDescription user = ssoService.getUserDescription(
                GetUserDescriptionRequest
                        .builder()
                        .username("no-such-user")
                        .build()
        );
        Assert.assertNull(user);
        verify(internalSSOService);
    }

    @Test
    public void testCompleteUser() {
        internalSSOService.completeUser("username");
        expectLastCall();
        replay(internalSSOService);
        ssoService.completeUser(
                CompleteUserRequest
                        .builder()
                        .username("username")
                        .build()
        );
        verify(internalSSOService);
    }

    @Test
    public void testPseudoAuthenticate() {
        SSOUser user = new SSOUser("username", Collections.<SSORole, SSOAction[]>emptyMap(),
                                   Collections.<String, String>emptyMap()
        );
        expect(internalSSOService.pseudoAuthenticate("username", "subsystemIdentifier")).andReturn(user);
        replay(internalSSOService);
        SSOUser user2 = ssoService.pseudoAuthenticate(
                PseudoAuthenticateRequest
                        .builder()
                        .username("username")
                        .subsystemIdentifier("subsystemIdentifier")
                        .build()
        );
        assertSame(user, user2);
        verify(internalSSOService);
    }

    @Test
    public void testChangeUserRole() {
        ssoService.setSubsystemIdentifierObtainer(new SubsystemIdentifierObtainer() {
            @Override
            public String obtainSubsystemIdentifier(String systemIdentifier) {
                return "test";
            }
        });

        internalSSOService.changeUserRole("username", "ROLE_TO", "test");
        expectLastCall();
        replay(internalSSOService);

        ssoService.changeUserRole(
                ChangeUserRoleRequest
                        .builder()
                        .username("username")
                        .newRole("ROLE_TO")
                        .build()
        );

        verify(internalSSOService);
    }

    @Test
    public void testChangeUserRoleWithSubsystemHint() {
        internalSSOService.changeUserRole("username", "ROLE_TO", "test");
        expectLastCall();
        replay(internalSSOService);

        ssoService.changeUserRole(
                ChangeUserRoleRequest
                        .builder()
                        .username("username")
                        .newRole("ROLE_TO")
                        .subsystemHint("test")
                        .build()
        );

        verify(internalSSOService);
    }
}
