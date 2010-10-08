package com.payneteasy.superfly.security;

import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

/**
 * {@link AuthenticationEntryPoint} which is intended to be used for multi-step
 * authentication process. It redirects to URLs determined by the current
 * {@link Authentication} class.
 * 
 * @author Roman Puchkovskiy
 */
public class MultiStepLoginUrlAuthenticationEntryPoint extends
		LoginUrlAuthenticationEntryPoint implements AuthenticationEntryPoint {
	
	private Map<Class<? extends Authentication>, String> stepInsufficientAuthenticationMapping = Collections.emptyMap();
	
	public void setInsufficientAuthenticationMapping(
			Map<Class<? extends Authentication>,
			String> mapping) {
		this.stepInsufficientAuthenticationMapping = mapping;
	}

	@Override
	protected String determineUrlToUseForThisRequest(
			HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) {
		String url = null;
		Authentication auth = exception.getAuthentication();
		if (auth == null) {
			auth = SecurityContextHolder.getContext().getAuthentication();
		}
		if (auth != null) {
			url = stepInsufficientAuthenticationMapping.get(auth.getClass());
		}
		if (url == null) {
			url = getLoginFormUrl();
		}
		return url;
	}

}
