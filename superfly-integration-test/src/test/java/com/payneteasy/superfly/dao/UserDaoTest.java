package com.payneteasy.superfly.dao;

import java.util.List;

import com.payneteasy.superfly.model.AuthRole;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.user.UIUser;
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
	
	public void testGetUser() {
		UIUser user = userDao.getUser(-111L);
		assertNull("Must not find a user with ID=111", user);
	}
	
	public void testCreateUser() {
		UIUser user = new UIUser();
		user.setUsername("newuser");
		user.setPassword("secret");
		RoutineResult result = userDao.createUser(user);
		assertRoutineResult(result);
		assertNotNull("User ID must be generated", user.getId());
		
		user = userDao.getUser(user.getId());
		assertNotNull("Must find a user we have just created", user);
	}
	
	public void testUpdateUser() {
		UIUser user = getAnyUser();
		user.setPassword("password");
		RoutineResult result = userDao.updateUser(user);
		assertRoutineResult(result);
	}

	private UIUser getAnyUser() {
		long userId = getAnyUserId();
		UIUser user = userDao.getUser(userId);
		return user;
	}

	private long getAnyUserId() {
		List<UIUserForList> users = userDao.getUsers(0, 1, 1, "asc", null, null,
				null);
		UIUserForList userForList = users.get(0);
		long userId = userForList.getId();
		return userId;
	}
	
	public void testDeleteUser() {
		long userId = getAnyUserId();
		RoutineResult result = userDao.deleteUser(userId);
		assertRoutineResult(result);

		UIUser user = userDao.getUser(userId);
		// TODO: uncomment the assertion when user deletion is implemented
//		assertNull("User must not be found as it has been deleted", user);
	}
}
