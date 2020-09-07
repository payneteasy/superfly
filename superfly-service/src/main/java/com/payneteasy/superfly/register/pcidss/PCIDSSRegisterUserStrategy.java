package com.payneteasy.superfly.register.pcidss;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.UserRegisterRequest;
import com.payneteasy.superfly.register.RegisterUserStrategy;
import com.payneteasy.superfly.service.UserService;

public class PCIDSSRegisterUserStrategy implements RegisterUserStrategy {
    private final UserService userService;

    public PCIDSSRegisterUserStrategy(UserService userService) {
       this.userService = userService;
    }

    public RoutineResult registerUser(UserRegisterRequest registerUser) {
        registerUser.setIsPasswordTemp(true);
        return userService.registerUser(registerUser);
    }

}
