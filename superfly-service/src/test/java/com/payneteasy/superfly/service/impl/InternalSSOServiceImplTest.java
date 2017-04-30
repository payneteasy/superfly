package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.api.*;
import com.payneteasy.superfly.crypto.pgp.PGPCrypto;
import com.payneteasy.superfly.dao.SessionDao;
import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.lockout.LockoutStrategy;
import com.payneteasy.superfly.model.*;
import com.payneteasy.superfly.model.UserRegisterRequest;
import com.payneteasy.superfly.model.ui.user.UserForDescription;
import com.payneteasy.superfly.password.*;
import com.payneteasy.superfly.policy.password.PasswordSaltPair;
import com.payneteasy.superfly.policy.password.none.DefaultPasswordPolicyValidation;
import com.payneteasy.superfly.register.none.NoneRegisterUserStrategy;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.NotificationService;
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

    private UserDao userDao;
    private SessionDao sessionDao;
    private InternalSSOServiceImpl internalSSOService;
    private HOTPProvider hotpProvider;
    private HOTPService hotpService;

    @Before
    public void setUp() {
        userDao = createStrictMock(UserDao.class);
        sessionDao = createStrictMock(SessionDao.class);
        hotpProvider = createMock(HOTPProvider.class);
        hotpService = createMock(HOTPService.class);
        InternalSSOServiceImpl service = new InternalSSOServiceImpl();
        service.setUserDao(userDao);
        service.setSessionDao(sessionDao);
        service.setLoggerSink(TrivialProxyFactory.createProxy(LoggerSink.class));
        service.setNotificationService(TrivialProxyFactory.createProxy(NotificationService.class));
        service.setHotpProvider(hotpProvider);
        service.setPolicyValidation(new DefaultPasswordPolicyValidation());
        service.setLockoutStrategy(TrivialProxyFactory.createProxy(LockoutStrategy.class));
        service.setRegisterUserStrategy(new NoneRegisterUserStrategy(userDao));
        service.setHotpSaltGenerator(new SHA256RandomGUIDSaltGenerator());
        service.setHotpService(hotpService);
        service.setSaltSource(new ConstantSaltSource("abc"));
        internalSSOService = service;
    }

    @Test
    public void testPasswordEncodingWithPlainTextAndNullSalt() {
        internalSSOService.setPasswordEncoder(new PlaintextPasswordEncoder());
        internalSSOService.setSaltSource(new NullSaltSource());
        userDao.authenticate(eq("user"), eq("pass"), anyObject(String.class), anyObject(String.class),
                anyObject(String.class));
        expectLastCall().andReturn(null);
        replay(userDao);
        internalSSOService.authenticate("user", "pass", null, null, null);
        verify(userDao);
    }

    @Test
    public void testPasswordEncodingWithPlainTextAndNonNullSalt() {
        internalSSOService.setPasswordEncoder(new PlaintextPasswordEncoder());
        internalSSOService.setSaltSource(new ConstantSaltSource("salt"));
        userDao.authenticate(eq("user"), eq("pass{salt}"), anyObject(String.class), anyObject(String.class),
                anyObject(String.class));
        expectLastCall().andReturn(null);
        replay(userDao);
        internalSSOService.authenticate("user", "pass", null, null, null);
        verify(userDao);
    }

    @Test
    public void testRegisterUserPasswordEncoding() throws Exception {
        MessageDigestPasswordEncoder encoder = new MessageDigestPasswordEncoder();
        encoder.setAlgorithm("md5");
        internalSSOService.setPasswordEncoder(encoder);
        internalSSOService.setSaltSource(new ConstantSaltSource("e2e4"));
        expect(userDao.getUserPasswordHistoryAndCurrentPassword("user")).andReturn(
                Collections.<PasswordSaltPair>emptyList());
        expect(userDao.registerUser(anyObject(UserRegisterRequest.class))).andAnswer(new IAnswer<RoutineResult>() {
            public RoutineResult answer() throws Throwable {
                UserRegisterRequest user = (UserRegisterRequest) getCurrentArguments()[0];
                assertEquals(DigestUtils.md5Hex("secret{e2e4}"), user.getPassword());
                assertNotNull(user.getHotpSalt());
                user.setUserid(1L);
                return RoutineResult.okResult();
            }
        });
        hotpService.sendTableIfSupported("subsystem", 1L);
        expectLastCall();
        replay(userDao, hotpService);
        internalSSOService.registerUser("user", "secret", "email", "subsystem", new RoleGrantSpecification[]{}, "user",
                "user", "question", "answer", null, "test organization");
        verify(userDao, hotpService);
    }

    @Test
    public void testAuthenticateHOTP() {
        expect(hotpProvider.authenticate(null, "pete", "123456")).andReturn(true);
        replay(hotpProvider);
        assertTrue(internalSSOService.authenticateHOTP(null, "pete", "123456"));
        verify(hotpProvider);

        reset(hotpProvider);
        expect(hotpProvider.authenticate(null, "pete", "123456")).andReturn(false);
        replay(hotpProvider);
        assertFalse(internalSSOService.authenticateHOTP(null, "pete", "123456"));
        verify(hotpProvider);
    }

    @Test
    public void testRegisterUserWithBadPublicKey() throws Exception {
        internalSSOService.setPasswordEncoder(new PlaintextPasswordEncoder());
        try {
            internalSSOService.registerUser("username", "password", "email.domain.com",
                    "subsystem", new RoleGrantSpecification[]{}, "name", "surname",
                    "secretQuestion", "secretAnswer",
                    "not a key, just junk!", "test organization");
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
                    "test organization");
            fail();
        } catch (BadPublicKeyException e) {
            // expected
        }
    }

    @Test
    public void testRegisterUserWithNullOrEmptyPublicKey() throws Exception {
        internalSSOService.setPasswordEncoder(new PlaintextPasswordEncoder());
        internalSSOService.setPublicKeyCrypto(new PGPCrypto());

        expect(userDao.getUserPasswordHistoryAndCurrentPassword("username")).andReturn(
                Collections.<PasswordSaltPair>emptyList());
        expect(userDao.registerUser(anyObject(UserRegisterRequest.class))).andAnswer(new IAnswer<RoutineResult>() {
            public RoutineResult answer() throws Throwable {
                UserRegisterRequest user = (UserRegisterRequest) getCurrentArguments()[0];
                assertEquals(null, user.getPublicKey());
                return RoutineResult.okResult();
            }
        });
        replay(userDao);
        internalSSOService.registerUser("username", "password", "email.domain.com",
                "subsystem", new RoleGrantSpecification[]{}, "name", "surname",
                "secretQuestion", "secretAnswer",
                null, "test organization");
        verify(userDao);

        reset(userDao);

        expect(userDao.getUserPasswordHistoryAndCurrentPassword("username")).andReturn(
                Collections.<PasswordSaltPair>emptyList());
        expect(userDao.registerUser(anyObject(UserRegisterRequest.class))).andAnswer(new IAnswer<RoutineResult>() {
            public RoutineResult answer() throws Throwable {
                UserRegisterRequest user = (UserRegisterRequest) getCurrentArguments()[0];
                assertEquals("", user.getPublicKey());
                return RoutineResult.okResult();
            }
        });
        replay(userDao);
        internalSSOService.registerUser("username", "password", "email.domain.com",
                "subsystem", new RoleGrantSpecification[]{}, "name", "surname",
                "secretQuestion", "secretAnswer",
                "", "test organization");
        verify(userDao);
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
        expect(userDao.exchangeSubsystemToken("valid-token"))
                .andReturn(session);
        replay(userDao);

        SSOUser user = internalSSOService.exchangeSubsystemToken("valid-token");
        assertNotNull(user);
        assertEquals("pete", user.getName());
        assertEquals("1", user.getSessionId());

        verify(userDao);
    }

    @Test
    public void testExchangeSubsystemTokenNullResult() {
        expect(userDao.exchangeSubsystemToken("valid-token"))
                .andReturn(null);
        replay(userDao);

        assertNull(internalSSOService.exchangeSubsystemToken("valid-token"));

        verify(userDao);
    }

    @Test
    public void testTouchSessions() {
        sessionDao.touchSessions("1,2,3");
        expectLastCall();
        replay(sessionDao);
        internalSSOService.touchSessions(Arrays.asList(1L, 2L, 3L));
        verify(sessionDao);

        reset(sessionDao);
        replay(sessionDao);
        internalSSOService.touchSessions(Collections.<Long>emptyList());
        verify(sessionDao);

        reset(sessionDao);
        replay(sessionDao);
        internalSSOService.touchSessions(null);
        verify(sessionDao);
    }

    @Test
    public void testCompleteUser() {
        userDao.completeUser("username");
        expectLastCall();
        replay(userDao);
        internalSSOService.completeUser("username");
        verify(userDao);
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

        expect(userDao.pseudoAuthenticate("username", "subsystemIdentifier")).andReturn(session);
        replay(userDao);

        SSOUser user = internalSSOService.pseudoAuthenticate("username", "subsystemIdentifier");
        Assert.assertEquals("username", user.getName());
        Assert.assertEquals("1", user.getSessionId());
        Assert.assertEquals(1, user.getActionsMap().size());
        SSORole role = user.getActionsMap().keySet().iterator().next();
        Assert.assertEquals("role1", role.getName());
        SSOAction[] actions = user.getActionsMap().get(role);
        Assert.assertArrayEquals(new SSOAction[]{new SSOAction("a1", false), new SSOAction("a2", false)}, actions);

        verify(userDao);
    }

    @Test
    public void testPseudoAuthenticateNoSuchUser() {
        expect(userDao.pseudoAuthenticate("username", "subsystemIdentifier")).andReturn(null);
        replay(userDao);

        SSOUser user = internalSSOService.pseudoAuthenticate("username", "subsystemIdentifier");
        assertNull(user);

        verify(userDao);
    }

    @Test
    public void testChangeUserRole() {
        userDao.changeUserRole("username", "ROLE_TO", "test");
        expectLastCall().andReturn(new RoutineResult("OK", null));
        replay(userDao);

        internalSSOService.changeUserRole("username", "ROLE_TO", "test");

        verify(userDao);
    }

    @Test
    public void testFailedChangeUserRole() {
        userDao.changeUserRole("username", "ROLE_TO", "test");
        expectLastCall().andReturn(new RoutineResult("Failed", "Error message"));
        replay(userDao);

        try {
            internalSSOService.changeUserRole("username", "ROLE_TO", "test");
            fail("An exception should have been thrown");
        } catch (IllegalStateException e) {
            assertEquals("Error message", e.getMessage());
        }

        verify(userDao);
    }

}
