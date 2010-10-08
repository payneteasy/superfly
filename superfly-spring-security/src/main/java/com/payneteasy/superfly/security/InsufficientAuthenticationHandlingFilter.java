package com.payneteasy.superfly.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

/**
 * Filter which intercepts non-complete authentications and throws an
 * {@link InsufficientAuthenticationException}.
 *
 * @author Roman Puchkovskiy
 */
public class InsufficientAuthenticationHandlingFilter extends GenericFilterBean {
	
	private Class<?>[] insufficientAuthenticationClasses = new Class<?>[]{};
	
	public void setInsufficientAuthenticationClasses(Class<?>[] classes) {
		insufficientAuthenticationClasses = classes;
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		Authentication authentication = obtainAuthentication();
		if (authentication != null) {
			boolean insufficient = false;
			for (Class<?> clazz : insufficientAuthenticationClasses) {
				if (clazz.isAssignableFrom(authentication.getClass())) {
					insufficient = true;
					break;
				}
			}
			if (insufficient) {
				InsufficientAuthenticationException ex = new InsufficientAuthenticationException(authentication.getClass().getName());
				ex.setAuthentication(authentication);
				throw ex;
			}
		}
		// sufficient authentication or no authenticatio at all, just proceed
		chain.doFilter(request, response);
	}

	protected Authentication obtainAuthentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}

}
