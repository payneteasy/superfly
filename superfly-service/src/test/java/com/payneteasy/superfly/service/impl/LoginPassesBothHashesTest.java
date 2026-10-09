package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.lockout.none.NoneLockoutStrategy;
import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.model.AuthSession;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.user.UIUser;
import com.payneteasy.superfly.password.ConstantSaltSource;
import com.payneteasy.superfly.password.MessageDigestPasswordEncoder;
import com.payneteasy.superfly.password.Pbkdf2PasswordEncoder;
import com.payneteasy.superfly.password.PlaintextPasswordEncoder;
import com.payneteasy.superfly.password.UserPasswordEncoderImpl;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.easymock.EasyMock;
import org.junit.Test;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;

/**
 * Login paths must send both the new and the legacy hash so that SQL can rehash on success.
 */
public class LoginPassesBothHashesTest {
    private static final String LEGACY = DigestUtils.sha256Hex("pass{salt}");

    @Test
    public void internalSsoAuthenticate() {
        UserService userService = EasyMock.createStrictMock(UserService.class);
        InternalSSOServiceImpl service = new InternalSSOServiceImpl();
        service.setUserService(userService);
        service.setLoggerSink(TrivialProxyFactory.createProxy(LoggerSink.class));
        service.setLockoutStrategy(TrivialProxyFactory.createProxy(com.payneteasy.superfly.lockout.LockoutStrategy.class));
        service.setPasswordEncoder(new PlaintextPasswordEncoder());
        service.setLegacyPasswordEncoder(new MessageDigestPasswordEncoder());
        service.setSaltSource(new ConstantSaltSource("salt"));
        EasyMock.expect(userService.userHasRolesInSubsystem("user", "superfly")).andReturn(false);
        EasyMock.expect(userService.userHasRolesInSubsystem("user", "subsystem")).andReturn(true);
        EasyMock.expect(userService.authenticate(eq("user"), eq("pass{salt}"), eq(LEGACY),
                anyObject(String.class), anyObject(String.class), anyObject(String.class))).andReturn(null);
        EasyMock.replay(userService);

        service.authenticate("user", "pass", "subsystem", null, null);

        EasyMock.verify(userService);
    }

    @Test
    public void localSecurityAuthenticate() {
        UserService userService = EasyMock.createStrictMock(UserService.class);
        LocalSecurityServiceImpl service = new LocalSecurityServiceImpl();
        service.setUserService(userService);
        service.setLoggerSink(TrivialProxyFactory.createProxy(LoggerSink.class));
        service.setLockoutStrategy(new NoneLockoutStrategy());
        UserPasswordEncoderImpl encoder = new UserPasswordEncoderImpl();
        encoder.setPasswordEncoder(new PlaintextPasswordEncoder());
        encoder.setLegacyPasswordEncoder(new MessageDigestPasswordEncoder());
        encoder.setSaltSource(new ConstantSaltSource("salt"));
        service.setUserPasswordEncoder(encoder);
        EasyMock.expect(userService.authenticate(eq("user"), eq("pass{salt}"), eq(LEGACY),
                anyObject(String.class), anyObject(String.class), anyObject(String.class)))
                .andReturn(new AuthSession("user"));
        EasyMock.replay(userService);

        service.authenticate("user", "pass");

        EasyMock.verify(userService);
    }

    @Test
    public void nullPasswordIsOrdinaryFailedAttemptInternalSso() {
        UserService userService = EasyMock.createStrictMock(UserService.class);
        InternalSSOServiceImpl service = new InternalSSOServiceImpl();
        service.setUserService(userService);
        service.setLoggerSink(TrivialProxyFactory.createProxy(LoggerSink.class));
        service.setLockoutStrategy(TrivialProxyFactory.createProxy(com.payneteasy.superfly.lockout.LockoutStrategy.class));
        service.setPasswordEncoder(new Pbkdf2PasswordEncoder());
        service.setLegacyPasswordEncoder(new MessageDigestPasswordEncoder());
        service.setSaltSource(new ConstantSaltSource("salt"));
        EasyMock.expect(userService.userHasRolesInSubsystem("user", "superfly")).andReturn(false);
        EasyMock.expect(userService.userHasRolesInSubsystem("user", "subsystem")).andReturn(true);
        EasyMock.expect(userService.authenticate(eq("user"), eq(Pbkdf2PasswordEncoder.NEVER_MATCHING_HASH), EasyMock.isNull(String.class),
                anyObject(String.class), anyObject(String.class), anyObject(String.class))).andReturn(null);
        EasyMock.replay(userService);

        service.authenticate("user", null, "subsystem", null, null);

        EasyMock.verify(userService);
    }

    @Test
    public void nullPasswordIsOrdinaryFailedAttemptLocal() {
        UserService userService = EasyMock.createStrictMock(UserService.class);
        LocalSecurityServiceImpl service = new LocalSecurityServiceImpl();
        service.setUserService(userService);
        service.setLoggerSink(TrivialProxyFactory.createProxy(LoggerSink.class));
        service.setLockoutStrategy(new NoneLockoutStrategy());
        UserPasswordEncoderImpl encoder = new UserPasswordEncoderImpl();
        encoder.setPasswordEncoder(new Pbkdf2PasswordEncoder());
        encoder.setLegacyPasswordEncoder(new MessageDigestPasswordEncoder());
        encoder.setSaltSource(new ConstantSaltSource("salt"));
        service.setUserPasswordEncoder(encoder);
        EasyMock.expect(userService.authenticate(eq("user"), eq(Pbkdf2PasswordEncoder.NEVER_MATCHING_HASH), EasyMock.isNull(String.class),
                anyObject(String.class), anyObject(String.class), anyObject(String.class)))
                .andReturn(null);
        EasyMock.replay(userService);

        service.authenticate("user", null);

        EasyMock.verify(userService);
    }

    @Test
    public void nullPasswordIsOrdinaryFailedAttemptLoginStatus() {
        UserDao userDao = EasyMock.createStrictMock(UserDao.class);
        UserServiceImpl service = new UserServiceImpl();
        service.setUserDao(userDao);
        service.setPasswordEncoder(new Pbkdf2PasswordEncoder());
        service.setLegacyPasswordEncoder(new MessageDigestPasswordEncoder());
        service.setSaltSource(new ConstantSaltSource("salt"));
        service.setLockoutStrategy(TrivialProxyFactory.createProxy(com.payneteasy.superfly.lockout.LockoutStrategy.class));
        service.setLoggerSink(TrivialProxyFactory.createProxy(LoggerSink.class));
        EasyMock.expect(userDao.getUserLoginStatus(eq("user"), eq(Pbkdf2PasswordEncoder.NEVER_MATCHING_HASH),
                EasyMock.isNull(String.class), eq("subsystem"), EasyMock.isNull(String.class))).andReturn("N");
        EasyMock.replay(userDao);

        service.checkUserCanLoginWithThisPassword("user", null, "subsystem");

        EasyMock.verify(userDao);
    }

    @Test
    public void updateUserWithNullPasswordDoesNotEncode() {
        UserDao userDao = EasyMock.createStrictMock(UserDao.class);
        UserServiceImpl service = new UserServiceImpl();
        service.setUserDao(userDao);
        service.setPasswordEncoder(new Pbkdf2PasswordEncoder());
        service.setSaltSource(new ConstantSaltSource("salt"));
        service.setLoggerSink(TrivialProxyFactory.createProxy(LoggerSink.class));
        EasyMock.expect(userDao.updateUser(anyObject(UIUser.class))).andReturn(RoutineResult.okResult());
        EasyMock.replay(userDao);

        UIUser user = new UIUser();
        user.setUsername("pete");
        service.updateUser(user);

        EasyMock.verify(userDao);
    }
}
