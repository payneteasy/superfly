package com.payneteasy.superfly.policy.impl;

import com.payneteasy.superfly.policy.IPolicy;
import com.payneteasy.superfly.policy.IPolicyContext;
import com.payneteasy.superfly.policy.IPolicyValidation;
import com.payneteasy.superfly.api.PolicyValidationException;

import java.util.ArrayList;
import java.util.List;

/**
 * Kuccyp
 * Date: 07.10.2010
 * Time: 11:17:51
 * (C) 2010
 * Skype: kuccyp
 */
public abstract class AbstractPolicyValidation<T extends IPolicyContext> implements IPolicyValidation<T>{

    public AbstractPolicyValidation() {
        init();
    }

    protected List<IPolicy<T>> policyList=new ArrayList<IPolicy<T>>();

    public void validate(T aContext) throws PolicyValidationException {
       for(IPolicy<T> policy:policyList){
           policy.apply(aContext);
       }
    }

    protected void addPolicy(IPolicy<T> policy){
        policyList.add(policy);
    }

    protected abstract void init();
}
