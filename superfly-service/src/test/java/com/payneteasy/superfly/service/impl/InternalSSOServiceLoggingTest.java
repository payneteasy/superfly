package com.payneteasy.superfly.service.impl;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;

import java.util.Collections;

import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.model.AuthSession;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import com.payneteasy.superfly.api.RoleGrantSpecification;
import com.payneteasy.superfly.api.UserExistsException;
import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.lockout.LockoutStrategy;
import com.payneteasy.superfly.model.AuthRole;
import com.payneteasy.superfly.model.UserRegisterRequest;
import com.payneteasy.superfly.password.NullSaltSource;
import com.payneteasy.superfly.password.PlaintextPasswordEncoder;
import com.payneteasy.superfly.password.SHA256RandomGUIDSaltGenerator;
import com.payneteasy.superfly.policy.password.PasswordSaltPair;
import com.payneteasy.superfly.policy.password.none.DefaultPasswordPolicyValidation;
import com.payneteasy.superfly.register.none.NoneRegisterUserStrategy;
import com.payneteasy.superfly.service.InternalSSOService;
import com.payneteasy.superfly.service.NotificationService;
import com.payneteasy.superfly.spisupport.HOTPService;

public class InternalSSOServiceLoggingTest extends AbstractServiceLoggingTest {

    private InternalSSOService internalSSOService;
    private UserDao userDao;

    @Before
    public void setUp() {
        InternalSSOServiceImpl service = new InternalSSOServiceImpl();
        userDao = EasyMock.createStrictMock(UserDao.class);
        service.setUserDao(userDao);
        service.setNotificationService(TrivialProxyFactory.createProxy(NotificationService.class));
        service.setLoggerSink(loggerSink);
        service.setPasswordEncoder(new PlaintextPasswordEncoder());
        service.setSaltSource(new NullSaltSource());
        service.setHotpSaltGenerator(new SHA256RandomGUIDSaltGenerator());
        service.setPolicyValidation(new DefaultPasswordPolicyValidation());
        service.setLockoutStrategy(TrivialProxyFactory.createProxy(LockoutStrategy.class));
        service.setRegisterUserStrategy(new NoneRegisterUserStrategy(userDao));
        service.setHotpService(TrivialProxyFactory.createProxy(HOTPService.class));
        internalSSOService = service;
    }

    @Test
    public void testRegisterUser() throws Exception {
        EasyMock.expect(userDao.getUserPasswordHistoryAndCurrentPassword("new-user")).andReturn(Collections.<PasswordSaltPair>emptyList());
        userDao.registerUser(anyObject(UserRegisterRequest.class));
        EasyMock.expectLastCall().andReturn(okResult());
        loggerSink.info(anyObject(Logger.class), eq("REGISTER_USER"), eq(true), eq("new-user"));
        EasyMock.replay(loggerSink, userDao);

        internalSSOService.registerUser("new-user", "new-password", "new-email", null, new RoleGrantSpecification[] {}, "user", "user", "question", "answer", null,"test organization", OTPType.NONE);

        EasyMock.verify(loggerSink);
    }

    @Test
    public void testRegisterUserDuplicate() throws Exception {
        EasyMock.expect(userDao.getUserPasswordHistoryAndCurrentPassword("new-user")).andReturn(Collections.<PasswordSaltPair>emptyList());
        userDao.registerUser(anyObject(UserRegisterRequest.class));
        EasyMock.expectLastCall().andReturn(duplicateResult());
        loggerSink.info(anyObject(Logger.class), eq("REGISTER_USER"), eq(false), eq("new-user"));
        EasyMock.replay(loggerSink, userDao);

        try {
            internalSSOService.registerUser("new-user", "new-password", "new-email", null,
                    new RoleGrantSpecification[] {}, "user", "user", "question", "answer", null,"test organization", OTPType.NONE);
        } catch (UserExistsException e) {
            // expected
        }

        EasyMock.verify(loggerSink);
    }

    @Test
    public void testRegisterUserFail() throws Exception {
        EasyMock.expect(userDao.getUserPasswordHistoryAndCurrentPassword("new-user")).andReturn(Collections.<PasswordSaltPair>emptyList());
        userDao.registerUser(anyObject(UserRegisterRequest.class));
        EasyMock.expectLastCall().andReturn(failureResult());
        loggerSink.info(anyObject(Logger.class), eq("REGISTER_USER"), eq(false), eq("new-user"));
        EasyMock.replay(loggerSink, userDao);

        try {
            internalSSOService.registerUser("new-user", "new-password", "new-email", null,
                                            new RoleGrantSpecification[] {}, "user", "user", "question", "answer", null, "test organization", OTPType.NONE);
        } catch (IllegalStateException e) {
            // expected
        }

        EasyMock.verify(loggerSink);
    }

    @Test
    public void testAuthenticate() throws Exception {
        AuthSession session = new AuthSession("username", 1L);
        session.setRoles(Collections.singletonList(new AuthRole()));

        EasyMock.expect(
                userDao.authenticate(eq("username"), eq("password"), anyObject(String.class), anyObject(String.class),
                        anyObject(String.class))).andReturn(session);
        loggerSink.info(anyObject(Logger.class), eq("REMOTE_LOGIN"), eq(true), eq("username"));
        EasyMock.replay(loggerSink, userDao);

        internalSSOService.authenticate("username", "password", null, null, null);

        EasyMock.verify(loggerSink, userDao);
    }

    @Test
    public void testAuthenticateFail() throws Exception {
        EasyMock.expect(
                userDao.authenticate(eq("username"), eq("password"), anyObject(String.class), anyObject(String.class),
                        anyObject(String.class))).andReturn(null);
        loggerSink.info(anyObject(Logger.class), eq("REMOTE_LOGIN"), eq(false), eq("username"));
        EasyMock.replay(loggerSink, userDao);

        internalSSOService.authenticate("username", "password", null, null, null);

        EasyMock.verify(loggerSink);
    }

    @Test
    public void testAuthenticateNoRoles() throws Exception {
        AuthSession session = new AuthSession("username");
        session.setRoles(Collections.<AuthRole> emptyList());

        EasyMock.expect(
                userDao.authenticate(eq("username"), eq("password"), anyObject(String.class), anyObject(String.class),
                        anyObject(String.class))).andReturn(session);
        loggerSink.info(anyObject(Logger.class), eq("REMOTE_LOGIN"), eq(false), eq("username"));
        EasyMock.replay(loggerSink, userDao);

        internalSSOService.authenticate("username", "password", null, null, null);

        EasyMock.verify(loggerSink);
    }

}
