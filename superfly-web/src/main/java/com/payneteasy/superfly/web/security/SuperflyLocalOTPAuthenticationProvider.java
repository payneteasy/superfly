package com.payneteasy.superfly.web.security;

import com.payneteasy.superfly.security.exception.BadOTPValueException;
import com.payneteasy.superfly.service.LocalSecurityService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * Authentication provider which is used to authenticate a local user using
 * HOTP.
 *
 * @author Roman Puchkovskiy
 */
@Setter
@RequiredArgsConstructor
public class SuperflyLocalOTPAuthenticationProvider implements AuthenticationProvider {

    private final LocalSecurityService localSecurityService;
    private       Class<?>             supportedAuthenticationClass = LocalCheckOTPToken.class;

    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {
        String  username = authentication.getName();
        String  otp      = (String) authentication.getCredentials();
        boolean ok       = localSecurityService.authenticateUsingOTP(username, otp);
        if (!ok) {
            throw new BadOTPValueException("Did not find a user with matching password");
        }
        return new FullAuthentication(username, authentication.getAuthorities());
    }

    public boolean supports(Class<?> authentication) {
        return supportedAuthenticationClass.isAssignableFrom(authentication);
    }

}
