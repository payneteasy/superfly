package com.payneteasy.superfly.policy.create.none;

import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.user.UICloneUserRequest;
import com.payneteasy.superfly.model.ui.user.UIUserForCreate;
import com.payneteasy.superfly.policy.create.CreateUserStrategy;

public class NoneCreateUserStrategy implements CreateUserStrategy {
    private UserDao userDao;

    public NoneCreateUserStrategy(UserDao userDao) {
        this.userDao = userDao;
    }

    public RoutineResult createUser(UIUserForCreate createUser) {
        createUser.setIsPasswordTemp(false);
        return userDao.createUser(createUser);
    }

    public RoutineResult cloneUser(UICloneUserRequest cloneUser) {
        cloneUser.setIsPasswordTemp(false);
        return userDao.cloneUser(cloneUser);
    }
}
