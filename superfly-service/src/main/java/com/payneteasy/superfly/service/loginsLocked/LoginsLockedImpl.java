package com.payneteasy.superfly.service.loginsLocked;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import com.payneteasy.superfly.dao.UserDao;

@Transactional
public class LoginsLockedImpl implements LoginsLocked {

	private Long maxLoginsFailed;
	private Boolean isLoginsLocked;
	private UserDao userDao;

	@Required
	public void setMaxLoginsFailed(Long maxLoginsFailed) {
		this.maxLoginsFailed = maxLoginsFailed;
	}

	@Required
	public void setIsLoginsLocked(Boolean isLoginsLocked) {
		this.isLoginsLocked = isLoginsLocked;
	}

	@Required
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public void checkLoginsFailed(String userName, String password) {
       if(isLoginsLocked){
    	   userDao.loginLocked(userName, password, maxLoginsFailed);
       }
	}
}
