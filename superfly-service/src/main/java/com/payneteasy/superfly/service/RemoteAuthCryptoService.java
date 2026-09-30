package com.payneteasy.superfly.service;

import com.payneteasy.superfly.service.impl.remote.check.KeyPairData;
import com.payneteasy.superfly.service.impl.remote.check.RemoteAuthEncryptionAlgorithm;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * Service for cryptographic operations in remote authentication.
 */
public interface RemoteAuthCryptoService {

    KeyPairData generateKeyPair(RemoteAuthEncryptionAlgorithm algorithm);
    
    /**
     * Decrypts a password using the subsystem's private key.
     *
     * @param encryptedPassword The encrypted password (Base64 URL-safe no padding).
     * @param privateKey        The private key of the subsystem.
     * @param algorithm         The encryption algorithm to use.
     * @return The decrypted password.
     */
    String decryptPassword(String encryptedPassword, String privateKey, RemoteAuthEncryptionAlgorithm algorithm)
            throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException,
            InvalidKeySpecException, BadPaddingException, InvalidKeyException;

    /**
     * Decrypts an OTP using the subsystem's private key.
     *
     * @param encryptedOtp The encrypted OTP (Base64 URL-safe no padding).
     * @param privateKey   The private key of the subsystem.
     * @param algorithm    The encryption algorithm to use.
     * @return The decrypted OTP.
     */
    String decryptOtp(String encryptedOtp, String privateKey, RemoteAuthEncryptionAlgorithm algorithm)
            throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException,
            InvalidKeySpecException, BadPaddingException, InvalidKeyException;
}

