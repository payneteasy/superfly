package com.payneteasy.superfly.service;

import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.api.SsoDecryptException;
import com.payneteasy.superfly.api.UserDescription;
import com.payneteasy.superfly.model.ui.user.OtpUserDescription;
import com.payneteasy.superfly.model.ui.user.UserForDescription;

/**
 * Service used for local security-related purposes.
 * 
 * @author Roman Puchkovskiy
 */
public interface LocalSecurityService {
    /**
     * Authenticates a local user.
     *
     * @param username    user name
     * @param password    password
     * @return actions assigned to the given user on success, or null on
     * failure (failure occurs if no such user exists, password does not match,
     * or if user is locked)
     */
    String[] authenticate(String username, String password);

    /**
     * Authenticates a user's one-time password.
     *
     * @param username    user name
     * @param otp        one-time password
     * @return authentication result
     */
    boolean authenticateUsingOTP(String username, String otp);

    OtpUserDescription getOtpUserForDescription(String username);

    void persistOtpKey(OTPType otpType, String username, String key) throws SsoDecryptException;
}
