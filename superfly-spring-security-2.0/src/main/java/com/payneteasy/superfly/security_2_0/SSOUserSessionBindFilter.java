package com.payneteasy.superfly.security_2_0;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.ui.FilterChainOrder;
import org.springframework.security.ui.SpringSecurityFilter;

import com.payneteasy.superfly.common.session.SessionMapping;
import com.payneteasy.superfly.common.session.SessionMappingLocator;
import com.payneteasy.superfly.security_2_0.authentication.SSOUserAuthenticationToken;

/**
 * Filter which creates a correlation between a Superfly session key and an
 * HttpSession.
 * 
 * @author Roman Puchkovskiy
 */
public class SSOUserSessionBindFilter extends SpringSecurityFilter {

	@Override
	protected void doFilterHttp(HttpServletRequest request,
			HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpSession session = request.getSession(false);
		if (session != null) {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication != null && authentication instanceof SSOUserAuthenticationToken) {
				SSOUserAuthenticationToken token = (SSOUserAuthenticationToken) authentication;
				getSessionMapping().addSession(token.getUser().getSessionId(), session);
			}
		}
		chain.doFilter(request, response);
	}

	protected SessionMapping getSessionMapping() {
		return SessionMappingLocator.getSessionMapping();
	}

	public int getOrder() {
		return FilterChainOrder.AUTHENTICATION_PROCESSING_FILTER + 1;
	}

}
