package com.payneteasy.superfly.policy.password.pcidss;

import com.payneteasy.superfly.policy.impl.AbstractPolicyValidation;
import com.payneteasy.superfly.policy.password.PasswordCheckContext;
import com.payneteasy.superfly.spring.Policy;
import com.payneteasy.superfly.spring.conditional.OnPolicyCondition;
import org.springframework.stereotype.Component;

/**
 * Kuccyp
 * Date: 07.10.2010
 * Time: 11:23:17
 * (C) 2010
 * Skype: kuccyp
 */

@Component
@OnPolicyCondition(Policy.PCIDSS)
public class PCIDSSPasswordPolicyValidation extends AbstractPolicyValidation<PasswordCheckContext>{
    private final static int MIN_LEN=7;
    private final static int HISTORY_DEPTH=4;

    @Override
    protected void init() {
        addPolicy(new PasswordMinLen(MIN_LEN));
        addPolicy(new PasswordComplex());
        addPolicy(new PasswordAlreadyExist(HISTORY_DEPTH));
    }
}
