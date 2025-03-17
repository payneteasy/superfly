package com.payneteasy.superfly.web.security;

import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.model.ui.user.OtpUserDescription;
import com.payneteasy.superfly.security.authentication.CompoundAuthentication;
import com.payneteasy.superfly.security.processor.AuthenticationPostProcessor;
import com.payneteasy.superfly.service.LocalSecurityService;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.security.core.Authentication;

/**
 * Post processor for init user's OTP settings if need
 * (OTP - One Time Password)
 *
 * @author Igor Vasilev
 */
@Accessors(chain = true)
public class CheckRequiredInitOtpAuthenticationPostProcessor implements
        AuthenticationPostProcessor {

    @Setter
    private LocalSecurityService localSecurityService;
    private OTPType              forceMultiFactorAuthMethod;
    @Setter
    private boolean              enableMultiFactorAuth;

    public CheckRequiredInitOtpAuthenticationPostProcessor setForceMultiFactorAuthMethod(OTPType forceMultiFactorAuthMethod) {
        this.forceMultiFactorAuthMethod = forceMultiFactorAuthMethod;
        return this;
    }

    public Authentication postProcess(Authentication auth) {
        CompoundAuthentication compoundAuthentication = (CompoundAuthentication) auth;
        if (compoundAuthentication != null) {
            if (!enableMultiFactorAuth) {
                return compoundAuthentication.getLatestReadyAuthentication();
            }
            Authentication     lastAuth = compoundAuthentication.getLatestReadyAuthentication();
            OtpUserDescription user     = localSecurityService.getOtpUserForDescription(lastAuth.getName());

            if (isUserOtpDifferentThanSystemOtp(user)) {
                compoundAuthentication.addReadyAuthentication(new LocalNeedOTPToken(lastAuth.getName(),
                                                                                    forceMultiFactorAuthMethod
                ));
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
