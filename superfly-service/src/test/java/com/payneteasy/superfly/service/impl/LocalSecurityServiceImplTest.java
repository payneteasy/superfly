package com.payneteasy.superfly.service.impl;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import junit.framework.TestCase;

import org.easymock.EasyMock;

import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.lockout.none.NoneLockoutStrategy;
import com.payneteasy.superfly.password.ConstantSaltSource;
import com.payneteasy.superfly.password.NullSaltSource;
import com.payneteasy.superfly.password.PlaintextPasswordEncoder;
import com.payneteasy.superfly.service.LoggerSink;

public class LocalSecurityServiceImplTest extends TestCase {
	private UserDao userDao;
	private LocalSecurityServiceImpl localSecurityService;
	
	public void setUp() {
		userDao = EasyMock.createStrictMock(UserDao.class);
		LocalSecurityServiceImpl service = new LocalSecurityServiceImpl();
		service.setUserDao(userDao);
		service.setLoggerSink(TrivialProxyFactory.createProxy(LoggerSink.class));
		service.setLockoutStrategy(new NoneLockoutStrategy());
		localSecurityService = service;
	}
	
	public void testPasswordEncodingWithPlainTextAndNullSalt() {
		localSecurityService.setPasswordEncoder(new PlaintextPasswordEncoder());
		localSecurityService.setSaltSource(new NullSaltSource());
		userDao.authenticate(eq("user"), eq("pass"), anyObject(String.class), anyObject(String.class), anyObject(String.class));
		EasyMock.expectLastCall().andReturn(null);
		EasyMock.replay(userDao);
		localSecurityService.authenticate("user", "pass");
		EasyMock.verify(userDao);
	}
	
	public void testPasswordEncodingWithPlainTextAndNonNullSalt() {
		localSecurityService.setPasswordEncoder(new PlaintextPasswordEncoder());
		localSecurityService.setSaltSource(new ConstantSaltSource("salt"));
		userDao.authenticate(eq("user"), eq("pass{salt}"), anyObject(String.class), anyObject(String.class), anyObject(String.class));
		EasyMock.expectLastCall().andReturn(null);
		EasyMock.replay(userDao);
		localSecurityService.authenticate("user", "pass");
		EasyMock.verify(userDao);
	}
}
