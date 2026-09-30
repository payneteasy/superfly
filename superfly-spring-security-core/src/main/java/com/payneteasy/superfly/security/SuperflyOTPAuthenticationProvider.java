package com.payneteasy.superfly.security;

import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.api.exceptions.SsoDecryptException;
import com.payneteasy.superfly.api.request.CheckOtpRequest;
import com.payneteasy.superfly.security.authentication.CheckOTPToken;
import com.payneteasy.superfly.security.authentication.OTPCheckedToken;
import com.payneteasy.superfly.security.exception.BadOTPValueException;
import lombok.Setter;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * {@link AuthenticationProvider} which authenticates using the Superfly HOTP.
 *
 * @author Igor Vasilyev
 */
public class SuperflyOTPAuthenticationProvider implements AuthenticationProvider {

    @Setter
    private SSOService ssoService;
    private Class<?>   supportedAuthenticationClass = CheckOTPToken.class;

    public void setSupportedAuthenticationClass(Class<? extends Authentication> clazz) {
        this.supportedAuthenticationClass = clazz;
    }

    public SuperflyOTPAuthenticationProvider() {
    }

    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {
        Authentication result = null;
        if (authentication instanceof CheckOTPToken authRequest) {
            if (authRequest.getCredentials() == null) {
                throw new BadOTPValueException("Null OTP secret");
            }
            boolean ok;
            try {
                ok = ssoService.checkOtp(
                        new CheckOtpRequest(
                                authRequest.getSsoUser(),
                                authRequest.getCredentials().toString()
                        )
                );
            } catch (SsoDecryptException e) {
                throw new BadOTPValueException(e.getMessage());
            }
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
