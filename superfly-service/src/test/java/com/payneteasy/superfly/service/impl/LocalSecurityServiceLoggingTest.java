package com.payneteasy.superfly.service.impl;


import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;

import java.util.Collections;

import com.payneteasy.superfly.model.AuthSession;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.lockout.none.NoneLockoutStrategy;
import com.payneteasy.superfly.model.AuthRole;
import com.payneteasy.superfly.password.NullSaltSource;
import com.payneteasy.superfly.password.PlaintextPasswordEncoder;
import com.payneteasy.superfly.password.UserPasswordEncoderImpl;
import com.payneteasy.superfly.service.LocalSecurityService;

public class LocalSecurityServiceLoggingTest extends AbstractServiceLoggingTest {

    private LocalSecurityService localSecurityService;
    private UserDao userDao;

    @Before
    public void setUp() {
        LocalSecurityServiceImpl service = new LocalSecurityServiceImpl();
        userDao = EasyMock.createStrictMock(UserDao.class);
        service.setUserDao(userDao);
        service.setLoggerSink(loggerSink);
        service.setLocalRoleName("local");
        UserPasswordEncoderImpl userPasswordEncoder = new UserPasswordEncoderImpl();
        userPasswordEncoder.setPasswordEncoder(new PlaintextPasswordEncoder());
        userPasswordEncoder.setSaltSource(new NullSaltSource());
        service.setUserPasswordEncoder(userPasswordEncoder);
        service.setLockoutStrategy(new NoneLockoutStrategy());
        localSecurityService = service;
    }

    @Test
    public void testAuthenticateUser() throws Exception {
        final AuthRole role = new AuthRole();
        role.setRoleName("local");
        EasyMock.expect(userDao.authenticate(eq("username"), eq("password"),
                anyObject(String.class), anyObject(String.class), anyObject(String.class)))
                        .andReturn(new AuthSession("username", 1L){{setRoles(Collections.singletonList(role));}});
        loggerSink.info(anyObject(Logger.class), eq("LOCAL_LOGIN"), eq(true), eq("username"));
        EasyMock.replay(loggerSink, userDao);

        localSecurityService.authenticate("username", "password");

        EasyMock.verify(loggerSink);
    }

    @Test
    public void testAuthenticateUserFailNotNull() throws Exception {
        EasyMock.expect(userDao.authenticate(eq("username"), eq("password"),
                anyObject(String.class), anyObject(String.class), anyObject(String.class)))
                        .andReturn(new AuthSession("username"));
        loggerSink.info(anyObject(Logger.class), eq("LOCAL_LOGIN"), eq(false), eq("username"));
        EasyMock.replay(loggerSink, userDao);

        localSecurityService.authenticate("username", "password");

        EasyMock.verify(loggerSink);
    }

    @Test
    public void testAuthenticateUserFailWithNull() throws Exception {
           EasyMock.expect(userDao.authenticate(eq("username"), eq("password"),
                   anyObject(String.class), anyObject(String.class), anyObject(String.class)))
                           .andReturn(null);
           loggerSink.info(anyObject(Logger.class), eq("LOCAL_LOGIN"), eq(false), eq("username"));
           EasyMock.replay(loggerSink, userDao);

           localSecurityService.authenticate("username", "password");

           EasyMock.verify(loggerSink);
       }

}