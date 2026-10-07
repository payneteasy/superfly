package com.payneteasy.superfly.spisupport;

import com.payneteasy.superfly.api.CheckOtpResult;
import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.api.exceptions.SsoDecryptException;
import com.payneteasy.superfly.api.UserNotFoundException;

/**
 * Service to deal with HOTP management.
 *
 * @author Roman Puchkovskiy
 */
public interface HOTPService {
    /**
     * Reset Master Key
     *
     * @param subsystemIdentifier identifier of subsystem
     *                            which smtp server to user when sending message
     * @param username            name of the user
     * @return New master key
     * @throws UserNotFoundException if no such user
     * @since 1.7
     */
    String resetGoogleAuthMasterKey(String subsystemIdentifier, String username) throws UserNotFoundException, SsoDecryptException;

    /**
     * Get google auth QR code
     *
     * @param secretKey   Google Auth secret key
     * @param issuer      The issuer name. This parameter cannot contain the colon
     *                    (:) character. This parameter can be null.
     * @param accountName The account name. This parameter shall not be null.
     * @return an otpauth scheme URI for loading into a client application.
     */
    String getUrlToGoogleAuthQrCode(String secretKey, String issuer, String accountName);

    /**
     * Checks a Google Authenticator code and marks its time step as used on success.
     *
     * @param username name of the user
     * @param password the code
     * @return {@link CheckOtpResult.Status#SUCCESS}, {@link CheckOtpResult.Status#INVALID},
     * {@link CheckOtpResult.Status#ALREADY_USED} or {@link CheckOtpResult.Status#CLOCK_SKEW}
     */
    CheckOtpResult.Status validateGoogleTimePassword(String username, String password) throws SsoDecryptException;

    void persistOtpKey(OTPType otpType, String username, String key) throws SsoDecryptException;

}
