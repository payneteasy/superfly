package com.payneteasy.superfly.policy.account.pcidss;

import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.password.NullSaltSource;
import com.payneteasy.superfly.password.PasswordGeneratorImpl;
import com.payneteasy.superfly.password.PlaintextPasswordEncoder;
import com.payneteasy.superfly.password.UserPasswordEncoderImpl;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PCIDSSAccountPolicyTest {
	private PCIDSSAccountPolicy policy;
	private UserDao userDao;

    @Before
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

    @Test
	public void testUnlockNotSuspendedUser() {
		EasyMock.expect(userDao.unlockUser(1L)).andReturn(RoutineResult.okResult());
		EasyMock.replay(userDao);

        Assert.assertNull(policy.unlockUser(1L, false));
		
		EasyMock.verify(userDao);
	}

    @Test
	public void testUnlockSuspendedUser() {
		EasyMock.expect(userDao.unlockSuspendedUser(EasyMock.eq(1L), EasyMock.anyObject(String.class))).andReturn(RoutineResult.okResult());
		EasyMock.replay(userDao);
		
        Assert.assertNotNull(policy.unlockUser(1L, true));
		
		EasyMock.verify(userDao);
	}
}
