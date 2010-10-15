package com.payneteasy.superfly.lockout.pcidss;

import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.lockout.LockoutStrategy;
import com.payneteasy.superfly.model.LockoutType;

public class PCIDSSLockoutStrategy implements LockoutStrategy {

	private UserDao userDao;
    private Long maxLoginsFailed;
    
    public PCIDSSLockoutStrategy(UserDao userDao, Long maxLoginsFailed){
    	this.userDao=userDao;
    	this.maxLoginsFailed=maxLoginsFailed;
    }
	

	public void checkLoginsFailed(String userName, LockoutType lockoutType) {
		userDao.lockoutConditionnally(userName, maxLoginsFailed, lockoutType.name());
	}
}
