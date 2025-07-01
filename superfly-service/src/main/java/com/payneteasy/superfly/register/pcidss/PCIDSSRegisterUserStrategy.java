package com.payneteasy.superfly.register.pcidss;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.UserRegisterRequest;
import com.payneteasy.superfly.register.RegisterUserStrategy;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.spring.Policy;
import com.payneteasy.superfly.spring.conditional.OnPolicyCondition;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@OnPolicyCondition(Policy.PCIDSS)
@AllArgsConstructor
public class PCIDSSRegisterUserStrategy implements RegisterUserStrategy {
    private final UserService userService;

    public RoutineResult registerUser(UserRegisterRequest registerUser) {
        registerUser.setIsPasswordTemp(true);
        return userService.registerUser(registerUser);
    }

}
