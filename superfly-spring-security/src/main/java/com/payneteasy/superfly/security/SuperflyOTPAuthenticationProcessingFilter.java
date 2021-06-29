package com.payneteasy.superfly.security;

import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.security.authentication.CheckOTPToken;
import com.payneteasy.superfly.security.authentication.CompoundAuthentication;
import com.payneteasy.superfly.security.authentication.SSOUserTransportAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Filter which processes an Google Auth TOTP authentication. It assumes that user has
 * already been authenticated by password authentication.
 *
 * @author Igor Vasilyev
 */
public class SuperflyOTPAuthenticationProcessingFilter extends
        AbstractSingleStepAuthenticationProcessingFilter {

    private static final Logger logger = LoggerFactory.getLogger(SuperflyOTPAuthenticationProcessingFilter.class);

    private String otpParameter = "j_otp";

    public SuperflyOTPAuthenticationProcessingFilter() {
        super("/j_superfly_otp_security_check");
    }

    public void setOtpParameter(String otpParameter) {
        this.otpParameter = otpParameter;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new BadCredentialsException("Authentication is null");
        }
        Assert.notNull(authentication, "authentication cannot be null");

        CompoundAuthentication compound = getCompoundAuthenticationOrNewOne(authentication);
        authentication = extractLatestAuthOrSimpleAuth(authentication);

        String otp = obtainOtp(request);

        Authentication authRequest = createSimpleAuthRequest(authentication, otp);

        return getAuthenticationManager().authenticate(new CompoundAuthentication(compound.getReadyAuthentications(), authRequest));
    }

    protected Authentication createSimpleAuthRequest(
            Authentication authentication, String otp) {
        if (!(authentication instanceof SSOUserTransportAuthenticationToken)) {
            String msg = "Unexpected authentication of class " + authentication.getClass() + ": " + authentication;
            logger.error(msg);
            throw new AuthenticationServiceException(msg);
        }
        SSOUserTransportAuthenticationToken token = (SSOUserTransportAuthenticationToken) authentication;
        return createCheckOtpAuthRequest(otp, token.getSsoUser());
    }

    protected Authentication createCheckOtpAuthRequest(String otp, SSOUser ssoUser) {
        return new CheckOTPToken(ssoUser, otp);
    }

    protected String obtainOtp(HttpServletRequest request) {
        return request.getParameter(otpParameter);
    }
}
