package com.payneteasy.superfly.service.impl;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.payneteasy.superfly.dao.SmtpServerDao;
import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.lockout.LockoutStrategy;
import com.payneteasy.superfly.lockout.none.NoneLockoutStrategy;
import com.payneteasy.superfly.model.AuthSession;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.UserLoginStatus;
import com.payneteasy.superfly.model.ui.user.UserForDescription;
import com.payneteasy.superfly.password.ConstantSaltSource;
import com.payneteasy.superfly.password.MessageDigestPasswordEncoder;
import com.payneteasy.superfly.password.PlaintextPasswordEncoder;
import com.payneteasy.superfly.service.UserInfoService;
import com.payneteasy.superfly.service.UserService;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Each audited operation must leave one structured record with actor, target, result and client IP,
 * and never a secret.
 */
public class AuditEventsTest {

    private static final String IP = "10.1.2.3";

    private UserDao userDao;
    private UserServiceImpl userService;
    private ListAppender<ILoggingEvent> appender;
    private final Logger userServiceLogger = (Logger) LoggerFactory.getLogger(UserServiceImpl.class);
    private final Logger smtpLogger = (Logger) LoggerFactory.getLogger(SmtpServerServiceImpl.class);
    private LoggerSinkImpl loggerSink;

    @Before
    public void setUp() {
        UserInfoService userInfoService = new UserInfoService() {
            public String getUsername() {
                return "admin";
            }

            @Override
            public String getRemoteAddress() {
                return IP;
            }
        };
        loggerSink = new LoggerSinkImpl();
        loggerSink.setUserInfoService(userInfoService);

        userDao = EasyMock.createStrictMock(UserDao.class);
        userService = new UserServiceImpl();
        userService.setUserDao(userDao);
        userService.setLoggerSink(loggerSink);
        userService.setUserInfoService(userInfoService);
        userService.setPasswordEncoder(new PlaintextPasswordEncoder());
        userService.setLegacyPasswordEncoder(new MessageDigestPasswordEncoder());
        userService.setSaltSource(new ConstantSaltSource("salt"));
        userService.setLockoutStrategy(new NoneLockoutStrategy());

        appender = new ListAppender<>();
        appender.start();
        userServiceLogger.addAppender(appender);
        smtpLogger.addAppender(appender);
    }

    @After
    public void tearDown() {
        userServiceLogger.detachAppender(appender);
        smtpLogger.detachAppender(appender);
    }

    private List<String> messages() {
        return appender.list.stream().map(ILoggingEvent::getFormattedMessage).collect(Collectors.toList());
    }

    private String onlyMessage() {
        assertEquals(messages().toString(), 1, appender.list.size());
        return messages().get(0);
    }

    @Test
    public void ipv6AddressReachesDaoUntruncated() {
        String ipv6 = "2001:db8:85a3::8a2e:370:7334";
        EasyMock.expect(userDao.authenticate("bob", "p", "l", "billing", ipv6, "info")).andReturn(new AuthSession("bob"));
        EasyMock.replay(userDao);

        userService.authenticate("bob", "p", "l", "billing", ipv6, "info");

        EasyMock.verify(userDao);
    }

    @Test
    public void userRolesChangeListsRoleIds() {
        userService.setNotificationService(TrivialProxyFactory.createProxy(com.payneteasy.superfly.service.NotificationService.class));
        EasyMock.expect(userDao.changeUserRoles(5L, "1,2", "3", "1")).andReturn(RoutineResult.okResult());
        EasyMock.replay(userDao);

        userService.changeUserRoles(5L, java.util.Arrays.asList(1L, 2L), java.util.Arrays.asList(3L),
                java.util.Arrays.asList(1L));

        assertEquals("user:admin, event:CHANGE_USER_ROLES, resource:5, result:success, "
                + "details:added=1,2, removed=3, grantActions=1, ip:" + IP, onlyMessage());
    }

    @Test
    public void remoteChangeUserRole() {
        EasyMock.expect(userDao.changeUserRole("bob", "ROLE_B", "billing")).andReturn(RoutineResult.okResult());
        EasyMock.replay(userDao);

        userService.changeUserRole("bob", "ROLE_B", "billing");

        assertEquals("user:admin, event:REMOTE_CHANGE_USER_ROLE, resource:bob, result:success, "
                + "details:role=ROLE_B, subsystem=billing, ip:" + IP, onlyMessage());
    }

    @Test
    public void remoteChangeUserRoleFailure() {
        EasyMock.expect(userDao.changeUserRole("bob", "ROLE_B", "billing")).andReturn(RoutineResult.failureResult());
        EasyMock.replay(userDao);

        userService.changeUserRole("bob", "ROLE_B", "billing");

        assertTrue(onlyMessage().contains("event:REMOTE_CHANGE_USER_ROLE, resource:bob, result:failure"));
    }

    @Test
    public void disablingOtpIsAudited() {
        userDao.updateUserIsOtpOptionalValue("bob", true);
        EasyMock.replay(userDao);

        userService.updateUserIsOtpOptionalValue("bob", true);

        assertEquals("user:admin, event:CHANGE_USER_OTP_OPTIONAL, resource:bob, result:success, "
                + "details:otpOptional=true, ip:" + IP, onlyMessage());
    }

    @Test
    public void otpTypeChangeIsAudited() {
        userDao.updateUserOtpType("bob", "NONE");
        EasyMock.replay(userDao);

        userService.updateUserOtpType("bob", "NONE");

        assertEquals("user:admin, event:CHANGE_USER_OTP_TYPE, resource:bob, result:success, "
                + "details:otpType=NONE, ip:" + IP, onlyMessage());
    }

    @Test
    public void otpMasterKeyResetIsAuditedWithoutTheKey() {
        userDao.persistGoogleAuthMasterKeyForUsername("bob", "SECRET-KEY");
        EasyMock.replay(userDao);

        userService.persistOtpMasterKeyForUsername("bob", "SECRET-KEY");

        String message = onlyMessage();
        assertEquals("user:admin, event:PERSIST_OTP_MASTER_KEY, resource:bob, result:success, ip:" + IP, message);
        assertFalse(message.contains("SECRET-KEY"));
    }

    @Test
    public void userDescriptionUpdateIsAudited() {
        UserForDescription user = new UserForDescription();
        user.setUsername("bob");
        user.setPublicKey("-----BEGIN PGP PUBLIC KEY BLOCK-----");
        userDao.updateUserForDescription(user);
        EasyMock.replay(userDao);

        userService.updateUserForDescription(user);

        assertEquals("user:admin, event:UPDATE_USER_DESCRIPTION, resource:bob, result:success, ip:" + IP,
                onlyMessage());
    }

    @Test
    public void unlockSuspendedUserIsAuditedWithoutThePassword() {
        EasyMock.expect(userDao.unlockSuspendedUser(7L, "NEW-PASSWORD-HASH")).andReturn(RoutineResult.okResult());
        EasyMock.replay(userDao);

        userService.unlockSuspendedUser(7L, "NEW-PASSWORD-HASH");

        String message = onlyMessage();
        assertEquals("user:admin, event:UNLOCK_SUSPENDED_USER, resource:7, result:success, ip:" + IP, message);
        assertFalse(message.contains("NEW-PASSWORD-HASH"));
    }

    @Test
    public void autoLockIsAudited() {
        EasyMock.expect(userDao.lockoutConditionnally("bob", 5L, "PASSWORD"))
                .andReturn(new RoutineResult("OK", "ACCOUNT_LOCKED"));
        EasyMock.replay(userDao);

        userService.lockoutConditionnally("bob", 5L, "PASSWORD");

        assertEquals("user:admin, event:AUTO_LOCK_USER, resource:bob, result:success, "
                + "details:reason=PASSWORD, maxLoginsFailed=5, ip:" + IP, onlyMessage());
    }

    @Test
    public void lockoutCheckWithoutLockIsNotAudited() {
        EasyMock.expect(userDao.lockoutConditionnally("bob", 5L, "PASSWORD")).andReturn(RoutineResult.okResult());
        EasyMock.replay(userDao);

        userService.lockoutConditionnally("bob", 5L, "PASSWORD");

        assertTrue(messages().isEmpty());
    }

    @Test
    public void ssoPasswordStepSuccessPassesIpAndIsAudited() {
        EasyMock.expect(userDao.userHasRolesInSubsystem("bob", "superfly")).andReturn("N");
        EasyMock.expect(userDao.userHasRolesInSubsystem("bob", "billing")).andReturn("Y");
        EasyMock.expect(userDao.getUserLoginStatus(eq("bob"), eq("pw{salt}"), anyObject(String.class),
                eq("billing"), eq(IP))).andReturn("Y");
        EasyMock.replay(userDao);

        assertEquals(UserLoginStatus.SUCCESS, userService.checkUserCanLoginWithThisPassword("bob", "pw", "billing"));

        EasyMock.verify(userDao);
        String message = onlyMessage();
        assertEquals("user:admin, event:SSO_PASSWORD_LOGIN, resource:bob, result:success, "
                + "details:subsystem=billing, ip:" + IP, message);
        assertFalse(message.contains("pw{salt}"));
    }

    @Test
    public void ssoPasswordStepFailureIsAudited() {
        EasyMock.expect(userDao.userHasRolesInSubsystem("bob", "superfly")).andReturn("N");
        EasyMock.expect(userDao.userHasRolesInSubsystem("bob", "billing")).andReturn("Y");
        EasyMock.expect(userDao.getUserLoginStatus(eq("bob"), anyObject(String.class), anyObject(String.class),
                eq("billing"), eq(IP))).andReturn("N");
        EasyMock.replay(userDao);

        assertEquals(UserLoginStatus.FAILED, userService.checkUserCanLoginWithThisPassword("bob", "bad", "billing"));

        assertTrue(onlyMessage().contains("event:SSO_PASSWORD_LOGIN, resource:bob, result:failure"));
    }

    @Test
    public void ssoPasswordStepFailureStillCountsTowardsLockout() {
        LockoutStrategy lockoutStrategy = EasyMock.createStrictMock(LockoutStrategy.class);
        lockoutStrategy.checkLoginsFailed("bob", com.payneteasy.superfly.model.LockoutType.PASSWORD);
        EasyMock.replay(lockoutStrategy);
        userService.setLockoutStrategy(lockoutStrategy);
        EasyMock.expect(userDao.userHasRolesInSubsystem("bob", "superfly")).andReturn("N");
        EasyMock.expect(userDao.userHasRolesInSubsystem("bob", "billing")).andReturn("Y");
        EasyMock.expect(userDao.getUserLoginStatus(eq("bob"), anyObject(String.class), anyObject(String.class),
                eq("billing"), eq(IP))).andReturn("N");
        EasyMock.replay(userDao);

        userService.checkUserCanLoginWithThisPassword("bob", "bad", "billing");

        EasyMock.verify(lockoutStrategy);
    }

    @Test
    public void localLoginPassesClientIpToDao() {
        UserService mockUserService = EasyMock.createStrictMock(UserService.class);
        LocalSecurityServiceImpl service = new LocalSecurityServiceImpl();
        service.setUserService(mockUserService);
        service.setLoggerSink(loggerSink);
        service.setLockoutStrategy(new NoneLockoutStrategy());
        service.setUserInfoService(userInfoWithIp());
        com.payneteasy.superfly.password.UserPasswordEncoderImpl encoder =
                new com.payneteasy.superfly.password.UserPasswordEncoderImpl();
        encoder.setPasswordEncoder(new PlaintextPasswordEncoder());
        encoder.setLegacyPasswordEncoder(new PlaintextPasswordEncoder());
        encoder.setSaltSource(new com.payneteasy.superfly.password.NullSaltSource());
        service.setUserPasswordEncoder(encoder);
        EasyMock.expect(mockUserService.authenticate(eq("admin"), anyObject(String.class), anyObject(String.class),
                eq("superfly"), eq(IP), EasyMock.isNull(String.class))).andReturn(new AuthSession("admin"));
        EasyMock.replay(mockUserService);

        service.authenticate("admin", "pw");

        EasyMock.verify(mockUserService);
    }

    @Test
    public void smtpPasswordViewIsAudited() {
        SmtpServerServiceImpl service = new SmtpServerServiceImpl();
        service.setSmtpServerDao(EasyMock.createNiceMock(SmtpServerDao.class));
        service.setLoggerSink(loggerSink);

        service.logPasswordViewed("main-smtp");

        assertEquals("user:admin, event:VIEW_SMTP_PASSWORD, resource:main-smtp, result:success, ip:" + IP,
                onlyMessage());
    }

    private static UserInfoService userInfoWithIp() {
        return new UserInfoService() {
            public String getUsername() {
                return "admin";
            }

            @Override
            public String getRemoteAddress() {
                return IP;
            }
        };
    }
}
