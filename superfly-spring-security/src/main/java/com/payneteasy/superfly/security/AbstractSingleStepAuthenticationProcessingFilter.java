package com.payneteasy.superfly.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationProcessingFilter;

import com.payneteasy.superfly.security.exception.PreconditionsException;

/**
 * Base for {@link AuthenticationProcessingFilter} implementations for a single
 * step of a multi-step authentication.
 *
 * @author Roman Puchkovskiy
 */
public abstract class AbstractSingleStepAuthenticationProcessingFilter extends AbstractAuthenticationProcessingFilter {
	
	private Class<?>[] requiredExistingAuthenticationClasses = null;
	
	protected AbstractSingleStepAuthenticationProcessingFilter(
			String defaultFilterProcessesUrl) {
		super(defaultFilterProcessesUrl);
	}
	
	public void setRequiredExistingAuthenticationClasses(Class<?>[] classes) {
		requiredExistingAuthenticationClasses = classes;
	}

	@Override
	public final Authentication attemptAuthentication(HttpServletRequest request,
			HttpServletResponse response) throws AuthenticationException,
			IOException, ServletException {
		checkBefore(request, response);
		return doAttemptAuthentication(request, response);
	}
	
	protected void checkBefore(HttpServletRequest request,
			HttpServletResponse response) throws AuthenticationException,
			IOException, ServletException {
		checkRequiredExistingAuthentication(SecurityContextHolder.getContext().getAuthentication());
	}
	
	protected void checkRequiredExistingAuthentication(Authentication authentication)
			throws AuthenticationException {
		if (requiredExistingAuthenticationClasses != null) {
			boolean willThrow = false;
			if (authentication == null) {
				willThrow = true;
			}
			if (!willThrow) {
				boolean found = false;
				for (Class<?> clazz : requiredExistingAuthenticationClasses) {
					if (authentication.getClass() == clazz) {
						found = true;
						break;
					}
				}
				willThrow = !found;
			}
			if (willThrow) {
				throw new PreconditionsException("Unexpected authentication class before authentication");
			}
		}
	}

	protected abstract Authentication doAttemptAuthentication(HttpServletRequest request,
			HttpServletResponse response) throws AuthenticationException,
			IOException, ServletException;

}
