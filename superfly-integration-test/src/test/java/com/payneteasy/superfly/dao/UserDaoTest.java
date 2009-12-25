package com.payneteasy.superfly.dao;

import java.util.List;

import com.payneteasy.superfly.model.AuthRole;
import com.payneteasy.superfly.model.ui.user.UIUserForList;

public class UserDaoTest extends AbstractDaoTest {
	
	private UserDao userDao;
	
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public void testAuthenticate() {
		List<AuthRole> roles = userDao.authenticate("admin", "password", "test1", null, null);
		assertNotNull("Must authenticate successfully", roles);
	}
	
	public void testGetUsers() {
		List<UIUserForList> users;
		users = userDao.getUsers(0, 10, DaoConstants.DEFAULT_SORT_FIELD_NUMBER,
				"asc", null, null, null);
		assertNotNull("List cannot be null", users);
		users = userDao.getUsers(0, 10, DaoConstants.DEFAULT_SORT_FIELD_NUMBER,
				"asc", "someprefix", 1L, 2L);
		assertNotNull("List cannot be null", users);
	}
	
	public void testGetUsersCount() {
		int count = userDao.getUsersCount(null, null, null);
		assertTrue("Must get some users", count > 0);
		userDao.getUsersCount("someprefix", 1L, 2L);
	}
}
