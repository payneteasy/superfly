package com.payneteasy.superfly.policy.account.pcidss;

import junit.framework.TestCase;

import org.easymock.EasyMock;

import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.password.NullSaltSource;
import com.payneteasy.superfly.password.PasswordGeneratorImpl;
import com.payneteasy.superfly.password.PlaintextPasswordEncoder;
import com.payneteasy.superfly.password.UserPasswordEncoderImpl;

public class PCIDSSAccountPolicyTest extends TestCase {
	private PCIDSSAccountPolicy policy;
	private UserDao userDao;
	
	public void setUp() {
		userDao = EasyMock.createMock(UserDao.class);
		PasswordGeneratorImpl passwordGenerator = new PasswordGeneratorImpl();
		policy = new PCIDSSAccountPolicy();
		policy.setPasswordGenerator(passwordGenerator);
		UserPasswordEncoderImpl userPasswordEncoder = new UserPasswordEncoderImpl();
		userPasswordEncoder.setPasswordEncoder(new PlaintextPasswordEncoder());
		userPasswordEncoder.setSaltSource(new NullSaltSource());
		policy.setUserPasswordEncoder(userPasswordEncoder);
		policy.setUserDao(userDao);
	}
	
	public void testUnlockNotSuspendedUser() {
		EasyMock.expect(userDao.unlockUser(1L)).andReturn(RoutineResult.okResult());
		EasyMock.replay(userDao);
		
		assertNull(policy.unlockUser(1L, false));
		
		EasyMock.verify(userDao);
	}
	
	public void testUnlockSuspendedUser() {
		EasyMock.expect(userDao.unlockSuspendedUser(EasyMock.eq(1L), EasyMock.anyObject(String.class))).andReturn(RoutineResult.okResult());
		EasyMock.replay(userDao);
		
		assertNotNull(policy.unlockUser(1L, true));
		
		EasyMock.verify(userDao);
	}
}
