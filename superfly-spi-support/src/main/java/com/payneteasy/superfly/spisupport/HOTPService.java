package com.payneteasy.superfly.spisupport;

import com.payneteasy.superfly.api.MessageSendException;
import com.payneteasy.superfly.api.SsoDecryptException;
import com.payneteasy.superfly.api.UserNotFoundException;

/**
 * Service to deal with HOTP management.
 *
 * @author Roman Puchkovskiy
 */
public interface HOTPService {
    /**
     * Sends a table to a user if provider supports this.
     *
     * @param userId    ID of a user
     * @throws MessageSendException
     */
    void sendTableIfSupported(String subsystemIdentifier, long userId) throws MessageSendException;

    /**
     * Resets a table and sends a new table to a user if provider supports
     * sending.
     *
     * @param userId    ID of a user
     * @throws MessageSendException
     */
    void resetTableAndSendIfSupported(String subsystemIdentifier, long userId) throws MessageSendException;

    /**
     * Reset Master Key
     * @param subsystemIdentifier identifier of subsystem
     *                            which smtp server to user when sending message
     * @param username            name of the user
     * @throws UserNotFoundException if no such user
     * @return New master key
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
}
