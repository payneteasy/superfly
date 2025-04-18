package com.payneteasy.superfly.web.security;

import com.payneteasy.superfly.api.exceptions.SsoDecryptException;
import com.payneteasy.superfly.model.ui.user.OtpUserDescription;
import com.payneteasy.superfly.security.AbstractSingleStepAuthenticationProcessingFilter;
import com.payneteasy.superfly.security.authentication.CompoundAuthentication;
import com.payneteasy.superfly.security.csrf.CsrfValidator;
import com.payneteasy.superfly.service.LocalSecurityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Filter to init OTP setting for user
 *
 * @author Igor Vasilyev
 */
@Setter
public class SuperflyInitOTPAuthenticationProcessingFilter extends AbstractSingleStepAuthenticationProcessingFilter {

    private static final Logger logger = LoggerFactory.getLogger(SuperflyInitOTPAuthenticationProcessingFilter.class);

    private LocalSecurityService localSecurityService;
    private CsrfValidator        csrfValidator;
    private String               otpKeyParameter = "j_key";

    public SuperflyInitOTPAuthenticationProcessingFilter() {
        super("/j_superfly_otp_reset");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {
        csrfValidator.validateToken(request);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new BadCredentialsException("Not authorized");
        }

        CompoundAuthentication result            = getCompoundAuthenticationOrNewOne(authentication);
        LocalNeedOTPToken      localNeedOTPToken = (LocalNeedOTPToken) extractLatestAuthOrSimpleAuth(authentication);

        if (localNeedOTPToken != null) {
            String otpKey   = obtainOtpKey(request);
            String username = localNeedOTPToken.getName();
            try {
                localSecurityService.persistOtpKey(localNeedOTPToken.getOtpType(), username, otpKey);
            } catch (SsoDecryptException e) {
                logger.error("Error when encrypt OTP master key", e);
            }
            OtpUserDescription userForDescription = localSecurityService.getOtpUserForDescription(username);
            if (userForDescription.isUserOtpRequiredAndInit()) {
                result = new CompoundAuthentication(result.getFirstReadyAuthentication());
            }
        }

        return getAuthenticationManager().authenticate(result);
    }

    protected String obtainOtpKey(HttpServletRequest request) {
        return request.getParameter(otpKeyParameter);
    }

}
