package com.payneteasy.superfly.security;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.access.intercept.RunAsUserToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.security.authentication.CheckHOTPToken;
import com.payneteasy.superfly.security.authentication.HOTPCheckedToken;
import com.payneteasy.superfly.security.exception.BadOTPValueException;

/**
 * {@link AuthenticationProvider} which authenticates using the Superfly HOTP.
 * 
 * @author Roman Puchkovskiy
 */
public class SuperflyHOTPAuthenticationProvider implements AuthenticationProvider {

    private SSOService ssoService;
    private Class<?> supportedAuthenticationClass = CheckHOTPToken.class;

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
        if (authentication instanceof CheckHOTPToken) {
            CheckHOTPToken authRequest = (CheckHOTPToken) authentication;
            if (authRequest.getCredentials() == null) {
                throw new BadOTPValueException("Null HOTP value");
            }
            boolean ok = ssoService.authenticateUsingHOTP(authRequest.getName(), authRequest.getCredentials().toString());
            if (!ok) {
                throw new BadOTPValueException("Invalid HOTP value");
            }
            result = createAuthentication(authRequest.getSsoUser());
        }
        return result;
    }

    public boolean supports(Class<?> authentication) {
        return supportedAuthenticationClass.isAssignableFrom(authentication);
    }

    protected Authentication createAuthentication(SSOUser ssoUser) {
        return new HOTPCheckedToken(ssoUser);
    }

}
