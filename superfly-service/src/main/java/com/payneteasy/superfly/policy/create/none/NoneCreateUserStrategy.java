package com.payneteasy.superfly.policy.create.none;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.user.UICloneUserRequest;
import com.payneteasy.superfly.model.ui.user.UIUserForCreate;
import com.payneteasy.superfly.policy.create.CreateUserStrategy;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.spring.Policy;
import com.payneteasy.superfly.spring.conditional.OnPolicyCondition;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@OnPolicyCondition(Policy.NONE)
@AllArgsConstructor
public class NoneCreateUserStrategy implements CreateUserStrategy {
    private final UserService userService;

    public RoutineResult createUser(UIUserForCreate createUser) {
        createUser.setIsPasswordTemp(false);
        return userService.createUser(createUser);
    }

    public RoutineResult cloneUser(UICloneUserRequest cloneUser) {
        cloneUser.setIsPasswordTemp(false);
        return userService.cloneUser(cloneUser);
    }
}
