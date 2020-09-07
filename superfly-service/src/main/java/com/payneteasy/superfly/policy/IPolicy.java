package com.payneteasy.superfly.policy;

import com.payneteasy.superfly.api.PolicyValidationException;

/**
 * Kuccyp
 * Date: 06.10.2010
 * Time: 16:19:03
 * (C) 2010
 * Skype: kuccyp
 */
public interface IPolicy<T extends IPolicyContext> {
    void apply(T aContext) throws PolicyValidationException;
}
