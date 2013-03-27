package com.payneteasy.superfly.policy.account.none;

import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.model.RoutineResult;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SimpleAccountPolicyTest {
	private SimpleAccountPolicy policy;
	private UserDao userDao;

    @Before
	public void setUp() {
		userDao = EasyMock.createMock(UserDao.class);
		policy = new SimpleAccountPolicy();
		policy.setUserDao(userDao);
	}

    @Test
	public void testUnlockUser() {
		EasyMock.expect(userDao.unlockUser(1L)).andReturn(RoutineResult.okResult());
		EasyMock.replay(userDao);
		
		String newPassword = policy.unlockUser(1L, true);
        Assert.assertNull(newPassword);
		
		EasyMock.verify(userDao);
	}
}
