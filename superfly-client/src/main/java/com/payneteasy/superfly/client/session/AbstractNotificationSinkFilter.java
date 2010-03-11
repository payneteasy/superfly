package com.payneteasy.superfly.client.session;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * Base for filters that accept notifications from a Superfly server.
 * 
 * @author Roman Puchkovskiy
 */
public abstract class AbstractNotificationSinkFilter implements Filter {

	public AbstractNotificationSinkFilter() {
		super();
	}

	protected abstract boolean doFilterRequest(HttpServletRequest request);

	protected abstract boolean acceptsNotificationType(String notificationType);

	public void init(FilterConfig filterConfig) throws ServletException {
	}

	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		if ("POST".equals(request.getMethod())) {
			String notificationType = request.getParameter(getSuperflyNotificationParameterName());
			if (notificationType != null && acceptsNotificationType(notificationType)) {
				if (doFilterRequest(request)) {
					return;
				}
			}
		}
		chain.doFilter(req, resp);
	}

	protected String getSuperflyNotificationParameterName() {
		return "superflyNotification";
	}

	public void destroy() {
	}

}