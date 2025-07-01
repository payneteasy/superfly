package com.payneteasy.superfly.password;

import com.payneteasy.superfly.spring.Policy;
import com.payneteasy.superfly.spring.conditional.OnPolicyCondition;
import org.springframework.stereotype.Component;

/**
 * Always supplies null for salt. This leads to unsalted passwords.
 *
 * @author Roman Puchkovskiy
 */
@OnPolicyCondition(Policy.NONE)
@Component
public class NullSaltSource implements SaltSource {

    public String getSalt(String username) {
        return null;
    }

    public String getSalt(long userId) {
        return null;
    }

}
