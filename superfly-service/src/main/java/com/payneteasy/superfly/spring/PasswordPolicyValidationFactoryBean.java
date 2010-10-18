package com.payneteasy.superfly.spring;

import com.payneteasy.superfly.policy.IPolicyValidation;
import com.payneteasy.superfly.policy.impl.AbstractPolicyValidation;
import com.payneteasy.superfly.policy.password.none.DefaultPasswordPolicyValidation;
import com.payneteasy.superfly.policy.password.pcidss.PCIDSSPasswordPolicyValidation;

/**
 * Kuccyp
 * Date: 07.10.2010
 * Time: 11:29:01
 * (C) 2010
 * Skype: kuccyp
 */
public class PasswordPolicyValidationFactoryBean extends AbstractPolicyDependingFactoryBean{

    AbstractPolicyValidation policyValidation;

    public Object getObject() throws Exception {
        if (policyValidation == null) {
            Policy p = findPolicyByIdentifier();
            switch (p) {
            case NONE:
                policyValidation = new DefaultPasswordPolicyValidation();
                break;
            case PCIDSS:
                policyValidation = new PCIDSSPasswordPolicyValidation();
                break;
            default:
                throw new IllegalArgumentException();
            }
        }
        return policyValidation;
    }

    public Class getObjectType() {
        return IPolicyValidation.class;
    }

    public boolean isSingleton() {
        return true;
    }
}
