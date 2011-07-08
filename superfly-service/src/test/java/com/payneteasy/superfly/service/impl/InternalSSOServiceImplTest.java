package com.payneteasy.superfly.service.impl;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;

import java.util.Collections;

import junit.framework.TestCase;

import org.apache.commons.codec.digest.DigestUtils;
import org.easymock.EasyMock;
import org.easymock.IAnswer;

import com.payneteasy.superfly.api.BadPublicKeyException;
import com.payneteasy.superfly.api.RoleGrantSpecification;
import com.payneteasy.superfly.crypto.pgp.PGPCrypto;
import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.lockout.LockoutStrategy;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.UserRegisterRequest;
import com.payneteasy.superfly.password.ConstantSaltSource;
import com.payneteasy.superfly.password.MessageDigestPasswordEncoder;
import com.payneteasy.superfly.password.NullSaltSource;
import com.payneteasy.superfly.password.PlaintextPasswordEncoder;
import com.payneteasy.superfly.password.SHA256RandomGUIDSaltGenerator;
import com.payneteasy.superfly.policy.password.PasswordSaltPair;
import com.payneteasy.superfly.policy.password.none.DefaultPasswordPolicyValidation;
import com.payneteasy.superfly.register.none.NoneRegisterUserStrategy;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.NotificationService;
import com.payneteasy.superfly.spi.HOTPProvider;
import com.payneteasy.superfly.spisupport.HOTPService;

public class InternalSSOServiceImplTest extends TestCase {
	
	private UserDao userDao;
	private InternalSSOServiceImpl internalSSOService;
	private HOTPProvider hotpProvider;
	private HOTPService hotpService;
	
	public void setUp() {
		userDao = EasyMock.createStrictMock(UserDao.class);
		hotpProvider = EasyMock.createMock(HOTPProvider.class);
		hotpService = EasyMock.createMock(HOTPService.class);
		InternalSSOServiceImpl service = new InternalSSOServiceImpl();
		service.setUserDao(userDao);
		service.setLoggerSink(TrivialProxyFactory.createProxy(LoggerSink.class));
		service.setNotificationService(TrivialProxyFactory.createProxy(NotificationService.class));
		service.setHotpProvider(hotpProvider);
        service.setPolicyValidation(new DefaultPasswordPolicyValidation());
        service.setLockoutStrategy(TrivialProxyFactory.createProxy(LockoutStrategy.class));
        service.setRegisterUserStrategy(new NoneRegisterUserStrategy(userDao));
        service.setHotpSaltGenerator(new SHA256RandomGUIDSaltGenerator());
        service.setHotpService(hotpService);
        service.setSaltSource(new ConstantSaltSource("abc"));
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
	
	public void testRegisterUserPasswordEncoding() throws Exception {
		MessageDigestPasswordEncoder encoder = new MessageDigestPasswordEncoder();
		encoder.setAlgorithm("md5");
		internalSSOService.setPasswordEncoder(encoder);
		internalSSOService.setSaltSource(new ConstantSaltSource("e2e4"));
		EasyMock.expect(userDao.getUserPasswordHistoryAndCurrentPassword("user")).andReturn(Collections.<PasswordSaltPair>emptyList());
		EasyMock.expect(userDao.registerUser(anyObject(UserRegisterRequest.class))).andAnswer(new IAnswer<RoutineResult>() {
			public RoutineResult answer() throws Throwable {
				UserRegisterRequest user = (UserRegisterRequest) EasyMock.getCurrentArguments()[0];
				assertEquals(DigestUtils.md5Hex("secret{e2e4}"), user.getPassword());
				assertNotNull(user.getHotpSalt());
				user.setUserid(1L);
				return RoutineResult.okResult();
			}
		});
		hotpService.sendTableIfSupported(1L);
		EasyMock.expectLastCall();
		EasyMock.replay(userDao, hotpService);
		internalSSOService.registerUser("user", "secret", "email", null, new RoleGrantSpecification[]{},"user", "user", "question", "answer", null);
		EasyMock.verify(userDao, hotpService);
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
	
	public void testRegisterUserWithBadPublicKey() throws Exception {
		internalSSOService.setPasswordEncoder(new PlaintextPasswordEncoder());
		try {
			internalSSOService.registerUser("username", "password", "email.domain.com",
					"subsystem", new RoleGrantSpecification[]{}, "name", "surname",
					"secretQuestion", "secretAnswer",
					"not a key, just junk!");
			fail();
		} catch (BadPublicKeyException e) {
			// expected
		}
	}
	
	public void testRegisterUserWithPrefixedAndPostfixedBadPublicKey() throws Exception {
		internalSSOService.setPasswordEncoder(new PlaintextPasswordEncoder());
		internalSSOService.setPublicKeyCrypto(new PGPCrypto());
		try {
			internalSSOService.registerUser("username", "password", "email.domain.com",
					"subsystem", new RoleGrantSpecification[]{}, "name", "surname",
					"secretQuestion", "secretAnswer",
					"-----BEGIN PGP PUBLIC KEY BLOCK-----not a key, just junk!-----END PGP PUBLIC KEY BLOCK-----");
			fail();
		} catch (BadPublicKeyException e) {
			// expected
		}
	}
	
	public void testRegisterUserWithNullOrEmptyPublicKey() throws Exception {
		internalSSOService.setPasswordEncoder(new PlaintextPasswordEncoder());
		internalSSOService.setPublicKeyCrypto(new PGPCrypto());
		
		EasyMock.expect(userDao.getUserPasswordHistoryAndCurrentPassword("username")).andReturn(Collections.<PasswordSaltPair>emptyList());
		EasyMock.expect(userDao.registerUser(anyObject(UserRegisterRequest.class))).andAnswer(new IAnswer<RoutineResult>() {
			public RoutineResult answer() throws Throwable {
				UserRegisterRequest user = (UserRegisterRequest) EasyMock.getCurrentArguments()[0];
				assertEquals(null, user.getPublicKey());
				return RoutineResult.okResult();
			}
		});
		EasyMock.replay(userDao);
		internalSSOService.registerUser("username", "password", "email.domain.com",
				"subsystem", new RoleGrantSpecification[]{}, "name", "surname",
				"secretQuestion", "secretAnswer",
				null);
		EasyMock.verify(userDao);
		
		EasyMock.reset(userDao);
		
		EasyMock.expect(userDao.getUserPasswordHistoryAndCurrentPassword("username")).andReturn(Collections.<PasswordSaltPair>emptyList());
		EasyMock.expect(userDao.registerUser(anyObject(UserRegisterRequest.class))).andAnswer(new IAnswer<RoutineResult>() {
			public RoutineResult answer() throws Throwable {
				UserRegisterRequest user = (UserRegisterRequest) EasyMock.getCurrentArguments()[0];
				assertEquals("", user.getPublicKey());
				return RoutineResult.okResult();
			}
		});
		EasyMock.replay(userDao);
		internalSSOService.registerUser("username", "password", "email.domain.com",
				"subsystem", new RoleGrantSpecification[]{}, "name", "surname",
				"secretQuestion", "secretAnswer",
				"");
		EasyMock.verify(userDao);
	}

}
