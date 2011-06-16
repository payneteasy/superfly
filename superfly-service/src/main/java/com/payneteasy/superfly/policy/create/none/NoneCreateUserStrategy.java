package com.payneteasy.superfly.policy.create.none;

import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.UserRegisterRequest;
import com.payneteasy.superfly.model.ui.user.UIUserForCreate;
import com.payneteasy.superfly.policy.create.CreateUserStrategy;
import com.payneteasy.superfly.register.RegisterUserStrategy;

public class NoneCreateUserStrategy implements CreateUserStrategy {
	private UserDao userDao;
	private final String TEMPORARY_PASSWORD_NO = "N";

	public NoneCreateUserStrategy(UserDao userDao) {
		this.userDao = userDao;
	}

    public RoutineResult createUser(UIUserForCreate createUser) {
        createUser.setIsPasswordTemp(TEMPORARY_PASSWORD_NO);
        return userDao.createUser(createUser);
    }
}
