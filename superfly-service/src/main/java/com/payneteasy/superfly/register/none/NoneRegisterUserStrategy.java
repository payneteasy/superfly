package com.payneteasy.superfly.register.none;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.UserRegisterRequest;
import com.payneteasy.superfly.register.RegisterUserStrategy;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.spring.Policy;
import com.payneteasy.superfly.spring.conditional.OnPolicyCondition;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@OnPolicyCondition(Policy.NONE)
@AllArgsConstructor
public class NoneRegisterUserStrategy implements RegisterUserStrategy {
    private final UserService userService;

    public RoutineResult registerUser(UserRegisterRequest registerUser) {
        registerUser.setIsPasswordTemp(false);
        return userService.registerUser(registerUser);
    }

}
