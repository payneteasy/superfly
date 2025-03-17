package com.payneteasy.superfly.policy.account.none;

import com.payneteasy.superfly.policy.account.AccountPolicy;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.spring.Policy;
import com.payneteasy.superfly.spring.conditional.OnPolicyCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * {@link AccountPolicy} which behaves as simple as it's possible.
 *
 * @author Roman Puchkovskiy
 */
@Component
@OnPolicyCondition(Policy.NONE)
public class SimpleAccountPolicy implements AccountPolicy {

    private UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public String unlockUser(long userId, boolean unlockingSuspendedUser) {
        userService.unlockUser(userId);
        return null;
    }

    public void suspendUsersIfNeeded(int days, UserService userService) {
        // doing nothing as suspension is not needed here
    }

    public void expirePasswordsIfNeeded(int days, UserService userService) {
        // doing nothing as password expiration is not needed here
    }

}
