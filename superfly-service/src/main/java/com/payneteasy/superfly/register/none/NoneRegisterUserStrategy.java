package com.payneteasy.superfly.register.none;

import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.UserRegisterRequest;
import com.payneteasy.superfly.register.RegisterUserStrategy;

public class NoneRegisterUserStrategy implements RegisterUserStrategy {
	private UserDao userDao;
	private final String TEMPORARY_PASSWORD_NO = "N";

	public NoneRegisterUserStrategy(UserDao userDao) {
		this.userDao = userDao;
	}

	public RoutineResult registerUser(UserRegisterRequest registerUser) {
		registerUser.setIsPasswordTemp(TEMPORARY_PASSWORD_NO);
		return userDao.registerUser(registerUser);
	}

}
