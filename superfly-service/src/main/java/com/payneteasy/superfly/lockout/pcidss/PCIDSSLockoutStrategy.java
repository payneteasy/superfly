package com.payneteasy.superfly.lockout.pcidss;

import com.payneteasy.superfly.common.SuperflyProperties;
import com.payneteasy.superfly.lockout.LockoutStrategy;
import com.payneteasy.superfly.model.LockoutType;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.spring.Policy;
import com.payneteasy.superfly.spring.conditional.OnPolicyCondition;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@OnPolicyCondition(Policy.PCIDSS)
@Component
public class PCIDSSLockoutStrategy implements LockoutStrategy {
    private final UserService userService;
    private final SuperflyProperties properties;

    public PCIDSSLockoutStrategy(UserService userService, SuperflyProperties properties) {
        this.userService = userService;
        this.properties = properties;
    }

    public void checkLoginsFailed(String userName, LockoutType lockoutType) {
        userService.lockoutConditionnally(userName, properties.maxLoginsFailed(), lockoutType.name());
    }
}
