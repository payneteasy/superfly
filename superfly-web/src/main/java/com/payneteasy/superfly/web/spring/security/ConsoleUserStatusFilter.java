package com.payneteasy.superfly.web.spring.security;

import com.payneteasy.superfly.model.ui.user.UIUserDetails;
import com.payneteasy.superfly.model.ui.user.UserForDescription;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.web.security.FullAuthentication;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Ends the admin console session of a user who has been locked, suspended or deleted after logging in: the
 * authentication kept in the session is not checked against the database otherwise. The status is read on
 * every request, so the session ends on whichever node the next request lands.
 */
class ConsoleUserStatusFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(ConsoleUserStatusFilter.class);

    private final UserService userService;
    private final LoggerSink  loggerSink;
    private final String      loginUrl;

    ConsoleUserStatusFilter(UserService userService, LoggerSink loggerSink, String loginUrl) {
        this.userService = userService;
        this.loggerSink = loggerSink;
        this.loginUrl = loginUrl;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String refusal = isConsoleLogin(authentication) ? refusal(authentication.getName()) : null;
        if (refusal == null) {
            chain.doFilter(request, response);
            return;
        }
        loggerSink.info(logger, "LOCAL_SESSION_CHECK", false, authentication.getName(), "status=" + refusal);
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();

        String location = request.getContextPath() + loginUrl;
        if ("true".equals(request.getHeader("Wicket-Ajax"))) {
            // XHR would follow a redirect by itself and hand the login page to Wicket as the ajax answer.
            // This is how Wicket itself redirects an ajax request: the page navigates to Ajax-Location.
            response.setHeader("Ajax-Location", location);
            response.setContentType("text/xml;charset=UTF-8");
            response.getWriter().write("<ajax-response><redirect><![CDATA[" + location + "]]></redirect></ajax-response>");
        } else {
            response.sendRedirect(location);
        }
    }

    /** The results of the console login form: the password step alone, or the password and the OTP steps. */
    private static boolean isConsoleLogin(Authentication authentication) {
        return authentication instanceof FullAuthentication
                || authentication instanceof UsernamePasswordAuthenticationToken && authentication.isAuthenticated();
    }

    /**
     * @return why the user may not keep the session, or null if the account is active
     */
    private String refusal(String username) {
        UserForDescription description = userService.getUserForDescription(username);
        UIUserDetails user = description == null ? null : userService.getUser(description.getUserId());
        if (user == null) {
            return "not_found";
        }
        if (user.isAccountSuspended()) {
            return "suspended";
        }
        if (user.isAccountLocked()) {
            return "locked";
        }
        return null;
    }
}
