package com.payneteasy.superfly.email;

/**
 * Used to send emails.
 *
 * @author Roman Puchkovskiy
 */
public interface EmailService {
    /**
     * Sends a message containing an HOTP table.
     *
     * @param subsystemIdentifier   identifier of subsystem
     *                              through which smtp-server
     *                              to send message
     * @param to                    to address
     * @param fileName                file name
     * @param table                    table content
     * @throws RuntimeMessagingException if something is wrong
     */
    @Deprecated
    void sendHOTPTable(String subsystemIdentifier, String to,
            String fileName, byte[] table) throws RuntimeMessagingException;

    /**
     * Sends a message which complains that there's no public key for the user.
     *
     * @param subsystemIdentifier   identifier of subsystem
     * through which smtp-server to send message
     * @param email        to address
     * @throws RuntimeMessagingException if something is wrong
     */
    @Deprecated
    void sendNoPublicKeyMessage(String subsystemIdentifier,
            String email) throws RuntimeMessagingException;

    /**
     * Sends a test message.
     *
     * @param serverId    id of the server to test
     * @throws RuntimeMessagingException
     */
    void sendTestMessage(long serverId, String email) throws RuntimeMessagingException;

    /**
        * Sends a message containing an encrypted password.
        *
     * @param subsystemIdentifier       identifier of subsystem
     *                                  through which smtp-server to
     *                                  send message
        * @param to                        to address
        * @param fileName                    file name
        * @param encryptedPasswordBytes    encrypted password data
        * @throws RuntimeMessagingException if something is wrong
        */
    void sendPassword(String subsystemIdentifier, String to, String fileName, byte[] encryptedPasswordBytes);
}
