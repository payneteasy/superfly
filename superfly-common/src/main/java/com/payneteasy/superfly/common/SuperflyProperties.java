package com.payneteasy.superfly.common;

import com.payneteasy.superfly.api.OTPType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Accessors(fluent = true, chain = true)
@Data()
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SuperflyProperties {
    String  superflyVersion;
    String  policyName;
    String  cryptoSecret;
    String  cryptoSalt;
    Long    maxLoginsFailed;
    Boolean csrfLoginValidatorEnable;
    Boolean enableMultiFactorAuth;
    OTPType forceMultiFactorAuthMethod;

    @Override
    public String toString() {
        return "SuperflyProperties{" +
                "superflyVersion='" + superflyVersion + '\'' +
                ", policyName='" + policyName + '\'' +
                ", cryptoSecret='" + "***" + '\'' +
                ", cryptoSalt='" + "***" + '\'' +
                ", maxLoginsFailed=" + maxLoginsFailed +
                ", csrfLoginValidatorEnable=" + csrfLoginValidatorEnable +
                ", enableMultiFactorAuth=" + enableMultiFactorAuth +
                ", forceMultiFactorAuthMethod=" + forceMultiFactorAuthMethod +
                '}';
    }
}
