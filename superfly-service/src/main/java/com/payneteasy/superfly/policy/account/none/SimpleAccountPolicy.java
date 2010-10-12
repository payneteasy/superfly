package com.payneteasy.superfly.policy.account.none;

import org.springframework.beans.factory.annotation.Required;

import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.policy.account.AccountPolicy;
import com.payneteasy.superfly.service.UserService;

/**
 * {@link AccountPolicy} which behaves as simple as it's possible.
 *
 * @author Roman Puchkovskiy
 */
public class SimpleAccountPolicy implements AccountPolicy {
	
	private UserDao userDao;
	
	@Required
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public String unlockUser(long userId, boolean unlockingSuspendedUser) {
		userDao.unlockUser(userId);
		return null;
	}

	public void suspendUsersIfNeeded(int days, UserService userService) {
		// doing nothing as suspension is not needed here
	}

	public void expirePasswordsIfNeeded(int days, UserService userService) {
		// doing nothing as password expiration is not needed here
	}

}
