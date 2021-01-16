package com.payneteasy.superfly.security.authentication;

import com.payneteasy.superfly.api.SSOUser;
import org.springframework.security.core.Authentication;

/**
 * {@link Authentication} which is a request to check an OTP.
 * 
 * @author Igor Vasilyev
 */
public class CheckOTPToken extends SSOUserTransportAuthenticationToken {
    private static final long serialVersionUID = 2290145086843797962L;

    private String hotp;

    public CheckOTPToken(SSOUser ssoUser, String hotp) {
        super(ssoUser);
        this.hotp = hotp;
    }

    @Override
    public Object getCredentials() {
        return hotp;
    }

}
