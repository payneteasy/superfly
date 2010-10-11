package com.payneteasy.superfly.service.impl;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;

import java.util.Collections;

import com.payneteasy.superfly.api.PolicyValidationException;
import com.payneteasy.superfly.policy.password.PasswordSaltPair;
import com.payneteasy.superfly.policy.password.none.DefaultPasswordPolicyValidation;
import junit.framework.TestCase;

import org.apache.commons.codec.digest.DigestUtils;
import org.easymock.EasyMock;
import org.easymock.IAnswer;

import com.payneteasy.superfly.api.RoleGrantSpecification;
import com.payneteasy.superfly.api.UserExistsException;
import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.lockout.LockoutStrategy;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.UserRegisterRequest;
import com.payneteasy.superfly.password.ConstantSaltSource;
import com.payneteasy.superfly.password.MessageDigestPasswordEncoder;
import com.payneteasy.superfly.password.NullSaltSource;
import com.payneteasy.superfly.password.PlaintextPasswordEncoder;
import com.payneteasy.superfly.password.SHA256RandomGUIDSaltGenerator;
import com.payneteasy.superfly.register.RegisterUserStrategy;
import com.payneteasy.superfly.register.none.NoneRegisterUserStrategy;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.NotificationService;
import com.payneteasy.superfly.spi.HOTPProvider;

public class InternalSSOServiceImplTest extends TestCase {
	
	private UserDao userDao;
	private InternalSSOServiceImpl internalSSOService;
	private HOTPProvider hotpProvider;
	private RegisterUserStrategy registerUserStrategy;
	
	public void setUp() {
		userDao = EasyMock.createStrictMock(UserDao.class);
		hotpProvider = EasyMock.createMock(HOTPProvider.class);
		registerUserStrategy = EasyMock.createMock(RegisterUserStrategy.class);
		InternalSSOServiceImpl service = new InternalSSOServiceImpl();
		service.setUserDao(userDao);
		service.setLoggerSink(TrivialProxyFactory.createProxy(LoggerSink.class));
		service.setNotificationService(TrivialProxyFactory.createProxy(NotificationService.class));
		service.setHOTPProvider(hotpProvider);
        service.setPolicyValidation(new DefaultPasswordPolicyValidation());
        service.setLockoutStrategy(TrivialProxyFactory.createProxy(LockoutStrategy.class));
        service.setRegisterUserStrategy(new NoneRegisterUserStrategy(userDao));
        service.setHotpSaltGenerator(new SHA256RandomGUIDSaltGenerator());
		internalSSOService = service;
	}
	
	public void testPasswordEncodingWithPlainTextAndNullSalt() {
		internalSSOService.setPasswordEncoder(new PlaintextPasswordEncoder());
		internalSSOService.setSaltSource(new NullSaltSource());
		userDao.authenticate(eq("user"), eq("pass"), anyObject(String.class), anyObject(String.class), anyObject(String.class));
		EasyMock.expectLastCall().andReturn(null);
		EasyMock.replay(userDao);
		internalSSOService.authenticate("user", "pass", null, null, null);
		EasyMock.verify(userDao);
	}
	
	public void testPasswordEncodingWithPlainTextAndNonNullSalt() {
		internalSSOService.setPasswordEncoder(new PlaintextPasswordEncoder());
		internalSSOService.setSaltSource(new ConstantSaltSource("salt"));
		userDao.authenticate(eq("user"), eq("pass{salt}"), anyObject(String.class), anyObject(String.class), anyObject(String.class));
		EasyMock.expectLastCall().andReturn(null);
		EasyMock.replay(userDao);
		internalSSOService.authenticate("user", "pass", null, null, null);
		EasyMock.verify(userDao);
	}
	
	public void testRegisterUserPasswordEncoding() throws UserExistsException, PolicyValidationException {
		MessageDigestPasswordEncoder encoder = new MessageDigestPasswordEncoder();
		encoder.setAlgorithm("md5");
		internalSSOService.setPasswordEncoder(encoder);
		internalSSOService.setSaltSource(new ConstantSaltSource("e2e4"));
		EasyMock.expect(userDao.getUserPasswordHistory("user")).andReturn(Collections.<PasswordSaltPair>emptyList());
		EasyMock.expect(userDao.registerUser(anyObject(UserRegisterRequest.class))).andAnswer(new IAnswer<RoutineResult>() {
			public RoutineResult answer() throws Throwable {
				UserRegisterRequest user = (UserRegisterRequest) EasyMock.getCurrentArguments()[0];
				assertEquals(DigestUtils.md5Hex("secret{e2e4}"), user.getPassword());
				assertNotNull(user.getHotpSalt());
				return RoutineResult.okResult();
			}
		});
		EasyMock.replay(userDao);
		internalSSOService.registerUser("user", "secret", "email", null, new RoleGrantSpecification[]{},"user", "user", "question", "answer");
		EasyMock.verify(userDao);
	}
	
	public void testAuthenticateHOTP() {
		EasyMock.expect(hotpProvider.authenticate("pete", "123456")).andReturn(true);
		EasyMock.replay(hotpProvider);
		assertTrue(internalSSOService.authenticateHOTP("pete", "123456"));
		EasyMock.verify(hotpProvider);
		
		EasyMock.reset(hotpProvider);
		EasyMock.expect(hotpProvider.authenticate("pete", "123456")).andReturn(false);
		EasyMock.replay(hotpProvider);
		assertFalse(internalSSOService.authenticateHOTP("pete", "123456"));
		EasyMock.verify(hotpProvider);
	}
}
