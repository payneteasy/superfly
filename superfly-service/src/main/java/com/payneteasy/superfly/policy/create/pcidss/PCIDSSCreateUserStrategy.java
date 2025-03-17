package com.payneteasy.superfly.policy.create.pcidss;

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
@OnPolicyCondition(Policy.PCIDSS)
@AllArgsConstructor
public class PCIDSSCreateUserStrategy implements CreateUserStrategy {
    private final UserService userService;

    public RoutineResult createUser(UIUserForCreate createUser) {
        createUser.setIsPasswordTemp(true);
        return userService.createUser(createUser);
    }

    public RoutineResult cloneUser(UICloneUserRequest cloneUser) {
        cloneUser.setIsPasswordTemp(true);
        return userService.cloneUser(cloneUser);
    }
}
