package com.payneteasy.superfly.security.authentication;

import com.payneteasy.superfly.api.SSOUser;

/**
 * Results of a successful username+password authentication for Superfly.
 * 
 * @author Roman Puchkovskiy
 */
public class UsernamePasswordCheckedToken extends
        SSOUserTransportAuthenticationToken {
    private static final long serialVersionUID = -7096904519277638358L;

    public UsernamePasswordCheckedToken(SSOUser ssoUser) {
        super(ssoUser);
    }

}
