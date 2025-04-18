package com.payneteasy.superfly.policy.password.pcidss;

import com.payneteasy.superfly.api.exceptions.PolicyValidationException;
import com.payneteasy.superfly.policy.IPolicy;
import com.payneteasy.superfly.policy.password.PasswordCheckContext;

/**
 * Kuccyp
 * Date: 06.10.2010
 * Time: 17:13:32
 * (C) 2010
 * Skype: kuccyp
 */
public class PasswordMinLen implements IPolicy<PasswordCheckContext> {

    public PasswordMinLen(int aMaxPasswordLen) {
        theMaxPasswordLen = aMaxPasswordLen;
    }

    public void apply(PasswordCheckContext aContext) throws PolicyValidationException {
        if(aContext.getPassword()==null)
            throw new PolicyValidationException(PolicyValidationException.EMPTY_PASSWORD);
        if(aContext.getPassword().length()<theMaxPasswordLen)
            throw new PolicyValidationException(PolicyValidationException.SHORT_PASSWORD);
    }


    protected final int theMaxPasswordLen;
}
