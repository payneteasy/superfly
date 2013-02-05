package com.payneteasy.superfly.client.session;

import com.payneteasy.superfly.client.SessionToucher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Base for a filter that can be used for touching session.
 *
 * @author Roman Puchkovskiy
 */
public abstract class AbstractSessionTouchingFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(AbstractSessionTouchingFilter.class);

    private SessionToucher sessionToucher;

    @Override
    public final void init(FilterConfig filterConfig) throws ServletException {
        sessionToucher = createSessionToucher(filterConfig);
    }

    @Override
    public final void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        try {
            if (request instanceof HttpServletRequest) {
                Long id = getSuperflySessionId((HttpServletRequest) request);
                if (id != null) {
                    sessionToucher.addSessionId(id);
                }
            }
        } catch (Exception e) {
            logger.error("Error while adding session ID to toucher", e);
        } finally {
            chain.doFilter(request, response);
        }
    }

    protected abstract SessionToucher createSessionToucher(FilterConfig filterConfig);

    protected abstract Long getSuperflySessionId(HttpServletRequest request);

    @Override
    public void destroy() {
    }
}
