package com.payneteasy.superfly.spring;

import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.lockout.LockoutStrategy;
import com.payneteasy.superfly.lockout.none.NoneLockoutStrategy;
import com.payneteasy.superfly.lockout.pcidss.PCIDSSLockoutStrategy;

public class LockoutStrategyFactoryBean extends AbstractPolicyDependingFactoryBean<LockoutStrategy> {
	private LockoutStrategy lockoutStrategy;
	private Long maxLoginsFailed;
	private UserDao userDao;

	public void setMaxLoginsFailed(Long maxLoginsFailed) {
		this.maxLoginsFailed = maxLoginsFailed;
	}

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public LockoutStrategy getObject() throws Exception {
		if (lockoutStrategy == null) {
			Policy p = findPolicyByIdentifier();
			switch (p) {
			case NONE:
				lockoutStrategy = new NoneLockoutStrategy();
				break;
			case PCIDSS:
				lockoutStrategy = new PCIDSSLockoutStrategy(userDao, maxLoginsFailed);
				break;
			default:
				throw new IllegalArgumentException();
			}
		}
		return lockoutStrategy;
	}

	public Class<?> getObjectType() {
		return LockoutStrategy.class;
	}

	public boolean isSingleton() {
		return true;
	}

}
