package com.payneteasy.superfly.service;

/**
 * Service for cryptographic operations in remote authentication.
 */
public interface RemoteAuthCryptoService {
    /**
     * Decrypts a password using the subsystem's private key.
     *
     * @param encryptedPassword The encrypted password (Base64 URL-safe no padding).
     * @param privateKey        The private key of the subsystem.
     * @return The decrypted password.
     */
    String decryptPassword(String encryptedPassword, String privateKey);

    /**
     * Decrypts an OTP using the subsystem's private key.
     *
     * @param encryptedOtp The encrypted OTP (Base64 URL-safe no padding).
     * @param privateKey   The private key of the subsystem.
     * @return The decrypted OTP.
     */
    String decryptOtp(String encryptedOtp, String privateKey);
}

