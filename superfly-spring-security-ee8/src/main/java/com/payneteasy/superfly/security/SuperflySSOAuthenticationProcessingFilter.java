package com.payneteasy.superfly.security;

import com.payneteasy.superfly.api.client.SSOLoginState;
import com.payneteasy.superfly.security.authentication.CompoundAuthentication;
import com.payneteasy.superfly.security.authentication.SSOAuthenticationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Filter which authenticates a user using the Superfly Single Sign-on
 * authentication.
 * <p>
 * The subsystem token is accepted only together with the one-time {@code state}
 * stored in the session by {@link SuperflySSOLoginUrlAuthenticationEntryPoint}.
 *
 * @author Roman Puchkovskiy
 */
public class SuperflySSOAuthenticationProcessingFilter extends
        AbstractSingleStepAuthenticationProcessingFilter {

    private static final Logger logger = LoggerFactory.getLogger(SuperflySSOAuthenticationProcessingFilter.class);

    private String subsystemTokenParameter = "subsystemToken";
    private String targetUrlParameter = "targetUrl";

    public void setSubsystemTokenParameter(String subsystemTokenParameter) {
        this.subsystemTokenParameter = subsystemTokenParameter;
    }

    public void setTargetUrlParameter(String targetUrlParameter) {
        this.targetUrlParameter = targetUrlParameter;
    }

    public SuperflySSOAuthenticationProcessingFilter() {
        super("/j_superfly_sso_security_check");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {
        Authentication authRequest;

        if (!consumeLoginState(request)) {
            logger.warn("SSO login state is missing or does not match the session, the subsystem token is not checked");
            throw new BadCredentialsException("SSO login state mismatch");
        }

        String subsystemToken = obtainSubsystemToken(request);
        String targetUrl = obtainTargetUrl(request);

        authRequest = createSSOAuthRequest(request, subsystemToken);

        return getAuthenticationManager().authenticate(new CompoundAuthentication(authRequest));
    }

    /** The state is one-time: it is removed from the session whatever the outcome. */
    private static boolean consumeLoginState(HttpServletRequest request) {
        String actual = request.getParameter(SSOLoginState.PARAMETER);
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        Object expected = session.getAttribute(SSOLoginState.SESSION_ATTRIBUTE);
        session.removeAttribute(SSOLoginState.SESSION_ATTRIBUTE);
        return expected instanceof String && SSOLoginState.matches((String) expected, actual);
    }

    protected String obtainSubsystemToken(HttpServletRequest request) {
        return request.getParameter(subsystemTokenParameter);
    }

    protected String obtainTargetUrl(HttpServletRequest request) {
        return request.getParameter(targetUrlParameter);
    }

    protected Authentication createSSOAuthRequest(
            HttpServletRequest request, String subsystemToken) {
        return new SSOAuthenticationRequest(subsystemToken);
    }

}

