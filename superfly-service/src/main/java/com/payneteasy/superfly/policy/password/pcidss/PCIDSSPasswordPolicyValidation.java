package com.payneteasy.superfly.policy.password.pcidss;

import com.payneteasy.superfly.policy.impl.AbstractPolicyValidation;
import com.payneteasy.superfly.policy.password.PasswordCheckContext;

/**
 * Kuccyp
 * Date: 07.10.2010
 * Time: 11:23:17
 * (C) 2010
 * Skype: kuccyp
 */
public class PCIDSSPasswordPolicyValidation extends AbstractPolicyValidation<PasswordCheckContext>{
    public void init() {
        addPolicy(new PasswordMaxLen(7));
        addPolicy(new PasswordComplex());
    }
}
