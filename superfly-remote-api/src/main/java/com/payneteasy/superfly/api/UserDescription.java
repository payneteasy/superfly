package com.payneteasy.superfly.api;

import lombok.Data;
import lombok.Getter;

import java.io.Serializable;

/**
 * Describes a user.
 *
 * @author Roman Puchkovskiy
 * @since 1.2-4
 */
@Data
public class UserDescription implements Serializable {
   private String  username;
    private String  email;
    private String  firstName;
    private String  lastName;
    private String  secretQuestion;
    private String  secretAnswer;
    private String  publicKey;
    private String  organization;
    private OTPType otpType;
    private boolean isOtpOptional;
}
