package com.payneteasy.superfly.security;

import java.io.IOException;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationException;
import org.springframework.security.BadCredentialsException;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.ui.webapp.AuthenticationProcessingFilter;

import com.payneteasy.superfly.api.SSORole;
import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.api.SSOUser;

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

	private String loginFormStepTwoUrl;
    private String roleParameter = SPRING_SECURITY_FORM_ROLE_KEY;
	private SSOService ssoService;

	public String getLoginFormStepTwoUrl() {
		return loginFormStepTwoUrl;
	}

	public void setLoginFormStepTwoUrl(String loginFormStepTwoUrl) {
		this.loginFormStepTwoUrl = loginFormStepTwoUrl;
	}

	public void setRoleParameter(String roleParameter) {
		this.roleParameter = roleParameter;
	}

	@Required
	public void setSsoService(SSOService ssoService) {
		this.ssoService = ssoService;
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
		
		if (password == null) {
			throw new BadCredentialsException("Null password");
		}
		
		SSOUser ssoUser = ssoService.authenticate(username, password);
		if (ssoUser == null) {
			throw new BadCredentialsException("Bad password");
		}
		
		if (ssoUser.getActionsMap().isEmpty()) {
			throw new BadCredentialsException("No roles assigned");
		}
		
		if (ssoUser.getActionsMap().keySet().size() > 1) {
			// we have to trigger step 2 because user is to select role
			Authentication token = new SSOUserTransportAuthenticationToken(ssoUser);
			SecurityContextHolder.getContext().setAuthentication(token);
			throw new StepTwoException("Going to step two");
		} else {
			// exactly one role - that's all, short-circuit
			return selectRole(ssoUser, ssoUser.getActionsMap().keySet().iterator().next());
		}
	}
	
	protected Authentication doStepTwo(HttpServletRequest request,
			Authentication authentication) {
		String roleKey = obtainRoleKey(request);
		SSOUserTransportAuthenticationToken token = (SSOUserTransportAuthenticationToken) authentication;
		SSOUser ssoUser = token.getSsoUser();
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
		
		return selectRole(ssoUser, selectedRole);
	}
	
	protected Authentication selectRole(SSOUser ssoUser, SSORole role) {
		return new SSOUserAndSelectedRoleAuthenticationToken(ssoUser, role);
	}
	
    @Override
	protected void unsuccessfulAuthentication(HttpServletRequest request,
			HttpServletResponse response, AuthenticationException failed)
			throws IOException, ServletException {
    	if (failed instanceof StepTwoException) {
			// triggering step two, but before this we must probably put
			// something to request or session
			prepareForStepTwo(request);
    		doForwardToStepTwo(request, response);
    	} else {
    		super.unsuccessfulAuthentication(request, response, failed);
    	}
	}

	protected void doForwardToStepTwo(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		RequestDispatcher rd = request.getRequestDispatcher(determineUrlForStepTwo());
		rd.forward(request, response);
	}

	protected String determineUrlForStepTwo() {
		return getLoginFormStepTwoUrl();
	}

	protected void prepareForStepTwo(HttpServletRequest request) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		SSOUserTransportAuthenticationToken token = (SSOUserTransportAuthenticationToken) authentication;
		Set<SSORole> roles = token.getSsoUser().getActionsMap().keySet();
		request.setAttribute("superflyRoles", roles);
		request.setAttribute("ctxPath", request.getContextPath());
	}

}
