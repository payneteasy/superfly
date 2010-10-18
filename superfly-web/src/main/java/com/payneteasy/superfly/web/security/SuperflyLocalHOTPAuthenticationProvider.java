package com.payneteasy.superfly.web.security;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.payneteasy.superfly.service.LocalSecurityService;

/**
 * Authentication provider which is used to authenticate a local user using
 * HOTP.
 * 
 * @author Roman Puchkovskiy
 */
public class SuperflyLocalHOTPAuthenticationProvider implements
		AuthenticationProvider {
	
	private LocalSecurityService localSecurityService;

	@Required
	public void setLocalSecurityService(LocalSecurityService localSecurityService) {
		this.localSecurityService = localSecurityService;
	}

	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		String username = authentication.getName();
		String hotp = (String) authentication.getCredentials();
		boolean ok = localSecurityService.authenticateUsingHOTP(username, hotp);
		if (!ok) {
			throw new BadCredentialsException("Did not find a user with matching password");
		}
		return new FullAuthentication(username, authentication.getAuthorities());
	}

	public boolean supports(Class<? extends Object> authentication) {
		return LocalCheckHOTPToken.class.isAssignableFrom(authentication);
	}

}
