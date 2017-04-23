package com.payneteasy.superfly.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;

import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.security.authentication.CheckHOTPToken;
import com.payneteasy.superfly.security.authentication.CompoundAuthentication;
import com.payneteasy.superfly.security.authentication.SSOUserTransportAuthenticationToken;

/**
 * Filter which processes an HOTP authentication. It assumes that user has
 * already been authenticated by password authentication.
 * 
 * @author Roman Puchkovskiy
 */
public class SuperflyHOTPAuthenticationProcessingFilter extends
        AbstractSingleStepAuthenticationProcessingFilter {

    private static final Logger logger = LoggerFactory.getLogger(SuperflyHOTPAuthenticationProcessingFilter.class);

    private String hotpParameter = "j_hotp";

    public SuperflyHOTPAuthenticationProcessingFilter() {
        super("/j_superfly_hotp_security_check");
    }

    public void setHotpParameter(String hotpParameter) {
        this.hotpParameter = hotpParameter;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
            HttpServletResponse response) throws AuthenticationException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Assert.notNull(authentication, "authentication cannot be null");

        CompoundAuthentication compound = getCompoundAuthenticationOrNewOne(authentication);
        authentication = extractLatestAuthOrSimpleAuth(authentication);

        String hotp = obtainHotp(request);

        Authentication authRequest = createSimpleAuthRequest(authentication,
                hotp);

        return getAuthenticationManager().authenticate(new CompoundAuthentication(compound.getReadyAuthentications(), authRequest));
    }

    protected Authentication createSimpleAuthRequest(
            Authentication authentication, String hotp) {
        if (!(authentication instanceof SSOUserTransportAuthenticationToken)) {
            String msg = "Unexpected authentication of class " + authentication.getClass() + ": " + authentication;
            logger.error(msg);
            throw new AuthenticationServiceException(msg);
        }
        SSOUserTransportAuthenticationToken token = (SSOUserTransportAuthenticationToken) authentication;
        Authentication authRequest = createCheckHotpAuthRequest(hotp, token.getSsoUser());
        return authRequest;
    }

    protected Authentication createCheckHotpAuthRequest(String hotp, SSOUser ssoUser) {
        return new CheckHOTPToken(ssoUser, hotp);
    }

    protected String obtainHotp(HttpServletRequest request) {
        return request.getParameter(hotpParameter);
    }
}
