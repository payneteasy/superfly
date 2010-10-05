package com.payneteasy.superfly.service.impl;


import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;

import org.easymock.EasyMock;
import org.slf4j.Logger;

import com.payneteasy.superfly.api.RoleGrantSpecification;
import com.payneteasy.superfly.api.UserExistsException;
import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.model.UserRegisterRequest;
import com.payneteasy.superfly.service.InternalSSOService;
import com.payneteasy.superfly.service.NotificationService;

public class InternalSSOServiceLoggingTest extends AbstractServiceLoggingTest {
	
	private InternalSSOService internalSSOService;
	private UserDao userDao;
	
	public void setUp() {
		super.setUp();
		InternalSSOServiceImpl service = new InternalSSOServiceImpl();
		userDao = EasyMock.createStrictMock(UserDao.class);
		service.setUserDao(userDao);
		service.setNotificationService(TrivialProxyFactory.createProxy(NotificationService.class));
		service.setLoggerSink(loggerSink);
		internalSSOService = service;
	}
	
	public void testRegisterUser() throws Exception {
		userDao.registerUser(anyObject(UserRegisterRequest.class));
		EasyMock.expectLastCall().andReturn(okResult());
		loggerSink.info(anyObject(Logger.class), eq("REGISTER_USER"), eq(true), eq("new-user"));
		EasyMock.replay(loggerSink, userDao);
		
		internalSSOService.registerUser("new-user", "new-password", "new-email", null, new RoleGrantSpecification[]{});
		
		EasyMock.verify(loggerSink);
	}
	
	public void testRegisterUserDuplicate() throws Exception {
		userDao.registerUser(anyObject(UserRegisterRequest.class));
		EasyMock.expectLastCall().andReturn(duplicateResult());
		loggerSink.info(anyObject(Logger.class), eq("REGISTER_USER"), eq(false), eq("new-user"));
		EasyMock.replay(loggerSink, userDao);
		
		try {
			internalSSOService.registerUser("new-user", "new-password", "new-email", null, new RoleGrantSpecification[]{});
		} catch (UserExistsException e) {
			// expected
		}
		
		EasyMock.verify(loggerSink);
	}
	
	public void testRegisterUserFail() throws Exception {
		userDao.registerUser(anyObject(UserRegisterRequest.class));
		EasyMock.expectLastCall().andReturn(failureResult());
		loggerSink.info(anyObject(Logger.class), eq("REGISTER_USER"), eq(false), eq("new-user"));
		EasyMock.replay(loggerSink, userDao);
		
		try {
			internalSSOService.registerUser("new-user", "new-password", "new-email", null, new RoleGrantSpecification[]{});
		} catch (IllegalStateException e) {
			// expected
		}
		
		EasyMock.verify(loggerSink);
	}
	
}
