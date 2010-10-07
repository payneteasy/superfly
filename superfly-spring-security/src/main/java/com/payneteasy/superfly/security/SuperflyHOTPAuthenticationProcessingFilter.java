package com.payneteasy.superfly.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.security.authentication.CheckHOTPToken;
import com.payneteasy.superfly.security.authentication.SSOUserTransportAuthenticationToken;

/**
 * Filter which processes an HOTP authentication. It assumes that user has
 * already been authenticated by password authentication.
 * 
 * @author Roman Puchkovskiy
 */
public class SuperflyHOTPAuthenticationProcessingFilter extends
		AbstractAuthenticationProcessingFilter {
	
	public SuperflyHOTPAuthenticationProcessingFilter() {
		super("/j_superfly_hotp_security_check");
	}
	
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request,
			HttpServletResponse response) throws AuthenticationException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Assert.assertNotNull("Must have an authentication here", authentication);
		Assert.assertTrue(authentication instanceof SSOUserTransportAuthenticationToken);
		SSOUserTransportAuthenticationToken token = (SSOUserTransportAuthenticationToken) authentication;
		
		String hotp = obtainHotp(request);
		
		Authentication authRequest = createCheckHotpAuthRequest(hotp, token.getSsoUser());
		
		return getAuthenticationManager().authenticate(authRequest);
	}

	protected Authentication createCheckHotpAuthRequest(String hotp, SSOUser ssoUser) {
		return new CheckHOTPToken(ssoUser, hotp);
	}
	
	protected String obtainHotp(HttpServletRequest request) {
		return request.getParameter("j_hotp");
	}
}
