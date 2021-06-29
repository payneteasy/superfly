package com.payneteasy.superfly.web.security;

import com.payneteasy.superfly.security.SuperflyOTPAuthenticationProcessingFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

/**
 * Filter for HOTP checking in Superfly webapp itself.
 *
 * @author Roman Puchkovskiy
 */
public class SuperflyLocalOTPAuthenticationProcessingFilter extends
        SuperflyOTPAuthenticationProcessingFilter {

    @Override
    protected Authentication createSimpleAuthRequest(
            Authentication authentication, String otp) {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;
        return new LocalCheckOTPToken(authentication.getName(), otp, token.getAuthorities());
    }

}
