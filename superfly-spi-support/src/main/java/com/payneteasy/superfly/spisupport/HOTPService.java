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
     * @param userId ID of a user
     * @return New master key
     */
    String resetGoogleAuthMasterKey(long userId) throws UserNotFoundException;

    boolean validateGoogleTimePassword(String username, String password) throws SsoDecryptException;
}
