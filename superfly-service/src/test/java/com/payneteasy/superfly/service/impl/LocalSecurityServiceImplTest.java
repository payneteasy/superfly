package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.common.utils.UserNames;
import com.payneteasy.superfly.lockout.none.NoneLockoutStrategy;
import com.payneteasy.superfly.lockout.LockoutStrategy;
import com.payneteasy.superfly.model.AuthAction;
import com.payneteasy.superfly.model.AuthRole;
import com.payneteasy.superfly.model.AuthSession;
import com.payneteasy.superfly.model.LockoutType;
import com.payneteasy.superfly.password.ConstantSaltSource;
import com.payneteasy.superfly.password.NullSaltSource;
import com.payneteasy.superfly.password.PlaintextPasswordEncoder;
import com.payneteasy.superfly.password.UserPasswordEncoder;
import com.payneteasy.superfly.password.UserPasswordEncoderImpl;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.UserService;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;

public class LocalSecurityServiceImplTest {
    private UserService userService;
    private LocalSecurityServiceImpl localSecurityService;

    @Before
    public void setUp() {
        userService = EasyMock.createStrictMock(UserService.class);
        LocalSecurityServiceImpl service = new LocalSecurityServiceImpl();
        service.setUserService(userService);
        service.setLoggerSink(TrivialProxyFactory.createProxy(LoggerSink.class));
        service.setLockoutStrategy(new NoneLockoutStrategy());
        localSecurityService = service;
    }

    @Test
    public void testTooLongOrEmptyNameFailsLikeUnknownUserWithoutDatabase() {
        UserPasswordEncoder userPasswordEncoder = EasyMock.createStrictMock(UserPasswordEncoder.class);
        localSecurityService.setUserPasswordEncoder(userPasswordEncoder);
        EasyMock.replay(userService, userPasswordEncoder);
        org.junit.Assert.assertNull(localSecurityService.authenticate(
                "a".repeat(UserNames.MAX_LENGTH + 1), "pass"));
        org.junit.Assert.assertNull(localSecurityService.authenticate("", "pass"));
        org.junit.Assert.assertNull(localSecurityService.authenticate(null, "pass"));
        EasyMock.verify(userService, userPasswordEncoder);
    }

    @Test
    public void testPasswordEncodingWithPlainTextAndNullSalt() {
        UserPasswordEncoderImpl userPasswordEncoder = new UserPasswordEncoderImpl();
        userPasswordEncoder.setPasswordEncoder(new PlaintextPasswordEncoder());
        userPasswordEncoder.setLegacyPasswordEncoder(new PlaintextPasswordEncoder());
        userPasswordEncoder.setSaltSource(new NullSaltSource());
        localSecurityService.setUserPasswordEncoder(userPasswordEncoder);
        EasyMock.expect(userService.userHasRolesInSubsystem("user", "superfly")).andReturn(true);
        userService.authenticate(eq("user"), eq("pass"), eq("pass"), anyObject(String.class), anyObject(String.class), anyObject(String.class));
        EasyMock.expectLastCall().andReturn(new AuthSession("user"));
        EasyMock.replay(userService);
        localSecurityService.authenticate("user", "pass");
        EasyMock.verify(userService);
    }

    @Test
    public void testPasswordEncodingWithPlainTextAndNonNullSalt() {
        UserPasswordEncoderImpl userPasswordEncoder = new UserPasswordEncoderImpl();
        userPasswordEncoder.setPasswordEncoder(new PlaintextPasswordEncoder());
        userPasswordEncoder.setLegacyPasswordEncoder(new PlaintextPasswordEncoder());
        userPasswordEncoder.setSaltSource(new ConstantSaltSource("salt"));
        localSecurityService.setUserPasswordEncoder(userPasswordEncoder);
        EasyMock.expect(userService.userHasRolesInSubsystem("user", "superfly")).andReturn(true);
        userService.authenticate(eq("user"), eq("pass{salt}"), eq("pass{salt}"), anyObject(String.class), anyObject(String.class), anyObject(String.class));
        EasyMock.expectLastCall().andReturn(new AuthSession("user"));
        EasyMock.replay(userService);
        localSecurityService.authenticate("user", "pass");
        EasyMock.verify(userService);
    }

    @Test
    public void testFailedPasswordCountsTowardsPasswordLockout() {
        LockoutStrategy lockoutStrategy = EasyMock.createStrictMock(LockoutStrategy.class);
        localSecurityService.setLockoutStrategy(lockoutStrategy);
        localSecurityService.setUserPasswordEncoder(plainEncoder());
        EasyMock.expect(userService.userHasRolesInSubsystem("user", "superfly")).andReturn(true);
        EasyMock.expect(userService.authenticate(eq("user"), eq("bad"), eq("bad"), anyObject(String.class), anyObject(String.class), anyObject(String.class)))
                .andReturn(null);
        lockoutStrategy.checkLoginsFailed("user", LockoutType.PASSWORD);
        EasyMock.replay(userService, lockoutStrategy);
        org.junit.Assert.assertNull(localSecurityService.authenticate("user", "bad"));
        EasyMock.verify(userService, lockoutStrategy);
    }

    @Test
    public void testCorrectPasswordWithoutRolesDoesNotCountTowardsLockout() {
        LockoutStrategy lockoutStrategy = EasyMock.createStrictMock(LockoutStrategy.class);
        localSecurityService.setLockoutStrategy(lockoutStrategy);
        localSecurityService.setUserPasswordEncoder(plainEncoder());
        EasyMock.expect(userService.userHasRolesInSubsystem("user", "superfly")).andReturn(true);
        EasyMock.expect(userService.authenticate(eq("user"), eq("pass"), eq("pass"), anyObject(String.class), anyObject(String.class), anyObject(String.class)))
                .andReturn(new AuthSession("user"));
        EasyMock.replay(userService, lockoutStrategy);
        org.junit.Assert.assertNull(localSecurityService.authenticate("user", "pass"));
        EasyMock.verify(userService, lockoutStrategy);
    }

    @Test
    public void testUserWithoutLocalRoleIsRejectedWithoutProcedureOrLockout() {
        LockoutStrategy lockoutStrategy = EasyMock.createStrictMock(LockoutStrategy.class);
        localSecurityService.setLockoutStrategy(lockoutStrategy);
        AtomicInteger encodeCalls = new AtomicInteger();
        UserPasswordEncoderImpl countingEncoder = new UserPasswordEncoderImpl();
        countingEncoder.setPasswordEncoder((password, salt) -> {
            encodeCalls.incrementAndGet();
            return password;
        });
        countingEncoder.setLegacyPasswordEncoder((password, salt) -> {
            encodeCalls.incrementAndGet();
            return password;
        });
        countingEncoder.setSaltSource(new NullSaltSource());
        localSecurityService.setUserPasswordEncoder(countingEncoder);
        EasyMock.expect(userService.userHasRolesInSubsystem("user", "superfly")).andReturn(false);
        EasyMock.replay(userService, lockoutStrategy);
        org.junit.Assert.assertNull(localSecurityService.authenticate("user", "pass"));
        // both hashes are computed to keep the response time independent of the user's subsystem
        org.junit.Assert.assertEquals(2, encodeCalls.get());
        EasyMock.verify(userService, lockoutStrategy);
    }

    @Test
    public void testAdminWithCorrectPasswordGetsActions() {
        localSecurityService.setUserPasswordEncoder(plainEncoder());
        AuthAction action = new AuthAction();
        action.setActionName("action_admin");
        AuthRole role = new AuthRole("admin");
        role.setActions(Collections.singletonList(action));
        AuthSession session = new AuthSession("user", 1L);
        session.setRoles(Collections.singletonList(role));
        EasyMock.expect(userService.userHasRolesInSubsystem("user", "superfly")).andReturn(true);
        EasyMock.expect(userService.authenticate(eq("user"), eq("pass"), eq("pass"), anyObject(String.class), anyObject(String.class), anyObject(String.class)))
                .andReturn(session);
        EasyMock.replay(userService);
        org.junit.Assert.assertArrayEquals(new String[]{"action_admin"}, localSecurityService.authenticate("user", "pass"));
        EasyMock.verify(userService);
    }

    private static UserPasswordEncoderImpl plainEncoder() {
        UserPasswordEncoderImpl encoder = new UserPasswordEncoderImpl();
        encoder.setPasswordEncoder(new PlaintextPasswordEncoder());
        encoder.setLegacyPasswordEncoder(new PlaintextPasswordEncoder());
        encoder.setSaltSource(new NullSaltSource());
        return encoder;
    }
}
