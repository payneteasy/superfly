package com.payneteasy.superfly.security;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.access.intercept.RunAsUserToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.security.authentication.CheckHOTPToken;
import com.payneteasy.superfly.security.authentication.HOTPCheckedToken;
import com.payneteasy.superfly.security.authentication.SSOUserTransportAuthenticationToken;

/**
 * {@link AuthenticationProvider} which authenticates using the Superfly HOTP.
 * 
 * @author Roman Puchkovskiy
 */
public class SuperflyHOTPAuthenticationProvider implements
		AuthenticationProvider {

	private SSOService ssoService;
	private Class<? extends Object> supportedAuthenticationClass = CheckHOTPToken.class;

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
		if (authentication instanceof SSOUserTransportAuthenticationToken) {
			SSOUserTransportAuthenticationToken authRequest = (SSOUserTransportAuthenticationToken) authentication;
			if (authRequest.getCredentials() == null) {
				throw new BadCredentialsException("Null HOTP value");
			}
			boolean ok = ssoService.authenticateUsingHOTP(authRequest.getName(), authRequest.getCredentials().toString());
			if (!ok) {
				throw new BadCredentialsException("Invalid HOTP value");
			}
			result = new HOTPCheckedToken(authRequest.getSsoUser());
		}
		return result;
	}

	public boolean supports(Class<? extends Object> authentication) {
		return supportedAuthenticationClass.isAssignableFrom(authentication);
	}

}
