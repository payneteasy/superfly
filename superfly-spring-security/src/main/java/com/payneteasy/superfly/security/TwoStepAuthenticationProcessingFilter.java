package com.payneteasy.superfly.security;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationException;
import org.springframework.security.BadCredentialsException;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.ui.webapp.AuthenticationProcessingFilter;

import com.payneteasy.superfly.api.AuthenticationRequestInfo;
import com.payneteasy.superfly.api.SSORole;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.security.authentication.SSOUserAndSelectedRoleAuthenticationToken;
import com.payneteasy.superfly.security.authentication.SSOUserTransportAuthenticationToken;
import com.payneteasy.superfly.security.authentication.UsernamePasswordAuthRequestInfoAuthenticationToken;

/**
 * Processing filter which carries out a two-step authentication process.
 * On the first step, username+password are requested from user to authenticate;
 * on success roles are obtained for the user. If there're more than 1 roles,
 * user is forwarded to step 2, on which he selects a role.
 * If there's only one role, it's selected and step 2 is ignored.
 * 
 * @author Roman Puchkovskiy
 */
public class TwoStepAuthenticationProcessingFilter extends AuthenticationProcessingFilter {
	
	public static final String SPRING_SECURITY_FORM_ROLE_KEY = "j_role";

    private String roleParameter = SPRING_SECURITY_FORM_ROLE_KEY;
    private String subsystemIdentifier = null;
	
	public void setRoleParameter(String roleParameter) {
		this.roleParameter = roleParameter;
	}

	public void setSubsystemIdentifier(String subsystemIdentifier) {
		this.subsystemIdentifier = subsystemIdentifier;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request)
			throws AuthenticationException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Authentication authRequest;
		if (isStepOne(request, authentication)) {
			authRequest = doStepOne(request, authentication);
		} else if (isStepTwo(request, authentication)) {
			authRequest = doStepTwo(request, authentication);
		} else {
			throw new IllegalStateException("Must execute either step 1 or step 2, but they both didn't match");
		}
		
		return getAuthenticationManager().authenticate(authRequest);
	}
	
	protected String obtainRoleKey(HttpServletRequest request) {
		return request.getParameter(roleParameter);
	}

	protected boolean isStepOne(HttpServletRequest request,
			Authentication authentication) {
		return obtainUsername(request) != null;
	}
	
	protected boolean isStepTwo(HttpServletRequest request,
			Authentication authentication) {
		return obtainRoleKey(request) != null;
	}

	@Override
	public String getDefaultFilterProcessesUrl() {
		return "/j_superfly_security_check";
	}

	protected Authentication doStepOne(HttpServletRequest request,
			Authentication authentication) {
		String username = obtainUsername(request);
		String password = obtainPassword(request);
		
		return createUsernamePasswordAuthRequest(request, username, password);
	}

	protected Authentication createUsernamePasswordAuthRequest(
			HttpServletRequest request, String username, String password) {
		AuthenticationRequestInfo authRequestInfo = createAuthRequestInfo(request);
		Authentication authRequest = new UsernamePasswordAuthRequestInfoAuthenticationToken(
				username, password, authRequestInfo);
		return authRequest;
	}
	
	protected Authentication doStepTwo(HttpServletRequest request,
			Authentication authentication) {
		String roleKey = obtainRoleKey(request);
		SSOUser ssoUser = (SSOUser) request.getSession().getAttribute(
				SSOUserTransportAuthenticationToken.SESSION_KEY);
		request.getSession().removeAttribute(
				SSOUserTransportAuthenticationToken.SESSION_KEY);
		if (ssoUser == null) {
			throw new BadCredentialsException("Session expired");
		}
		SSORole selectedRole = null;
		for (SSORole role : ssoUser.getActionsMap().keySet()) {
			if (role.getName().equals(roleKey)) {
				selectedRole = role;
				break;
			}
		}
		if (selectedRole == null) {
			throw new BadCredentialsException("Unknown role: " + roleKey);
		}
		
		return createUserRoleAuthRequest(ssoUser, selectedRole);
	}
	
	protected Authentication createUserRoleAuthRequest(SSOUser ssoUser, SSORole role) {
		return new SSOUserAndSelectedRoleAuthenticationToken(ssoUser, role);
	}

	protected AuthenticationRequestInfo createAuthRequestInfo(HttpServletRequest request) {
		AuthenticationRequestInfo result = new AuthenticationRequestInfo();
		result.setIpAddress(request.getRemoteAddr());
		result.setSubsystemIdentifier(subsystemIdentifier);
		return result;
	}

}
