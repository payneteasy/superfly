package com.payneteasy.superfly.register.none;

import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.UserRegisterRequest;
import com.payneteasy.superfly.register.RegisterUserStrategy;
import com.payneteasy.superfly.service.UserService;

public class NoneRegisterUserStrategy implements RegisterUserStrategy {
    private final UserService userService;

    public NoneRegisterUserStrategy(UserService userService) {
        this.userService = userService;
    }

    public RoutineResult registerUser(UserRegisterRequest registerUser) {
        registerUser.setIsPasswordTemp(false);
        return userService.registerUser(registerUser);
    }

}
