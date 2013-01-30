package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.api.MessageSendException;
import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.UserLoginStatus;
import com.payneteasy.superfly.model.ui.user.UICloneUserRequest;
import com.payneteasy.superfly.model.ui.user.UIUser;
import com.payneteasy.superfly.model.ui.user.UIUserForCreate;
import com.payneteasy.superfly.password.ConstantSaltSource;
import com.payneteasy.superfly.password.MessageDigestPasswordEncoder;
import com.payneteasy.superfly.password.PlaintextPasswordEncoder;
import com.payneteasy.superfly.password.SHA256RandomGUIDSaltGenerator;
import com.payneteasy.superfly.policy.create.none.NoneCreateUserStrategy;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.NotificationService;
import com.payneteasy.superfly.spisupport.HOTPService;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.commons.codec.digest.DigestUtils;
import org.easymock.EasyMock;
import org.easymock.IAnswer;

import static org.easymock.EasyMock.anyObject;

public class UserServiceImplTest extends TestCase {
	
	private UserDao userDao;
	private UserServiceImpl userService;
	
	public void setUp() {
		userDao = EasyMock.createStrictMock(UserDao.class);
		userService = new UserServiceImpl();
		userService.setUserDao(userDao);
		userService.setLoggerSink(TrivialProxyFactory.createProxy(LoggerSink.class));
		userService.setNotificationService(TrivialProxyFactory.createProxy(NotificationService.class));
		MessageDigestPasswordEncoder encoder = new MessageDigestPasswordEncoder();
		encoder.setAlgorithm("sha1");
		userService.setPasswordEncoder(encoder);
		userService.setSaltSource(new ConstantSaltSource("c3pio"));
		userService.setHotpSaltGenerator(new SHA256RandomGUIDSaltGenerator());
		userService.setHotpService(TrivialProxyFactory.createProxy(HOTPService.class));
        userService.setCreateUserStrategy(new NoneCreateUserStrategy(userDao));
	}
	
	public void testCreateUserPasswordEncryption() throws MessageSendException {
		EasyMock.expect(userDao.createUser(anyObject(UIUserForCreate.class))).andAnswer(new IAnswer<RoutineResult>() {
			public RoutineResult answer() throws Throwable {
				UIUserForCreate user = (UIUserForCreate) EasyMock.getCurrentArguments()[0];
				assertEquals(DigestUtils.shaHex("secret{c3pio}"), user.getPassword());
				assertNotNull(user.getHotpSalt());
				user.setId(1L);
				return RoutineResult.okResult();
			}
		});
		EasyMock.replay(userDao);
		
		UIUserForCreate user = new UIUserForCreate();
		user.setUsername("pete");
		user.setPassword("secret");
		userService.createUser(user, "subsystem");
		
		EasyMock.verify(userDao);
	}
	
	public void testUpdateUserPasswordEncryption() {
		EasyMock.expect(userDao.updateUser(anyObject(UIUser.class))).andAnswer(new IAnswer<RoutineResult>() {
			public RoutineResult answer() throws Throwable {
				UIUser user = (UIUser) EasyMock.getCurrentArguments()[0];
				assertEquals(DigestUtils.shaHex("secret{c3pio}"), user.getPassword());
				return RoutineResult.okResult();
			}
		});
		EasyMock.replay(userDao);
		
		UIUser user = new UIUser();
		user.setUsername("pete");
		user.setPassword("secret");
		userService.updateUser(user);
		
		EasyMock.verify(userDao);
	}
	
	public void testCloneUserPasswordEncryption() throws MessageSendException {
		EasyMock.expect(userDao.cloneUser(anyObject(UICloneUserRequest.class))).andAnswer(new IAnswer<RoutineResult>() {
			public RoutineResult answer() throws Throwable {
				UICloneUserRequest user = (UICloneUserRequest) EasyMock.getCurrentArguments()[0];
				assertEquals(DigestUtils.shaHex("secret{c3pio}"), user.getPassword());
				user.setId(1L);
				return RoutineResult.okResult();
			}
		});
		EasyMock.replay(userDao);
		
		userService.cloneUser(1L, "pete", "secret", "email", "new key", null);
		
		EasyMock.verify(userDao);
	}
	
	public void testCloneUser() throws MessageSendException {
		EasyMock.expect(userDao.cloneUser(anyObject(UICloneUserRequest.class))).andAnswer(new IAnswer<RoutineResult>() {
			public RoutineResult answer() throws Throwable {
				UICloneUserRequest user = (UICloneUserRequest) EasyMock.getCurrentArguments()[0];
				assertEquals("pete", user.getUsername());
				assertNotNull(user.getPassword());
				assertEquals("new-email", user.getEmail());
				assertNotNull(user.getSalt());
				assertNotNull(user.getHotpSalt());
				user.setId(1L);
				return RoutineResult.okResult();
			}
		});
		EasyMock.replay(userDao);
		
		userService.cloneUser(1L, "pete", "secret", "new-email", "new key", null);
		
		EasyMock.verify(userDao);
	}

    public void testGetUserLoginStatusSuccess() {
        userService.setPasswordEncoder(new PlaintextPasswordEncoder());
        EasyMock.expect(userDao.getUserLoginStatus("pete", "password{c3pio}", "subsystem"))
                .andReturn("Y");
        EasyMock.replay(userDao);

        Assert.assertEquals(UserLoginStatus.SUCCESS, userService.getUserLoginStatus("pete", "password", "subsystem"));

        EasyMock.verify(userDao);
    }

    public void testGetUserLoginStatusFailed() {
        userService.setPasswordEncoder(new PlaintextPasswordEncoder());
        EasyMock.expect(userDao.getUserLoginStatus("stranger", "password{c3pio}", "subsystem"))
                .andReturn("N");
        EasyMock.replay(userDao);

        Assert.assertEquals(UserLoginStatus.FAILED, userService.getUserLoginStatus("stranger", "password", "subsystem"));

        EasyMock.verify(userDao);
    }

    public void testGetUserLoginStatusTemp() {
        userService.setPasswordEncoder(new PlaintextPasswordEncoder());
        EasyMock.expect(userDao.getUserLoginStatus("old-pete", "password{c3pio}", "subsystem"))
                .andReturn("T");
        EasyMock.replay(userDao);

        Assert.assertEquals(UserLoginStatus.TEMP_PASSWORD, userService.getUserLoginStatus("old-pete", "password", "subsystem"));

        EasyMock.verify(userDao);
    }
}
