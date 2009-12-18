package com.payneteasy.superfly.security.authentication;

import org.springframework.security.providers.UsernamePasswordAuthenticationToken;

import com.payneteasy.superfly.api.AuthenticationRequestInfo;

/**
 * Extension of UsernamePasswordAuthenticationToken which also allows to
 * transfer AuthenticationRequestInfo instance.
 * 
 * @author Roman Puchkovskiy
 */
public class UsernamePasswordAuthRequestInfoAuthenticationToken extends
		UsernamePasswordAuthenticationToken {
	
	private AuthenticationRequestInfo authRequestInfo;

	public UsernamePasswordAuthRequestInfoAuthenticationToken(Object principal,
			Object credentials, AuthenticationRequestInfo authRequestInfo) {
		super(principal, credentials);
		this.authRequestInfo = authRequestInfo;
	}
	
	public AuthenticationRequestInfo getAuthRequestInfo() {
		return authRequestInfo;
	}

}
