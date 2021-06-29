package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.api.BadPublicKeyException;
import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.api.RoleGrantSpecification;
import com.payneteasy.superfly.api.SSOAction;
import com.payneteasy.superfly.api.SSORole;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.crypto.pgp.PGPCrypto;
import com.payneteasy.superfly.lockout.LockoutStrategy;
import com.payneteasy.superfly.model.AuthAction;
import com.payneteasy.superfly.model.AuthRole;
import com.payneteasy.superfly.model.AuthSession;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.UserRegisterRequest;
import com.payneteasy.superfly.model.ui.user.UserForDescription;
import com.payneteasy.superfly.password.ConstantSaltSource;
import com.payneteasy.superfly.password.MessageDigestPasswordEncoder;
import com.payneteasy.superfly.password.NullSaltSource;
import com.payneteasy.superfly.password.PlaintextPasswordEncoder;
import com.payneteasy.superfly.password.SHA256RandomGUIDSaltGenerator;
import com.payneteasy.superfly.policy.password.PasswordSaltPair;
import com.payneteasy.superfly.policy.password.none.DefaultPasswordPolicyValidation;
import com.payneteasy.superfly.register.none.NoneRegisterUserStrategy;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.NotificationService;
import com.payneteasy.superfly.service.SessionService;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.spi.HOTPProvider;
import com.payneteasy.superfly.spisupport.HOTPService;
import org.apache.commons.codec.digest.DigestUtils;
import org.easymock.IAnswer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class InternalSSOServiceImplTest {

    private UserService userService;
    private SessionService sessionService;
    private InternalSSOServiceImpl internalSSOService;
    private HOTPProvider hotpProvider;
    private HOTPService hotpService;

    @Before
    public void setUp() {
        userService = createStrictMock(UserService.class);
        sessionService = createStrictMock(SessionService.class);
        hotpProvider = createMock(HOTPProvider.class);
        hotpService = createMock(HOTPService.class);
        InternalSSOServiceImpl service = new InternalSSOServiceImpl();
        service.setUserService(userService);
        service.setSessionService(sessionService);
        service.setLoggerSink(TrivialProxyFactory.createProxy(LoggerSink.class));
        service.setNotificationService(TrivialProxyFactory.createProxy(NotificationService.class));
        service.setPolicyValidation(new DefaultPasswordPolicyValidation());
        service.setLockoutStrategy(TrivialProxyFactory.createProxy(LockoutStrategy.class));
        service.setRegisterUserStrategy(new NoneRegisterUserStrategy(userService));
        service.setHotpSaltGenerator(new SHA256RandomGUIDSaltGenerator());
        service.setHotpService(hotpService);
        service.setSaltSource(new ConstantSaltSource("abc"));
        internalSSOService = service;
    }

    @Test
    public void testPasswordEncodingWithPlainTextAndNullSalt() {
        internalSSOService.setPasswordEncoder(new PlaintextPasswordEncoder());
        internalSSOService.setSaltSource(new NullSaltSource());
        userService.authenticate(eq("user"), eq("pass"), anyObject(String.class), anyObject(String.class),
                anyObject(String.class));
        expectLastCall().andReturn(null);
        replay(userService);
        internalSSOService.authenticate("user", "pass", null, null, null);
        verify(userService);
    }

    @Test
    public void testPasswordEncodingWithPlainTextAndNonNullSalt() {
        internalSSOService.setPasswordEncoder(new PlaintextPasswordEncoder());
        internalSSOService.setSaltSource(new ConstantSaltSource("salt"));
        userService.authenticate(eq("user"), eq("pass{salt}"), anyObject(String.class), anyObject(String.class),
                anyObject(String.class));
        expectLastCall().andReturn(null);
        replay(userService);
        internalSSOService.authenticate("user", "pass", null, null, null);
        verify(userService);
    }

    @Test
    public void testRegisterUserPasswordEncoding() throws Exception {
        MessageDigestPasswordEncoder encoder = new MessageDigestPasswordEncoder();
        encoder.setAlgorithm("md5");
        internalSSOService.setPasswordEncoder(encoder);
        internalSSOService.setSaltSource(new ConstantSaltSource("e2e4"));
        expect(userService.getUserPasswordHistoryAndCurrentPassword("user")).andReturn(
                Collections.<PasswordSaltPair>emptyList());
        expect(userService.registerUser(anyObject(UserRegisterRequest.class))).andAnswer(new IAnswer<RoutineResult>() {
            public RoutineResult answer() throws Throwable {
                UserRegisterRequest user = (UserRegisterRequest) getCurrentArguments()[0];
                assertEquals(DigestUtils.md5Hex("secret{e2e4}"), user.getPassword());
                assertNotNull(user.getHotpSalt());
                user.setUserid(1L);
                return RoutineResult.okResult();
            }
        });
        expectLastCall();
        replay(userService);
        internalSSOService.registerUser("user", "secret", "email", "subsystem", new RoleGrantSpecification[]{}, "user",
                "user", "question", "answer", null, "test organization", OTPType.NONE);
        verify(userService);
    }

    @Test
    public void testAuthenticateHOTP() {
//        expect(hotpProvider.authenticate(null, "pete", "123456")).andReturn(true);
//        replay(hotpProvider);
//        assertTrue(internalSSOService.authenticateHOTP(null, "pete", "123456"));
//        verify(hotpProvider);
//
//        reset(hotpProvider);
//        expect(hotpProvider.authenticate(null, "pete", "123456")).andReturn(false);
//        replay(hotpProvider);
//        assertFalse(internalSSOService.authenticateHOTP(null, "pete", "123456"));
//        verify(hotpProvider);
    }

    @Test
    public void testRegisterUserWithBadPublicKey() throws Exception {
        internalSSOService.setPasswordEncoder(new PlaintextPasswordEncoder());
        try {
            internalSSOService.registerUser("username", "password", "email.domain.com",
                    "subsystem", new RoleGrantSpecification[]{}, "name", "surname",
                    "secretQuestion", "secretAnswer",
                    "not a key, just junk!", "test organization", OTPType.NONE);
            fail();
        } catch (BadPublicKeyException e) {
            // expected
        }
    }

    @Test
    public void testRegisterUserWithPrefixedAndPostfixedBadPublicKey() throws Exception {
        internalSSOService.setPasswordEncoder(new PlaintextPasswordEncoder());
        internalSSOService.setPublicKeyCrypto(new PGPCrypto());
        try {
            internalSSOService.registerUser("username", "password", "email.domain.com",
                    "subsystem", new RoleGrantSpecification[]{}, "name", "surname",
                    "secretQuestion", "secretAnswer",
                    "-----BEGIN PGP PUBLIC KEY BLOCK-----not a key, just junk!-----END PGP PUBLIC KEY BLOCK-----",
                    "test organization", OTPType.NONE);
            fail();
        } catch (BadPublicKeyException e) {
            // expected
        }
    }

    @Test
    public void testRegisterUserWithNullOrEmptyPublicKey() throws Exception {
        internalSSOService.setPasswordEncoder(new PlaintextPasswordEncoder());
        internalSSOService.setPublicKeyCrypto(new PGPCrypto());

        expect(userService.getUserPasswordHistoryAndCurrentPassword("username")).andReturn(
                Collections.<PasswordSaltPair>emptyList());
        expect(userService.registerUser(anyObject(UserRegisterRequest.class))).andAnswer(new IAnswer<RoutineResult>() {
            public RoutineResult answer() throws Throwable {
                UserRegisterRequest user = (UserRegisterRequest) getCurrentArguments()[0];
                assertEquals(null, user.getPublicKey());
                return RoutineResult.okResult();
            }
        });
        replay(userService);
        internalSSOService.registerUser("username", "password", "email.domain.com",
                "subsystem", new RoleGrantSpecification[]{}, "name", "surname",
                "secretQuestion", "secretAnswer",
                null, "test organization", OTPType.NONE);
        verify(userService);

        reset(userService);

        expect(userService.getUserPasswordHistoryAndCurrentPassword("username")).andReturn(
                Collections.<PasswordSaltPair>emptyList());
        expect(userService.registerUser(anyObject(UserRegisterRequest.class))).andAnswer(new IAnswer<RoutineResult>() {
            public RoutineResult answer() throws Throwable {
                UserRegisterRequest user = (UserRegisterRequest) getCurrentArguments()[0];
                assertEquals("", user.getPublicKey());
                return RoutineResult.okResult();
            }
        });
        replay(userService);
        internalSSOService.registerUser("username", "password", "email.domain.com",
                "subsystem", new RoleGrantSpecification[]{}, "name", "surname",
                "secretQuestion", "secretAnswer",
                "", "test organization", OTPType.NONE);
        verify(userService);
    }

    @Test
    public void testUpdateUserDescriptionWithBadPublicKey() throws Exception {
        UserForDescription user = new UserForDescription();
        user.setUsername("user");
        user.setPublicKey("not a key, just junk!");
        try {
            internalSSOService.updateUserForDescription(user);
            fail();
        } catch (BadPublicKeyException e) {
            // expected
        }
    }

    @Test
    public void testUpdateUserDescriptionWithPrefixedAndPostfixedBadPublicKey() throws Exception {
        internalSSOService.setPublicKeyCrypto(new PGPCrypto());
        UserForDescription user = new UserForDescription();
        user.setUsername("user");
        user.setPublicKey(
                "-----BEGIN PGP PUBLIC KEY BLOCK-----not a key, just junk!-----END PGP PUBLIC KEY BLOCK-----");
        try {
            internalSSOService.updateUserForDescription(user);
            fail();
        } catch (BadPublicKeyException e) {
            // expected
        }
    }

    @Test
    public void testUpdateUserDescriptionWithNullOrEmptyPublicKey() throws Exception {
        UserForDescription user = new UserForDescription();
        user.setUsername("user");
        user.setPublicKey(null);
        internalSSOService.updateUserForDescription(user);

        user.setPublicKey("");
        internalSSOService.updateUserForDescription(user);
    }

    @Test
    public void testExchangeSubsystemTokenSuccess() {
        AuthSession session = new AuthSession("pete", 1L);
        session.setRoles(Collections.singletonList(new AuthRole("test-role")));
        expect(userService.exchangeSubsystemToken("valid-token"))
                .andReturn(session);
        replay(userService);

        SSOUser user = internalSSOService.exchangeSubsystemToken("valid-token");
        assertNotNull(user);
        assertEquals("pete", user.getName());
        assertEquals("1", user.getSessionId());

        verify(userService);
    }

    @Test
    public void testExchangeSubsystemTokenNullResult() {
        expect(userService.exchangeSubsystemToken("valid-token"))
                .andReturn(null);
        replay(userService);

        assertNull(internalSSOService.exchangeSubsystemToken("valid-token"));

        verify(userService);
    }

    @Test
    public void testTouchSessions() {
        sessionService.touchSessions("1,2,3");
        expectLastCall();
        replay(sessionService);
        internalSSOService.touchSessions(Arrays.asList(1L, 2L, 3L));
        verify(sessionService);

        reset(sessionService);
        replay(sessionService);
        internalSSOService.touchSessions(Collections.<Long>emptyList());
        verify(sessionService);

        reset(sessionService);
        replay(sessionService);
        internalSSOService.touchSessions(null);
        verify(sessionService);
    }

    @Test
    public void testCompleteUser() {
        userService.completeUser("username");
        expectLastCall();
        replay(userService);
        internalSSOService.completeUser("username");
        verify(userService);
    }

    @Test
    public void testPseudoAuthenticateSuccess() {
        AuthSession session = new AuthSession("username", 1L);
        AuthRole authRole = new AuthRole("role1");
        AuthAction action1 = new AuthAction();
        action1.setActionName("a1");
        AuthAction action2 = new AuthAction();
        action2.setActionName("a2");
        authRole.setActions(Arrays.asList(action1, action2));
        session.setRoles(Collections.singletonList(authRole));

        expect(userService.pseudoAuthenticate("username", "subsystemIdentifier")).andReturn(session);
        replay(userService);

        SSOUser user = internalSSOService.pseudoAuthenticate("username", "subsystemIdentifier");
        Assert.assertEquals("username", user.getName());
        Assert.assertEquals("1", user.getSessionId());
        Assert.assertEquals(1, user.getActionsMap().size());
        SSORole role = user.getActionsMap().keySet().iterator().next();
        Assert.assertEquals("role1", role.getName());
        SSOAction[] actions = user.getActionsMap().get(role);
        Assert.assertArrayEquals(new SSOAction[]{new SSOAction("a1", false), new SSOAction("a2", false)}, actions);

        verify(userService);
    }

    @Test
    public void testPseudoAuthenticateNoSuchUser() {
        expect(userService.pseudoAuthenticate("username", "subsystemIdentifier")).andReturn(null);
        replay(userService);

        SSOUser user = internalSSOService.pseudoAuthenticate("username", "subsystemIdentifier");
        assertNull(user);

        verify(userService);
    }

    @Test
    public void testChangeUserRole() {
        userService.changeUserRole("username", "ROLE_TO", "test");
        expectLastCall().andReturn(new RoutineResult("OK", null));
        replay(userService);

        internalSSOService.changeUserRole("username", "ROLE_TO", "test");

        verify(userService);
    }

    @Test
    public void testFailedChangeUserRole() {
        userService.changeUserRole("username", "ROLE_TO", "test");
        expectLastCall().andReturn(new RoutineResult("Failed", "Error message"));
        replay(userService);

        try {
            internalSSOService.changeUserRole("username", "ROLE_TO", "test");
            fail("An exception should have been thrown");
        } catch (IllegalStateException e) {
            assertEquals("Error message", e.getMessage());
        }

        verify(userService);
    }

}
