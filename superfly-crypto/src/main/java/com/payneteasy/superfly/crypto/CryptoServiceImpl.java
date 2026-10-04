package com.payneteasy.superfly.crypto;

import com.payneteasy.superfly.crypto.exception.DecryptException;
import com.payneteasy.superfly.crypto.exception.EncryptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Encrypts with AES-256-GCM: {@code "v2:" + base64(nonce[12] || ciphertext || tag[16])}.
 * Decrypts that format and the legacy one (bare base64 of AES-CBC with a zero IV), which
 * contains no ':' and is therefore unambiguous.
 */
public class CryptoServiceImpl implements CryptoService {
    private static final String VERSION_PREFIX = "v2:";
    private static final int    NONCE_BYTES    = 12;
    private static final int    TAG_BITS       = 128;

    private static final String PLACEHOLDER_PREFIX = "GOOGLE_AUTH_OTP_";

    // The values the legacy deployments shipped with; used only to read legacy ciphertexts, never to encrypt
    private static final String LEGACY_DEFAULT_SECRET = PLACEHOLDER_PREFIX + "SECRET";
    private static final String LEGACY_DEFAULT_SALT   = PLACEHOLDER_PREFIX + "SALT";

    private static final Logger log = LoggerFactory.getLogger(CryptoServiceImpl.class);

    private final SecureRandom random = new SecureRandom();
    private final SecretKey    secretKey;
    private final SecretKey    legacyKey;

    public CryptoServiceImpl(String cryptoSecret, String cryptoSalt) {
        this(cryptoSecret, cryptoSalt, false);
    }

    /**
     * @param legacyDefaultKey if true, legacy (non-"v2:") ciphertexts are decrypted with the key derived from
     *                         the old default placeholders instead of the configured one
     */
    public CryptoServiceImpl(String cryptoSecret, String cryptoSalt, boolean legacyDefaultKey) {
        this.secretKey = deriveKey(cryptoSecret, cryptoSalt);
        if (legacyDefaultKey) {
            log.warn("legacy OTP keys are decrypted with the old default key, re-encryption pending");
            this.legacyKey = deriveKey(LEGACY_DEFAULT_SECRET, LEGACY_DEFAULT_SALT);
        } else {
            this.legacyKey = secretKey;
        }
    }

    @Override
    public boolean isLegacy(String ciphertext) {
        return !ciphertext.startsWith(VERSION_PREFIX);
    }

    /**
     * Fail-fast check for configured key material; never includes the values in the message.
     *
     * @throws IllegalStateException if a value is missing, blank or still a shipped placeholder
     */
    public static void requireConfigured(String secretName, String secret, String saltName, String salt) {
        requireConfigured(secretName, secret);
        requireConfigured(saltName, salt);
    }

    private static void requireConfigured(String name, String value) {
        if (value == null || value.isBlank() || value.startsWith(PLACEHOLDER_PREFIX)) {
            throw new IllegalStateException("OTP master key encryption parameter '" + name
                    + "' is not set (missing, blank or a placeholder); set it to a unique secret value");
        }
    }

    @Override
    public String encrypt(String strToEncrypt) throws EncryptException {
        try {
            byte[] nonce = new byte[NONCE_BYTES];
            random.nextBytes(nonce);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_BITS, nonce));
            byte[] ct = cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8));

            byte[] out = new byte[NONCE_BYTES + ct.length];
            System.arraycopy(nonce, 0, out, 0, NONCE_BYTES);
            System.arraycopy(ct, 0, out, NONCE_BYTES, ct.length);
            return VERSION_PREFIX + Base64.getEncoder().encodeToString(out);
        } catch (GeneralSecurityException e) {
            throw new EncryptException(e);
        }
    }

    @Override
    public String decrypt(String strToDecrypt) throws DecryptException {
        try {
            if (strToDecrypt.startsWith(VERSION_PREFIX)) {
                byte[] in = Base64.getDecoder().decode(strToDecrypt.substring(VERSION_PREFIX.length()));
                if (in.length < NONCE_BYTES + TAG_BITS / 8) {
                    throw new DecryptException("Ciphertext is too short");
                }
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_BITS, in, 0, NONCE_BYTES));
                return new String(cipher.doFinal(in, NONCE_BYTES, in.length - NONCE_BYTES), StandardCharsets.UTF_8);
            }
            return decryptLegacy(strToDecrypt);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new DecryptException(e);
        }
    }

    private String decryptLegacy(String strToDecrypt) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, legacyKey, new IvParameterSpec(new byte[16]));
        return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)), StandardCharsets.UTF_8);
    }

    // The same PBKDF2 derivation as the legacy code: existing ciphertexts depend on it
    private static SecretKey deriveKey(String secret, String salt) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            PBEKeySpec       spec    = new PBEKeySpec(secret.toCharArray(), salt.getBytes(StandardCharsets.UTF_8), 65536, 256);
            return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Cannot derive the OTP master key encryption key", e);
        }
    }
}
