package com.payneteasy.superfly.security.authentication;

import com.payneteasy.superfly.api.SSOUser;

/**
 * Results of a successful username+password authentication for Superfly.
 * 
 * @author Roman Puchkovskiy
 */
public class UsernamePasswordAndOtpCheckedToken extends
        UsernamePasswordCheckedToken {
    private static final long serialVersionUID = -7096904519277638358L;

    public UsernamePasswordAndOtpCheckedToken(SSOUser ssoUser) {
        super(ssoUser);
    }

}
