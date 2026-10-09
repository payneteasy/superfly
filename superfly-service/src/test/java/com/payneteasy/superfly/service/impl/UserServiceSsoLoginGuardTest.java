package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.lockout.LockoutStrategy;
import com.payneteasy.superfly.model.UserLoginStatus;
import com.payneteasy.superfly.password.ConstantSaltSource;
import com.payneteasy.superfly.password.PasswordEncoder;
import com.payneteasy.superfly.service.LoggerSink;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * The SSO password step only checks users of the target subsystem: a denial is a plain FAILED that does not reach
 * the procedure or the lockout counter and costs the same hashing as a regular attempt.
 */
public class UserServiceSsoLoginGuardTest {

    private static final String LOCAL  = "superfly";
    private static final String TARGET = "billing";
    private static final String USER   = "victim";

    private UserDao userDao;
    private LockoutStrategy lockoutStrategy;
    private AtomicInteger encodeCalls;
    private UserServiceImpl service;

    @Before
    public void setUp() {
        userDao = createStrictMock(UserDao.class);
        lockoutStrategy = createStrictMock(LockoutStrategy.class);
        encodeCalls = new AtomicInteger();
        PasswordEncoder countingEncoder = (password, salt) -> {
            encodeCalls.incrementAndGet();
            return password + "{" + salt + "}";
        };
        service = new UserServiceImpl();
        service.setUserDao(userDao);
        service.setPasswordEncoder(countingEncoder);
        service.setLegacyPasswordEncoder(countingEncoder);
        service.setSaltSource(new ConstantSaltSource("salt"));
        service.setLockoutStrategy(lockoutStrategy);
        service.setLoggerSink(TrivialProxyFactory.createProxy(LoggerSink.class));
    }

    @Test
    public void foreignUserFailsWithoutProcedureOrLockout() {
        expect(userDao.userHasRolesInSubsystem(USER, LOCAL)).andReturn("N");
        expect(userDao.userHasRolesInSubsystem(USER, TARGET)).andReturn("N");
        replay(userDao, lockoutStrategy);

        assertEquals(UserLoginStatus.FAILED, service.checkUserCanLoginWithThisPassword(USER, "pass", TARGET));

        assertEquals(2, encodeCalls.get());
        verify(userDao, lockoutStrategy);
    }

    @Test
    public void localUserFailsWithoutProcedureOrLockout() {
        expect(userDao.userHasRolesInSubsystem(USER, LOCAL)).andReturn("Y");
        replay(userDao, lockoutStrategy);

        assertEquals(UserLoginStatus.FAILED, service.checkUserCanLoginWithThisPassword(USER, "pass", TARGET));

        assertEquals(2, encodeCalls.get());
        verify(userDao, lockoutStrategy);
    }

    @Test
    public void missingSubsystemFails() {
        replay(userDao, lockoutStrategy);

        assertEquals(UserLoginStatus.FAILED, service.checkUserCanLoginWithThisPassword(USER, "pass", null));

        verify(userDao, lockoutStrategy);
    }

    @Test
    public void deniedUserWithNullPasswordDoesNotHash() {
        expect(userDao.userHasRolesInSubsystem(USER, LOCAL)).andReturn("N");
        expect(userDao.userHasRolesInSubsystem(USER, TARGET)).andReturn("N");
        replay(userDao, lockoutStrategy);

        assertEquals(UserLoginStatus.FAILED, service.checkUserCanLoginWithThisPassword(USER, null, TARGET));

        assertEquals(0, encodeCalls.get());
        verify(userDao, lockoutStrategy);
    }

    @Test
    public void ownUserGoesToTheProcedure() {
        expect(userDao.userHasRolesInSubsystem(USER, LOCAL)).andReturn("N");
        expect(userDao.userHasRolesInSubsystem(USER, TARGET)).andReturn("Y");
        expect(userDao.getUserLoginStatus(eq(USER), eq("pass{salt}"), eq("pass{salt}"), eq(TARGET), anyObject(String.class)))
                .andReturn("Y");
        replay(userDao, lockoutStrategy);

        assertEquals(UserLoginStatus.SUCCESS, service.checkUserCanLoginWithThisPassword(USER, "pass", TARGET));

        verify(userDao, lockoutStrategy);
    }

    @Test
    public void accessibilityRule() {
        expect(userDao.userHasRolesInSubsystem(USER, LOCAL)).andReturn("N");
        expect(userDao.userHasRolesInSubsystem(USER, TARGET)).andReturn("Y");
        replay(userDao);

        assertTrue(service.isUserAccessibleFrom(USER, TARGET));
        assertFalse(service.isUserAccessibleFrom(null, TARGET));
        assertFalse(service.isUserAccessibleFrom(USER, null));
        verify(userDao);
    }
}
