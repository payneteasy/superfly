package com.payneteasy.superfly.service.impl;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;

import java.util.Collections;

import org.easymock.EasyMock;
import org.slf4j.Logger;

import com.payneteasy.superfly.api.RoleGrantSpecification;
import com.payneteasy.superfly.api.UserExistsException;
import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.lockout.LockoutStrategy;
import com.payneteasy.superfly.model.AuthRole;
import com.payneteasy.superfly.model.UserRegisterRequest;
import com.payneteasy.superfly.password.NullSaltSource;
import com.payneteasy.superfly.password.PlaintextPasswordEncoder;
import com.payneteasy.superfly.policy.password.none.DefaultPasswordPolicyValidation;
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
		service.setPasswordEncoder(new PlaintextPasswordEncoder());
		service.setSaltSource(new NullSaltSource());
        service.setPolicyValidation(new DefaultPasswordPolicyValidation());
        service.setLockoutStrategy(TrivialProxyFactory.createProxy(LockoutStrategy.class));
		internalSSOService = service;
	}

	public void testRegisterUser() throws Exception {
		userDao.registerUser(anyObject(UserRegisterRequest.class));
		EasyMock.expectLastCall().andReturn(okResult());
		loggerSink.info(anyObject(Logger.class), eq("REGISTER_USER"), eq(true), eq("new-user"));
		EasyMock.replay(loggerSink, userDao);

		internalSSOService.registerUser("new-user", "new-password", "new-email", null, new RoleGrantSpecification[] {}, "user", "user", "question", "answer");

		EasyMock.verify(loggerSink);
	}

	public void testRegisterUserDuplicate() throws Exception {
		userDao.registerUser(anyObject(UserRegisterRequest.class));
		EasyMock.expectLastCall().andReturn(duplicateResult());
		loggerSink.info(anyObject(Logger.class), eq("REGISTER_USER"), eq(false), eq("new-user"));
		EasyMock.replay(loggerSink, userDao);

		try {
			internalSSOService.registerUser("new-user", "new-password", "new-email", null,
					new RoleGrantSpecification[] {}, "user", "user", "question", "answer");
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
			internalSSOService.registerUser("new-user", "new-password", "new-email", null,
					new RoleGrantSpecification[] {}, "user", "user", "question", "answer");
		} catch (IllegalStateException e) {
			// expected
		}

		EasyMock.verify(loggerSink);
	}

	public void testAuthenticate() throws Exception {
		EasyMock.expect(
				userDao.authenticate(eq("username"), eq("password"), anyObject(String.class), anyObject(String.class),
						anyObject(String.class))).andReturn(Collections.singletonList(new AuthRole()));
		loggerSink.info(anyObject(Logger.class), eq("REMOTE_LOGIN"), eq(true), eq("username"));
		EasyMock.replay(loggerSink, userDao);

		internalSSOService.authenticate("username", "password", null, null, null);

		EasyMock.verify(loggerSink);
	}

	public void testAuthenticateFail() throws Exception {
		EasyMock.expect(
				userDao.authenticate(eq("username"), eq("password"), anyObject(String.class), anyObject(String.class),
						anyObject(String.class))).andReturn(null);
		loggerSink.info(anyObject(Logger.class), eq("REMOTE_LOGIN"), eq(false), eq("username"));
		EasyMock.replay(loggerSink, userDao);

		internalSSOService.authenticate("username", "password", null, null, null);

		EasyMock.verify(loggerSink);
	}

	public void testAuthenticateNoRoles() throws Exception {
		EasyMock.expect(
				userDao.authenticate(eq("username"), eq("password"), anyObject(String.class), anyObject(String.class),
						anyObject(String.class))).andReturn(Collections.<AuthRole> emptyList());
		loggerSink.info(anyObject(Logger.class), eq("REMOTE_LOGIN"), eq(false), eq("username"));
		EasyMock.replay(loggerSink, userDao);

		internalSSOService.authenticate("username", "password", null, null, null);

		EasyMock.verify(loggerSink);
	}

}
