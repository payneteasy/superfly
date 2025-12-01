package com.payneteasy.superfly.service;

/**
 * Service for remote authentication of external applications.
 */
public interface RemoteAuthService {

    /**
     * Checks the password for a user in a subsystem.
     *
     * @param subsystemName     Name of the subsystem.
     * @param username          Username.
     * @param passwordEncrypted Encrypted password (Base64 URL-safe no padding).
     * @param bearerToken       Bearer token provided in the request.
     * @param ipAddress         IP address of the client.
     * @param userAgent         User agent of the client.
     * @return Session token if authentication is successful.
     * @throws RemoteAuthException If authentication fails or other errors occur.
     */
    String checkPassword(String subsystemName, String username, String passwordEncrypted, String bearerToken, String ipAddress, String userAgent) throws RemoteAuthException;

    /**
     * Checks the OTP for a user in a subsystem.
     *
     * @param subsystemName Name of the subsystem.
     * @param username      Username.
     * @param otpEncrypted  Encrypted OTP (Base64 URL-safe no padding).
     * @param sessionToken  Session token obtained from checkPassword.
     * @param bearerToken   Bearer token provided in the request.
     * @return Result of the OTP check (SUCCESS, BAD_USER..., etc.).
     * @throws RemoteAuthException If validation fails.
     */
    String checkOtp(String subsystemName, String username, String otpEncrypted, String sessionToken, String bearerToken) throws RemoteAuthException;

    /**
     * Exception class for remote auth errors.
     */
    class RemoteAuthException extends Exception {
        private final String errorCode;

        public RemoteAuthException(String message, String errorCode) {
            super(message);
            this.errorCode = errorCode;
        }

        public RemoteAuthException(String message, String errorCode, Throwable cause) {
            super(message, cause);
            this.errorCode = errorCode;
        }

        public String getErrorCode() {
            return errorCode;
        }
    }
}

