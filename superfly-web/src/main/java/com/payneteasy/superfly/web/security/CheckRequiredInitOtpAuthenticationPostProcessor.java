package com.payneteasy.superfly.web.security;

import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.model.ui.user.OtpUserDescription;
import com.payneteasy.superfly.model.ui.user.UserForDescription;
import com.payneteasy.superfly.security.authentication.CompoundAuthentication;
import com.payneteasy.superfly.security.processor.AuthenticationPostProcessor;
import com.payneteasy.superfly.service.LocalSecurityService;
import org.springframework.security.core.Authentication;

import javax.annotation.PostConstruct;

/**
 * Post processor for init user's OTP settings if need
 * (OTP - One Time Password)
 *
 * @author Igor Vasilev
 */
public class CheckRequiredInitOtpAuthenticationPostProcessor implements
        AuthenticationPostProcessor {

    private LocalSecurityService localSecurityService;
    private OTPType forceMultiFactorAuthMethod;
    private boolean enableMultiFactorAuth;

    public void setLocalSecurityService(LocalSecurityService localSecurityService) {
        this.localSecurityService = localSecurityService;
    }

    public void setForceMultiFactorAuthMethod(String forceMultiFactorAuthMethod) {
        this.forceMultiFactorAuthMethod = OTPType.fromCode(forceMultiFactorAuthMethod);
    }

    public void setEnableMultiFactorAuth(boolean enableMultiFactorAuth) {
        this.enableMultiFactorAuth = enableMultiFactorAuth;
    }

    public Authentication postProcess(Authentication auth) {
        CompoundAuthentication compoundAuthentication = (CompoundAuthentication) auth;
        if (compoundAuthentication != null) {
            if (!enableMultiFactorAuth) {
                return compoundAuthentication.getLatestReadyAuthentication();
            }
            Authentication lastAuth = compoundAuthentication.getLatestReadyAuthentication();
            OtpUserDescription user = localSecurityService.getOtpUserForDescription(lastAuth.getName());

            if (isUserOtpDifferentThanSystemOtp(user)) {
                compoundAuthentication.addReadyAuthentication(new LocalNeedOTPToken(lastAuth.getName(), forceMultiFactorAuthMethod));
            } else if (isUserOtpRequiredButNotInit(user)) {
                //For example, when reset otp user's master key
                compoundAuthentication.addReadyAuthentication(
                        new LocalNeedOTPToken(lastAuth.getName(), user.getOtpType())
                );
            } else if (user.getOtpType() == OTPType.NONE) {
                return lastAuth;
            }
        }

        return auth;
    }

    private boolean isUserOtpRequiredButNotInit(OtpUserDescription userForDescription) {
        return userForDescription.isUserOtpRequiredButNotInit();
    }

    private boolean isUserOtpDifferentThanSystemOtp(OtpUserDescription userForDescription) {
        return forceMultiFactorAuthMethod != OTPType.NONE
                && forceMultiFactorAuthMethod != userForDescription.getOtpType();
    }
}
