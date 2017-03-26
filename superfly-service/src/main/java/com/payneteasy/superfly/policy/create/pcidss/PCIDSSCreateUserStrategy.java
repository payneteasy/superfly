package com.payneteasy.superfly.policy.create.pcidss;

import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.user.UICloneUserRequest;
import com.payneteasy.superfly.model.ui.user.UIUserForCreate;
import com.payneteasy.superfly.policy.create.CreateUserStrategy;

public class PCIDSSCreateUserStrategy implements CreateUserStrategy {
    private UserDao userDao;

    public PCIDSSCreateUserStrategy(UserDao userDao) {
       this.userDao = userDao;
    }

    public RoutineResult createUser(UIUserForCreate createUser) {
        createUser.setIsPasswordTemp(true);
        return userDao.createUser(createUser);
    }

    public RoutineResult cloneUser(UICloneUserRequest cloneUser) {
        cloneUser.setIsPasswordTemp(true);
        return userDao.cloneUser(cloneUser);
    }
}
