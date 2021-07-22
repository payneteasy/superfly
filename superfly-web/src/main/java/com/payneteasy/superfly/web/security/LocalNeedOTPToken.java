package com.payneteasy.superfly.web.security;

import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.security.authentication.EmptyAuthenticationToken;

/**
 * Request to check an OTP.
 *
 * @author Igor Vasilev
 */
public class LocalNeedOTPToken extends EmptyAuthenticationToken {
    private final String username;
    private final OTPType otpType;

    public LocalNeedOTPToken(String username, OTPType otpType) {
        super();
        this.username = username;
        this.otpType = otpType;
    }

    @Override
    public String getName() {
        return username;
    }

    public OTPType getOtpType() {
        return otpType;
    }
}
