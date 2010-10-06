package com.payneteasy.superfly.policy;

/**
 * Kuccyp
 * Date: 06.10.2010
 * Time: 16:19:03
 * (C) 2010
 * Skype: kuccyp
 */
public interface IPolicy {
    void apply(IPolicyContext aContext) throws PolicyException;
}
