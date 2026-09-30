package com.payneteasy.superfly.service.impl.remote.check;

import com.payneteasy.superfly.service.RemoteAuthCryptoService;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Service
public class RemoteAuthCryptoServiceImpl implements RemoteAuthCryptoService {

    private static final OAEPParameterSpec OAEP_SHA256 = new OAEPParameterSpec(
            "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);

    @Override
    public KeyPairData generateKeyPair(RemoteAuthEncryptionAlgorithm algorithm) {
        return switch (algorithm) {
            case RSA, RSA_OAEP -> {
                try {
                    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
                    keyPairGenerator.initialize(4096);
                    KeyPair keyPair = keyPairGenerator.generateKeyPair();

                    String publicKeyPem = toPem(keyPair.getPublic().getEncoded(), "PUBLIC KEY");
                    String privateKeyPem = toPem(keyPair.getPrivate().getEncoded(), "PRIVATE KEY");

                    yield new KeyPairData(publicKeyPem, privateKeyPem);
                } catch (NoSuchAlgorithmException e) {
                    throw new IllegalStateException("Cannot generate RSA key pair", e);
                }
            }
            case EC -> throw new UnsupportedOperationException(
                    "EC algorithm is not supported yet. Please use RSA instead."
            );
        };
    }

    private String toPem(byte[] encodedKey, String type) {
        String base64 = Base64.getEncoder().encodeToString(encodedKey);
        StringBuilder sb = new StringBuilder();
        sb.append("-----BEGIN ").append(type).append("-----\n");
        for (int i = 0; i < base64.length(); i += 64) {
            int end = Math.min(i + 64, base64.length());
            sb.append(base64, i, end).append('\n');
        }
        sb.append("-----END ").append(type).append("-----");
        return sb.toString();
    }

    @Override
    public String decryptPassword(String encryptedPassword, String privateKey, RemoteAuthEncryptionAlgorithm algorithm)
            throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException,
            InvalidKeySpecException, BadPaddingException, InvalidKeyException {
        return decrypt(encryptedPassword, privateKey, algorithm);
    }

    @Override
    public String decryptOtp(String encryptedOtp, String privateKey, RemoteAuthEncryptionAlgorithm algorithm)
            throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException,
            InvalidKeySpecException, BadPaddingException, InvalidKeyException {
        return decrypt(encryptedOtp, privateKey, algorithm);
    }

    private String decrypt(String encrypted, String privateKey, RemoteAuthEncryptionAlgorithm algorithm)
            throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
            IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        byte[] encryptedBytes = Base64.getUrlDecoder().decode(encrypted);
        PrivateKey key = parsePrivateKey(privateKey, algorithm);
        byte[] decrypted = decryptBytes(encryptedBytes, key, algorithm);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    private PrivateKey parsePrivateKey(String privateKeyPem, RemoteAuthEncryptionAlgorithm algorithm)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        String normalized = privateKeyPem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(normalized);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        // RSA_OAEP keys are plain RSA keys; only the padding differs
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return keyFactory.generatePrivate(keySpec);
    }

    private byte[] decryptBytes(byte[] encrypted, PrivateKey privateKey, RemoteAuthEncryptionAlgorithm algorithm)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException {

        return switch (algorithm) {
            case RSA -> {
                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(Cipher.DECRYPT_MODE, privateKey);
                yield cipher.doFinal(encrypted);
            }
            case RSA_OAEP -> {
                Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
                // Explicit spec: the bare OAEPWithSHA-256AndMGF1Padding name makes SunJCE use MGF1 with SHA-1,
                // which breaks ciphertexts produced by OpenSSL/WebCrypto/Node (MGF1-SHA256).
                try {
                    cipher.init(Cipher.DECRYPT_MODE, privateKey, OAEP_SHA256);
                } catch (InvalidAlgorithmParameterException e) {
                    throw new InvalidKeyException("Unsupported OAEP parameters", e);
                }
                yield cipher.doFinal(encrypted);
            }
            case EC -> throw new UnsupportedOperationException(
                    "EC algorithm is not supported yet. Please use RSA instead."
            );
        };
    }
}
