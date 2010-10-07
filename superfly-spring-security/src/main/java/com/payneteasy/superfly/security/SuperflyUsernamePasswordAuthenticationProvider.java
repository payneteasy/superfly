package com.payneteasy.superfly.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.security.authentication.UsernamePasswordAuthRequestInfoAuthenticationToken;
import com.payneteasy.superfly.security.authentication.UsernamePasswordCheckedToken;

/**
 * {@link AuthenticationProvider} which uses Superfly password authentication
 * to authenticate user.
 * 
 * @author Roman Puchkovskiy
 */
public class SuperflyUsernamePasswordAuthenticationProvider extends AbstractSuperflySingleStepAuthenticationProvider {

	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		if (authentication instanceof UsernamePasswordAuthRequestInfoAuthenticationToken) {
			UsernamePasswordAuthRequestInfoAuthenticationToken authRequest = (UsernamePasswordAuthRequestInfoAuthenticationToken) authentication;
			if (authRequest.getCredentials() == null) {
				throw new BadCredentialsException("Null credentials");
			}
			SSOUser ssoUser = ssoService.authenticate(authRequest.getName(),
					authRequest.getCredentials().toString(),
					authRequest.getAuthRequestInfo());
			if (ssoUser == null) {
				throw new BadCredentialsException("Bad credentials");
			}
			if (ssoUser.getActionsMap().isEmpty()) {
				throw new BadCredentialsException("No roles");
			}
			return createAuthentication(authRequest, ssoUser);
		}
		return null;
	}

	public boolean supports(Class<? extends Object> authentication) {
		return UsernamePasswordAuthRequestInfoAuthenticationToken.class.isAssignableFrom(authentication);
	}

	@Override
	protected Authentication createNonFinalAuthentication(Authentication auth,
			SSOUser ssoUser) {
		return new UsernamePasswordCheckedToken(ssoUser);
	}

}
