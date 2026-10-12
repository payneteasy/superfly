package com.payneteasy.superfly.web.security;

import com.payneteasy.superfly.api.exceptions.SsoDecryptException;
import com.payneteasy.superfly.security.AbstractSingleStepAuthenticationProcessingFilter;
import com.payneteasy.superfly.security.authentication.CompoundAuthentication;
import com.payneteasy.superfly.security.csrf.CsrfValidator;
import com.payneteasy.superfly.security.exception.BadOTPValueException;
import com.payneteasy.superfly.service.LocalSecurityService;
import com.payneteasy.superfly.web.security.ratelimit.LoginRateLimitFilter;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import java.io.IOException;
import java.io.Serializable;

/**
 * Filter to init OTP setting for user: the TOTP key is issued by the setup page and kept in the session, the form
 * sends only a code from the authenticator app. The key is stored for the user once the code matches it, and the
 * same code completes the login.
 *
 * @author Igor Vasilyev
 */
@Setter
public class SuperflyInitOTPAuthenticationProcessingFilter extends AbstractSingleStepAuthenticationProcessingFilter {

    private static final Logger logger = LoggerFactory.getLogger(SuperflyInitOTPAuthenticationProcessingFilter.class);

    private static final String PENDING_KEY_ATTRIBUTE =
            SuperflyInitOTPAuthenticationProcessingFilter.class.getName() + ".PENDING_KEY";

    private LocalSecurityService         localSecurityService;
    private CsrfValidator                csrfValidator;
    private String                       otpParameter            = "j_otp";
    /** A code that does not match the key: the user stays on the setup page. */
    private AuthenticationFailureHandler setupCodeFailureHandler = LoginRateLimitFilter.recordingFailureHandler(
            new SimpleUrlAuthenticationFailureHandler("/login-setup"));

    public SuperflyInitOTPAuthenticationProcessingFilter() {
        super("/j_superfly_otp_reset");
    }

    /**
     * The key that the setup page shows to the user: issued on the first call and kept in the session until a code
     * confirms it, so a reload or a mistyped code does not require scanning a new QR code.
     */
    public static String pendingKey(HttpSession session, String username) {
        String key = issuedKey(session, username);
        if (key == null) {
            key = new GoogleAuthenticator().createCredentials().getKey();
            session.setAttribute(PENDING_KEY_ATTRIBUTE, new PendingKey(username, key));
        }
        return key;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {
        csrfValidator.validateToken(request);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new BadCredentialsException("Not authorized");
        }

        Authentication latest       = extractLatestAuthOrSimpleAuth(authentication);
        Authentication passwordStep = getCompoundAuthenticationOrNewOne(authentication).getFirstReadyAuthentication();
        if (!(latest instanceof LocalNeedOTPToken localNeedOTPToken)
                || !(passwordStep instanceof UsernamePasswordAuthenticationToken password)) {
            throw new BadCredentialsException("OTP initialization is not pending");
        }

        String username = localNeedOTPToken.getName();
        String key      = issuedKey(request.getSession(false), username);
        if (key == null) {
            throw new BadCredentialsException("No OTP key has been issued");
        }
        String code = obtainOtp(request);
        if (!isValidCode(key, code)) {
            throw new SetupCodeMismatchException();
        }
        try {
            localSecurityService.persistOtpKey(localNeedOTPToken.getOtpType(), username, key);
        } catch (SsoDecryptException e) {
            logger.error("Error when encrypt OTP master key", e);
            throw new AuthenticationServiceException("OTP key is not stored", e);
        }
        request.getSession().removeAttribute(PENDING_KEY_ATTRIBUTE);

        // The code step checks the code once more, now against the stored key, and records its time step as used:
        // the code cannot be accepted again at the next login.
        return getAuthenticationManager().authenticate(new CompoundAuthentication(new Authentication[]{password},
                new LocalCheckOTPToken(username, code, password.getAuthorities())));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException, ServletException {
        if (failed instanceof SetupCodeMismatchException) {
            // the partial login and the issued key are kept, so the user can enter another code
            setupCodeFailureHandler.onAuthenticationFailure(request, response, failed);
        } else {
            super.unsuccessfulAuthentication(request, response, failed);
        }
    }

    protected String obtainOtp(HttpServletRequest request) {
        return request.getParameter(otpParameter);
    }

    private static String issuedKey(HttpSession session, String username) {
        if (session != null && session.getAttribute(PENDING_KEY_ATTRIBUTE) instanceof PendingKey pending
                && pending.username().equals(username)) {
            return pending.key();
        }
        return null;
    }

    /** The same check as on the SSO setup page: six digits, ±1 time step. */
    private static boolean isValidCode(String key, String code) {
        if (code == null || !code.matches("^[0-9]{6}$")) {
            return false;
        }
        return new GoogleAuthenticator().authorize(key, Integer.parseInt(code));
    }

    private record PendingKey(String username, String key) implements Serializable {
    }

    private static final class SetupCodeMismatchException extends BadOTPValueException {
        SetupCodeMismatchException() {
            super("OTP value does not match the issued key");
        }
    }

}
