package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.lockout.none.NoneLockoutStrategy;
import com.payneteasy.superfly.model.AuthSession;
import com.payneteasy.superfly.password.ConstantSaltSource;
import com.payneteasy.superfly.password.NullSaltSource;
import com.payneteasy.superfly.password.PlaintextPasswordEncoder;
import com.payneteasy.superfly.password.UserPasswordEncoderImpl;
import com.payneteasy.superfly.service.LoggerSink;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;

public class LocalSecurityServiceImplTest {
	private UserDao userDao;
	private LocalSecurityServiceImpl localSecurityService;

    @Before
	public void setUp() {
		userDao = EasyMock.createStrictMock(UserDao.class);
		LocalSecurityServiceImpl service = new LocalSecurityServiceImpl();
		service.setUserDao(userDao);
		service.setLoggerSink(TrivialProxyFactory.createProxy(LoggerSink.class));
		service.setLockoutStrategy(new NoneLockoutStrategy());
		localSecurityService = service;
	}

    @Test
	public void testPasswordEncodingWithPlainTextAndNullSalt() {
		UserPasswordEncoderImpl userPasswordEncoder = new UserPasswordEncoderImpl();
		userPasswordEncoder.setPasswordEncoder(new PlaintextPasswordEncoder());
		userPasswordEncoder.setSaltSource(new NullSaltSource());
		localSecurityService.setUserPasswordEncoder(userPasswordEncoder);
		userDao.authenticate(eq("user"), eq("pass"), anyObject(String.class), anyObject(String.class), anyObject(String.class));
		EasyMock.expectLastCall().andReturn(new AuthSession("user"));
		EasyMock.replay(userDao);
		localSecurityService.authenticate("user", "pass");
		EasyMock.verify(userDao);
	}

    @Test
	public void testPasswordEncodingWithPlainTextAndNonNullSalt() {
		UserPasswordEncoderImpl userPasswordEncoder = new UserPasswordEncoderImpl();
		userPasswordEncoder.setPasswordEncoder(new PlaintextPasswordEncoder());
		userPasswordEncoder.setSaltSource(new ConstantSaltSource("salt"));
		localSecurityService.setUserPasswordEncoder(userPasswordEncoder);
		userDao.authenticate(eq("user"), eq("pass{salt}"), anyObject(String.class), anyObject(String.class), anyObject(String.class));
		EasyMock.expectLastCall().andReturn(new AuthSession("user"));
		EasyMock.replay(userDao);
		localSecurityService.authenticate("user", "pass");
		EasyMock.verify(userDao);
	}
}
