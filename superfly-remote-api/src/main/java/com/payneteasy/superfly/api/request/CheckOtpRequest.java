package com.payneteasy.superfly.api.request;

import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.api.SSOUser;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class CheckOtpRequest {
    private String userName;
    private String code;
    private OTPType otpType;
    private boolean isOtpOptional;

    public CheckOtpRequest(SSOUser ssoUser, String code) {
        userName = ssoUser.getName();
        isOtpOptional = ssoUser.isOtpOptional();
        otpType = ssoUser.getOtpType();
        this.code = code;
    }

    public CheckOtpRequest(String userName, String code, OTPType otpType, boolean isOtpOptional) {
        this.userName = userName;
        this.code = code;
        this.otpType = otpType;
        this.isOtpOptional = isOtpOptional;
    }
}
