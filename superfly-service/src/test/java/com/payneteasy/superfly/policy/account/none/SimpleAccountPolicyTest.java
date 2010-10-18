package com.payneteasy.superfly.policy.account.none;

import org.easymock.EasyMock;

import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.model.RoutineResult;

import junit.framework.TestCase;

public class SimpleAccountPolicyTest extends TestCase {
	private SimpleAccountPolicy policy;
	private UserDao userDao;
	
	public void setUp() {
		userDao = EasyMock.createMock(UserDao.class);
		policy = new SimpleAccountPolicy();
		policy.setUserDao(userDao);
	}
	
	public void testUnlockUser() {
		EasyMock.expect(userDao.unlockUser(1L)).andReturn(RoutineResult.okResult());
		EasyMock.replay(userDao);
		
		String newPassword = policy.unlockUser(1L, true);
		assertNull(newPassword);
		
		EasyMock.verify(userDao);
	}
}
