package com.payneteasy.superfly.spisupport;

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

    boolean validateGoogleTimePassword(String username, String password) throws SsoDecryptException;

    void persistOtpKey(OTPType otpType, String username, String key) throws SsoDecryptException;

}
