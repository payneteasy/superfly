package com.payneteasy.superfly.policy.password.pcidss;

import com.payneteasy.superfly.api.PolicyValidationException;
import com.payneteasy.superfly.policy.IPolicy;
import com.payneteasy.superfly.policy.password.PasswordCheckContext;

/**
 * Kuccyp
 * Date: 06.10.2010
 * Time: 17:13:32
 * (C) 2010
 * Skype: kuccyp
 */

/**
 * check user password history for   aHistoryLength depth
 */

public class PasswordAlreadyExist implements IPolicy<PasswordCheckContext> {

    public PasswordAlreadyExist(int aHistoryLength) {
        theHistoryLength = aHistoryLength;
    }

    public void apply(PasswordCheckContext aContext) throws PolicyValidationException {
        if(aContext.isPasswordExit(aContext.getPassword(),theHistoryLength))
            throw new PolicyValidationException(PolicyValidationException.EXISTING_PASSWORD);
    }

    private final int theHistoryLength;
}