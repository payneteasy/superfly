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
    private OTPType multiFactorAuthMethod;
    private String policyName;

    public void setLocalSecurityService(LocalSecurityService localSecurityService) {
        this.localSecurityService = localSecurityService;
    }

    public void setMultiFactorAuthMethod(String multiFactorAuthMethod) {
        this.multiFactorAuthMethod = OTPType.strictFromCode(multiFactorAuthMethod);
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    @PostConstruct
    protected void init() {
        if (policyName.equals("pcidss") && multiFactorAuthMethod == OTPType.NONE) {
            throw new IllegalStateException("For pcidss, you must set context param 'superfly-mfa-method' different than 'none'");
        }
    }

    public Authentication postProcess(Authentication auth) {
        CompoundAuthentication compoundAuthentication = (CompoundAuthentication) auth;
        if (compoundAuthentication != null) {
            Authentication lastAuth = compoundAuthentication.getLatestReadyAuthentication();
            OtpUserDescription user = localSecurityService.getOtpUserForDescription(lastAuth.getName());

            if (isUserOtpDifferentThanSystemOtp(user)) {
                compoundAuthentication.addReadyAuthentication(new LocalNeedOTPToken(lastAuth.getName(), multiFactorAuthMethod));
            } else if (isUserOtpRequiredButNotInit(user)) {
                //For example, when reset otp user's master key
                compoundAuthentication.addReadyAuthentication(
                        new LocalNeedOTPToken(lastAuth.getName(), user.getOtpType())
                );
            }
        }

        return auth;
    }

    private boolean isUserOtpRequiredButNotInit(OtpUserDescription userForDescription) {
        return userForDescription.isUserOtpRequiredButNotInit();
    }

    private boolean isUserOtpDifferentThanSystemOtp(OtpUserDescription userForDescription) {
        return multiFactorAuthMethod != OTPType.NONE
                && multiFactorAuthMethod != userForDescription.getOtpType();
    }
}
