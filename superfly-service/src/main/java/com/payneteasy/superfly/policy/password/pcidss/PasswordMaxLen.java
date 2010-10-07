package com.payneteasy.superfly.policy.password.pcidss;

import com.payneteasy.superfly.policy.IPolicy;
import com.payneteasy.superfly.policy.IPolicyContext;
import com.payneteasy.superfly.policy.PolicyException;
import com.payneteasy.superfly.policy.password.PasswordCheckContext;

/**
 * Kuccyp
 * Date: 06.10.2010
 * Time: 17:13:32
 * (C) 2010
 * Skype: kuccyp
 */
public class PasswordMaxLen implements IPolicy<PasswordCheckContext> {

    public PasswordMaxLen(int aMaxPasswordLen) {
        theMaxPasswordLen = aMaxPasswordLen;
    }

    public void apply(PasswordCheckContext aContext) throws PolicyException {
        if(aContext.getPassword()==null)
            throw new PolicyException(PolicyException.EMPTY_PASSWORD);
        if(aContext.getPassword().length()<theMaxPasswordLen)
            throw new PolicyException(PolicyException.SHORT_PASSWORD);
    }


    protected final int theMaxPasswordLen;
}
