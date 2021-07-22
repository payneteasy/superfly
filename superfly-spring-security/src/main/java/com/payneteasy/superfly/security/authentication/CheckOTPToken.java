package com.payneteasy.superfly.security.authentication;

import org.springframework.security.core.Authentication;

import com.payneteasy.superfly.api.SSOUser;

/**
 * {@link Authentication} which is a request to check an HOTP.
 * 
 * @author Roman Puchkovskiy
 */
public class CheckOTPToken extends SSOUserTransportAuthenticationToken {
    private static final long serialVersionUID = 2290145086843797962L;

    private String otp;

    public CheckOTPToken(SSOUser ssoUser, String otp) {
        super(ssoUser);
        this.otp = otp;
    }

    @Override
    public Object getCredentials() {
        return otp;
    }

}
