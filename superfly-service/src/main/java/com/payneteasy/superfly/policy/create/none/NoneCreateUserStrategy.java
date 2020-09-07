package com.payneteasy.superfly.policy.create.none;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.user.UICloneUserRequest;
import com.payneteasy.superfly.model.ui.user.UIUserForCreate;
import com.payneteasy.superfly.policy.create.CreateUserStrategy;
import com.payneteasy.superfly.service.UserService;

public class NoneCreateUserStrategy implements CreateUserStrategy {
    private final UserService userService;

    public NoneCreateUserStrategy(UserService userService) {
        this.userService = userService;
    }

    public RoutineResult createUser(UIUserForCreate createUser) {
        createUser.setIsPasswordTemp(false);
        return userService.createUser(createUser);
    }

    public RoutineResult cloneUser(UICloneUserRequest cloneUser) {
        cloneUser.setIsPasswordTemp(false);
        return userService.cloneUser(cloneUser);
    }
}
