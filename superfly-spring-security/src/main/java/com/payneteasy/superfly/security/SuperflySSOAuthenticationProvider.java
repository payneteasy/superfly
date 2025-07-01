package com.payneteasy.superfly.security;

import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.api.request.ExchangeSubsystemTokenRequest;
import com.payneteasy.superfly.security.authentication.SSOAuthenticationRequest;
import com.payneteasy.superfly.security.authentication.UsernamePasswordCheckedToken;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

/**
 * {@link AuthenticationProvider} which uses Superfly Single Sign-on
 * authentication to authenticate user.
 *
 * @author Roman Puchkovskiy
 */
@Setter
public class SuperflySSOAuthenticationProvider implements AuthenticationProvider {

    private SSOService ssoService;

    public SuperflySSOAuthenticationProvider() {
    }

    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {
        if (authentication instanceof SSOAuthenticationRequest authRequest) {
            if (authRequest.getSubsystemToken() == null) {
                throw new BadCredentialsException("Null subsystem token");
            }
            SSOUser ssoUser = ssoService.exchangeSubsystemToken(
                    new ExchangeSubsystemTokenRequest(
                            authRequest.getSubsystemToken()
                    )
            );
            if (ssoUser == null) {
                throw new BadCredentialsException("Bad credentials");
            }
            if (ssoUser.getActionsMap().isEmpty()) {
                throw new BadCredentialsException("No roles");
            }
            return createAuthentication(ssoUser);
        }
        return null;
    }

    public boolean supports(Class<?> authentication) {
        return SSOAuthenticationRequest.class.isAssignableFrom(authentication);
    }

    protected Authentication createAuthentication(SSOUser ssoUser) {
        // TODO: maybe we should separate them to make
        // user+password and SSO usable in the same chain
        return new UsernamePasswordCheckedToken(ssoUser);
    }

}
