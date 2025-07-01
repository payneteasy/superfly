package com.payneteasy.superfly.policy.password.none;

import com.payneteasy.superfly.policy.impl.AbstractPolicyValidation;
import com.payneteasy.superfly.policy.password.PasswordCheckContext;
import com.payneteasy.superfly.spring.Policy;
import com.payneteasy.superfly.spring.conditional.OnPolicyCondition;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Kuccyp
 * Date: 07.10.2010
 * Time: 11:23:17
 * (C) 2010
 * Skype: kuccyp
 */
@Component
@OnPolicyCondition(Policy.NONE)
public class DefaultPasswordPolicyValidation extends AbstractPolicyValidation<PasswordCheckContext>{
    @Override
    protected void init() {
    }
}
