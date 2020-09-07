package com.payneteasy.superfly.policy.create.pcidss;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.user.UICloneUserRequest;
import com.payneteasy.superfly.model.ui.user.UIUserForCreate;
import com.payneteasy.superfly.policy.create.CreateUserStrategy;
import com.payneteasy.superfly.service.UserService;

public class PCIDSSCreateUserStrategy implements CreateUserStrategy {
    private final UserService userService;

    public PCIDSSCreateUserStrategy(UserService userService) {
       this.userService = userService;
    }

    public RoutineResult createUser(UIUserForCreate createUser) {
        createUser.setIsPasswordTemp(true);
        return userService.createUser(createUser);
    }

    public RoutineResult cloneUser(UICloneUserRequest cloneUser) {
        cloneUser.setIsPasswordTemp(true);
        return userService.cloneUser(cloneUser);
    }
}
