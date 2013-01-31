package com.payneteasy.superfly.security;

import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.security.authentication.SSOAuthenticationRequest;
import com.payneteasy.superfly.security.authentication.UsernamePasswordCheckedToken;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * {@link AuthenticationProvider} which uses Superfly Single Sign-on
 * authentication to authenticate user.
 *
 * @author Roman Puchkovskiy
 */
public class SuperflySSOAuthenticationProvider implements AuthenticationProvider {
	
	private SSOService ssoService;

	@Required
	public void setSsoService(SSOService ssoService) {
		this.ssoService = ssoService;
	}

	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		if (authentication instanceof SSOAuthenticationRequest) {
            SSOAuthenticationRequest authRequest = (SSOAuthenticationRequest) authentication;
			if (authRequest.getSubsystemToken() == null) {
				throw new BadCredentialsException("Null subsystem token");
			}
			SSOUser ssoUser = ssoService.exchangeSubsystemToken(authRequest.getSubsystemToken());
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
