package com.payneteasy.superfly.policy;

import com.payneteasy.superfly.api.PolicyValidationException;

/**
 * Kuccyp
 * Date: 07.10.2010
 * Time: 11:10:41
 * (C) 2010
 * Skype: kuccyp
 */
public interface IPolicyValidation<T extends IPolicyContext> {
    void validate(T aContext) throws PolicyValidationException;
}
