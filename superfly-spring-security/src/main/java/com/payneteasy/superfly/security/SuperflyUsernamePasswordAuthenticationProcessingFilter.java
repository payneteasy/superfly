package com.payneteasy.superfly.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.payneteasy.superfly.api.AuthenticationRequestInfo;
import com.payneteasy.superfly.security.authentication.UsernamePasswordAuthRequestInfoAuthenticationToken;

/**
 * Filter which authenticates a user using the Superfly password authentication.
 * 
 * @author Roman Puchkovskiy
 */
public class SuperflyUsernamePasswordAuthenticationProcessingFilter extends
		UsernamePasswordAuthenticationFilter {
	
	private String subsystemIdentifier;

	public SuperflyUsernamePasswordAuthenticationProcessingFilter() {
		super();
		setFilterProcessesUrl("/j_superfly_password_security_check");
	}
	
	public void setSubsystemIdentifier(String subsystemIdentifier) {
		this.subsystemIdentifier = subsystemIdentifier;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request,
			HttpServletResponse response) throws AuthenticationException {
		Authentication authRequest;
		
		String username = obtainUsername(request);
		String password = obtainPassword(request);
		
		authRequest = createUsernamePasswordAuthRequest(request, username, password);
		
		return getAuthenticationManager().authenticate(authRequest);
	}
	
	protected Authentication createUsernamePasswordAuthRequest(
			HttpServletRequest request, String username, String password) {
		AuthenticationRequestInfo authRequestInfo = createAuthRequestInfo(request);
		Authentication authRequest = new UsernamePasswordAuthRequestInfoAuthenticationToken(
				username, password, authRequestInfo);
		return authRequest;
	}
	
	protected AuthenticationRequestInfo createAuthRequestInfo(HttpServletRequest request) {
		AuthenticationRequestInfo result = new AuthenticationRequestInfo();
		result.setIpAddress(request.getRemoteAddr());
		result.setSubsystemIdentifier(subsystemIdentifier);
		return result;
	}

}
