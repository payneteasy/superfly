package com.payneteasy.superfly.security;

import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.security.authentication.CheckOTPToken;
import com.payneteasy.superfly.security.authentication.OTPCheckedToken;
import com.payneteasy.superfly.security.exception.BadOTPValueException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.access.intercept.RunAsUserToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * {@link AuthenticationProvider} which authenticates using the Superfly HOTP.
 * 
 * @author Igor Vasilyev
 */
public class SuperflyOTPAuthenticationProvider implements AuthenticationProvider {

    private SSOService ssoService;
    private Class<?> supportedAuthenticationClass = CheckOTPToken.class;

    @Required
    public void setSsoService(SSOService ssoService) {
        this.ssoService = ssoService;
    }

    public void setSupportedAuthenticationClass(Class<RunAsUserToken> clazz) {
        this.supportedAuthenticationClass = clazz;
    }

    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {
        Authentication result = null;
        if (authentication instanceof CheckOTPToken) {
            CheckOTPToken authRequest = (CheckOTPToken) authentication;
            if (authRequest.getCredentials() == null) {
                throw new BadOTPValueException("Null OTP secret");
            }
            boolean ok = ssoService.checkOtp(authRequest.getSsoUser(), authRequest.getCredentials().toString());
            if (!ok) {
                throw new BadOTPValueException("Invalid OTP secret");
            }
            result = createAuthentication(authRequest.getSsoUser());
        }
        return result;
    }

    public boolean supports(Class<?> authentication) {
        return supportedAuthenticationClass.isAssignableFrom(authentication);
    }

    protected Authentication createAuthentication(SSOUser ssoUser) {
        return new OTPCheckedToken(ssoUser);
    }

}
