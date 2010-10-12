package com.payneteasy.superfly.policy.account.none;

import org.springframework.beans.factory.annotation.Required;

import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.policy.account.AccountPolicy;

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

}
