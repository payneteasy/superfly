package com.payneteasy.superfly.model.ui.user;

import com.payneteasy.superfly.api.OTPType;

import java.io.Serializable;

public class OtpUserDescription implements Serializable {
    private UserForDescription userForDescription;
    private boolean hasOtpMasterKey;

    public boolean isHasOtpMasterKey() {
        return hasOtpMasterKey;
    }

    public OtpUserDescription setHasOtpMasterKey(boolean hasOtpMasterKey) {
        this.hasOtpMasterKey = hasOtpMasterKey;
        return this;
    }

    public UserForDescription getUserForDescription() {
        return userForDescription;
    }

    public OtpUserDescription setUserForDescription(UserForDescription userForDescription) {
        this.userForDescription = userForDescription;
        return this;
    }

    public boolean isUserOtpRequiredButNotInit() {
        return userForDescription.getOtpType() != OTPType.NONE && !hasOtpMasterKey;
    }

    public boolean isUserOtpRequiredAndInit() {
        return userForDescription.getOtpType() != OTPType.NONE && hasOtpMasterKey;
    }

    public OTPType getOtpType() {
        return userForDescription.getOtpType();
    }
}
