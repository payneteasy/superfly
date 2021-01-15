package com.payneteasy.superfly.security.authentication;

import com.payneteasy.superfly.api.SSOUser;

/**
 * Results of a successful username+password without required OTP (for next step) authentication for Superfly.
 * 
 * @author Igor Vasilyev
 */
public class OtpUsernamePasswordCheckedToken extends
        SSOUserTransportAuthenticationToken {
    private static final long serialVersionUID = -7096904519277638358L;

    public OtpUsernamePasswordCheckedToken(SSOUser ssoUser) {
        super(ssoUser);
    }

}
