package com.payneteasy.superfly.security.authentication;

import com.payneteasy.superfly.api.SSOUser;

/**
 * Token which contains info about a successful HOTP authentication.
 * 
 * @author Roman Puchkovskiy
 */
public class OTPCheckedToken extends SSOUserTransportAuthenticationToken {
    private static final long serialVersionUID = -7861332629416740520L;

    public OTPCheckedToken(SSOUser ssoUser) {
        super(ssoUser);
    }

}
