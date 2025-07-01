package com.payneteasy.superfly.lockout.none;

import com.payneteasy.superfly.lockout.LockoutStrategy;
import com.payneteasy.superfly.model.LockoutType;
import com.payneteasy.superfly.spring.Policy;
import com.payneteasy.superfly.spring.conditional.OnPolicyCondition;
import org.springframework.stereotype.Component;

@OnPolicyCondition(Policy.NONE)
@Component
public class NoneLockoutStrategy implements LockoutStrategy {

    public void checkLoginsFailed(String userName, LockoutType lockoutType) {

    }

}
