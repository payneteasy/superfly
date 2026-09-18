package com.payneteasy.superfly.model.ui.user;

import com.payneteasy.superfly.api.OTPType;

/**
 * The rule "OTP mandatory =&gt; OTP type is not none", in one place.
 * <p>
 * The admin pages apply it both in the form, so that the administrator sees the outcome
 * before saving, and on submit. Keeping the default type and the condition here stops the
 * call sites from drifting apart.
 */
public final class OtpTypeDefaults {

    /** OTP type assigned when OTP is mandatory but no type has been chosen. */
    public static final OTPType MANDATORY_DEFAULT = OTPType.GOOGLE_AUTH;

    private OtpTypeDefaults() {
    }

    /**
     * @return true when the user would end up with mandatory OTP and no OTP type,
     *         which the authentication code silently treats as "no second factor"
     */
    public static boolean needsDefaultType(UIUser user) {
        return !user.isOtpOptional() && OTPType.fromCode(user.getOtpType()) == OTPType.NONE;
    }
}
