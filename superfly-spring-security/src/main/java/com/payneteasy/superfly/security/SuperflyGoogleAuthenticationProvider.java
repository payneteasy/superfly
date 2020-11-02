package com.payneteasy.superfly.security;

import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.api.SsoDecryptException;
import com.payneteasy.superfly.security.authentication.CheckGoogleAuthToken;
import com.payneteasy.superfly.security.authentication.CheckHOTPToken;
import com.payneteasy.superfly.security.authentication.HOTPCheckedToken;
import com.payneteasy.superfly.security.exception.BadGoogleAuthValueException;
import com.payneteasy.superfly.security.exception.BadOTPValueException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.access.intercept.RunAsUserToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * {@link AuthenticationProvider} which authenticates using the Superfly HOTP.
 * 
 * @author Roman Puchkovskiy
 */
public class SuperflyGoogleAuthenticationProvider implements AuthenticationProvider {

    private SSOService ssoService;
    private Class<?> supportedAuthenticationClass = CheckGoogleAuthToken.class;

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
        if (authentication instanceof CheckGoogleAuthToken) {
            CheckGoogleAuthToken authRequest = (CheckGoogleAuthToken) authentication;
            if (authRequest.getCredentials() == null) {
                throw new BadGoogleAuthValueException("Null GoogleAuth key");
            }
            boolean ok = false;
            try {
                ok = ssoService.authenticateUsingGoogleAuth(authRequest.getName(), authRequest.getCredentials().toString());
            } catch (SsoDecryptException e) {
                throw new BadGoogleAuthValueException("Can't decrypt master key for Google Auth", e);
            }
            if (!ok) {
                throw new BadGoogleAuthValueException("Invalid GoogleAuth key");
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
