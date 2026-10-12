package com.payneteasy.superfly.service.impl.remote;

import com.payneteasy.superfly.api.SSOAction;
import com.payneteasy.superfly.api.SSORole;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.api.UserDescription;
import com.payneteasy.superfly.api.request.*;
import com.payneteasy.superfly.model.ui.user.UserForDescription;
import com.payneteasy.superfly.service.InternalSSOService;
import com.payneteasy.superfly.service.LoggerSink;
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
        ssoService = new SSOServiceImpl(internalSSOService, null, null, null, null, createNiceMock(LoggerSink.class));
    }

    // username-based methods only work for users of the caller's subsystem
    private void expectCallerOwnsUser(String username) {
        ssoService.setSubsystemIdentifierObtainer(hint -> "caller");
        expect(internalSSOService.isUserAccessibleFrom(username, "caller")).andReturn(true);
    }

    private void expectCallerMayChangeUser(String username) {
        ssoService.setSubsystemIdentifierObtainer(hint -> "caller");
        expect(internalSSOService.isUserManageableFrom(username, "caller")).andReturn(true);
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
        ssoService.setSubsystemIdentifierObtainer(hint -> "caller");
        SSOUser user = new SSOUser("pete", Collections.singletonMap(
                new SSORole("test-role"), new SSOAction[]{new SSOAction("test-action", false)}
        ), null);
        expect(internalSSOService.exchangeSubsystemToken("token", "caller"))
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
        ssoService.setSubsystemIdentifierObtainer(hint -> "caller");
        internalSSOService.touchSessions(Arrays.asList(1L, 2L, 3L), "caller");
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
        expectCallerOwnsUser("no-such-user");
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

    private static UserForDescription storedUser() {
        UserForDescription user = new UserForDescription();
        user.setUsername("pete");
        user.setSecretQuestion("question");
        user.setSecretAnswer("stored-answer");
        return user;
    }

    @Test
    public void testGetUserDescriptionDoesNotReturnSecretAnswer() {
        expectCallerOwnsUser("pete");
        expect(internalSSOService.getUserDescription("pete")).andReturn(storedUser());
        replay(internalSSOService);
        UserDescription user = ssoService.getUserDescription(
                GetUserDescriptionRequest.builder().username("pete").build());
        Assert.assertEquals("question", user.getSecretQuestion());
        Assert.assertNull(user.getSecretAnswer());
        verify(internalSSOService);
    }

    @Test
    public void testUpdateUserDescriptionWithoutSecretAnswerKeepsStoredOne() throws Exception {
        expectCallerMayChangeUser("pete");
        UserForDescription stored = storedUser();
        expect(internalSSOService.getUserDescription("pete")).andReturn(stored);
        internalSSOService.updateUserForDescription(stored);
        replay(internalSSOService);
        UserDescription update = new UserDescription();
        update.setUsername("pete");
        update.setSecretQuestion("new question");
        ssoService.updateUserDescription(new UpdateUserDescriptionRequest(update));
        Assert.assertEquals("new question", stored.getSecretQuestion());
        Assert.assertEquals("stored-answer", stored.getSecretAnswer());
        verify(internalSSOService);
    }

    @Test
    public void testUpdateUserDescriptionWithSecretAnswerReplacesIt() throws Exception {
        expectCallerMayChangeUser("pete");
        UserForDescription stored = storedUser();
        expect(internalSSOService.getUserDescription("pete")).andReturn(stored);
        internalSSOService.updateUserForDescription(stored);
        replay(internalSSOService);
        UserDescription update = new UserDescription();
        update.setUsername("pete");
        update.setSecretAnswer("new-answer");
        ssoService.updateUserDescription(new UpdateUserDescriptionRequest(update));
        Assert.assertEquals("new-answer", stored.getSecretAnswer());
        verify(internalSSOService);
    }

    @Test
    public void testCompleteUser() {
        expectCallerMayChangeUser("username");
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

        expect(internalSSOService.isUserManageableFrom("username", "test")).andReturn(true);
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
        ssoService.setSubsystemIdentifierObtainer(hint -> hint == null ? "test" : hint);
        expect(internalSSOService.isUserManageableFrom("username", "test")).andReturn(true);
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
