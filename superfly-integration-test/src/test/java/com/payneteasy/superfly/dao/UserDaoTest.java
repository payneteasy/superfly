package com.payneteasy.superfly.dao;

import java.util.List;

import com.payneteasy.superfly.model.AuthRole;

public class UserDaoTest extends AbstractDaoTest {
	
	private UserDao userDao;
	
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public void testAuthenticate() {
		List<AuthRole> roles = userDao.authenticate("admin", "password", "test1", null, null);
		assertNotNull("Must authenticate successfully", roles);
	}
}
