package com.payneteasy.superfly.web.security.ratelimit;

import com.payneteasy.superfly.security.authentication.CompoundAuthentication;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Throttles the admin/SSO login steps (password, OTP, OTP initialization) by client IP and IP + username.
 * Must be placed before the login processing filters. A blocked request gets 429 and never reaches
 * the authentication manager, so it does not touch the DB and does not increase {@code logins_failed}.
 * <p>
 * A failure is reported by {@link #recordingFailureHandler}, which every login filter has to use.
 * Remote-auth ({@code /sso/check/*}, has its own limit) and subsystem RPC ({@code /remoting/*}: the caller is
 * a trusted subsystem and its address is not the end user's) are intentionally not covered.
 */
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final String FAILED_ATTRIBUTE = LoginRateLimitFilter.class.getName() + ".FAILED";

    private final LoginAttemptLimiter                limiter;
    private final Map<AntPathRequestMatcher, String> steps = new LinkedHashMap<>();

    public LoginRateLimitFilter(LoginAttemptLimiter limiter) {
        this.limiter = limiter;
        // Same matchers as the login processing filters use (servlet path, any method, no ;params).
        steps.put(new AntPathRequestMatcher("/j_superfly_password_security_check"), "password");
        steps.put(new AntPathRequestMatcher("/j_superfly_otp_security_check"), "otp");
        steps.put(new AntPathRequestMatcher("/j_superfly_otp_reset"), "otp-reset");
    }

    /** Wraps a failure handler so that the filter learns about a rejected attempt. */
    public static AuthenticationFailureHandler recordingFailureHandler(AuthenticationFailureHandler delegate) {
        return (request, response, exception) -> {
            // A DB outage is not a wrong credential: it must not lock users out.
            if (!(exception instanceof AuthenticationServiceException)) {
                request.setAttribute(FAILED_ATTRIBUTE, Boolean.TRUE);
            }
            delegate.onAuthenticationFailure(request, response, exception);
        };
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String step = stepOf(request);
        if (step == null) {
            chain.doFilter(request, response);
            return;
        }
        String ip       = request.getRemoteAddr();
        String username = LoginAttemptLimiter.normalizeUsername(usernameOf(request, step));

        if (limiter.checkBlocked(step, ip, username)) {
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(LoginAttemptLimiter.windowSeconds()));
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Too many failed login attempts. Try again later.");
            return;
        }

        chain.doFilter(request, response);

        if (request.getAttribute(FAILED_ATTRIBUTE) != null) {
            limiter.recordFailure(step, ip, username);
        } else {
            limiter.recordSuccess(step, ip, username);
        }
    }

    private String stepOf(HttpServletRequest request) {
        for (Map.Entry<AntPathRequestMatcher, String> entry : steps.entrySet()) {
            if (entry.getKey().matches(request)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /** The password step carries the username; later steps take it from the partially authenticated context. */
    private static String usernameOf(HttpServletRequest request, String step) {
        if ("password".equals(step)) {
            return request.getParameter("j_username");
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof CompoundAuthentication compound) {
            authentication = compound.getFirstReadyAuthentication();
        }
        return authentication == null ? null : authentication.getName();
    }
}
