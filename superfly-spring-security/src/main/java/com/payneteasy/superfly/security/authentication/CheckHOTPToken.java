package com.payneteasy.superfly.security.authentication;

import org.springframework.security.core.Authentication;

import com.payneteasy.superfly.api.SSOUser;

/**
 * {@link Authentication} which is a request to check an HOTP.
 * 
 * @author Roman Puchkovskiy
 */
public class CheckHOTPToken extends SSOUserTransportAuthenticationToken {
	private static final long serialVersionUID = 2290145086843797962L;
	
	private String hotp;

	public CheckHOTPToken(SSOUser ssoUser, String hotp) {
		super(ssoUser);
		this.hotp = hotp;
	}

	@Override
	public Object getCredentials() {
		return hotp;
	}

}
