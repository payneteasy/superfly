package com.payneteasy.superfly.security;

import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.security.authentication.CheckGoogleAuthToken;
import org.springframework.security.core.Authentication;

/**
 * Filter which processes an Google Auth TOTP authentication. It assumes that user has
 * already been authenticated by password authentication.
 *
 * @author Igor Vasilyev
 */
public class SuperflyGoogleAuthenticationProcessingFilter extends
        SuperflyHOTPAuthenticationProcessingFilter {

    public SuperflyGoogleAuthenticationProcessingFilter() {
        super();
        setFilterProcessesUrl("/j_superfly_ga_security_check");
    }

    @Override
    protected Authentication createCheckHotpAuthRequest(String hotp, SSOUser ssoUser) {
        return new CheckGoogleAuthToken(ssoUser, hotp);
    }

}
