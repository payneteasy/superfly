package com.payneteasy.superfly.register.pcidss;

import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.UserRegisterRequest;
import com.payneteasy.superfly.register.RegisterUserStrategy;

public class PCIDSSRegisterUserStrategy implements RegisterUserStrategy {
	private UserDao userDao;

	public PCIDSSRegisterUserStrategy(UserDao userDao) {
       this.userDao = userDao;
	}

	public RoutineResult registerUser(UserRegisterRequest registerUser) {
		registerUser.setIsPasswordTemp(true);
		return userDao.registerUser(registerUser);
	}

}
