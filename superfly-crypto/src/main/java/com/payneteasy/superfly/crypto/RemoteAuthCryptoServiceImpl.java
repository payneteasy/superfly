package com.payneteasy.superfly.crypto;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class RemoteAuthCryptoServiceImpl implements RemoteAuthCryptoService {

    private static final RemoteAuthEncryptionAlgorithm DEFAULT_ALGORITHM = RemoteAuthEncryptionAlgorithm.RSA;

    private static final String PEM_PUBLIC_BEGIN = "-----BEGIN PUBLIC KEY-----\n";
    private static final String PEM_PUBLIC_END   = "\n-----END PUBLIC KEY-----\n";

    @Override
    public String generateKeyPairForSubsystem(String subsystemName, RemoteAuthEncryptionAlgorithm algorithm) {

        var algorithmToUse = algorithm == null ? DEFAULT_ALGORITHM : algorithm;

        try {

            KeyPairGenerator kpg = switch (algorithmToUse) {
                case RSA -> {
                    var generator = KeyPairGenerator.getInstance("RSA");
                    generator.initialize(2048, new SecureRandom());
                    yield generator;
                }
                case EC -> {
                    var generator = KeyPairGenerator.getInstance("EC");
                    ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
                    generator.initialize(ecSpec, new SecureRandom());
                    yield generator;
                }
            };

            KeyPair keyPair = kpg.generateKeyPair();

            byte[] spki = keyPair.getPublic().getEncoded();

            //TODO continue here

            String base64 = Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(spki);
            return PEM_PUBLIC_BEGIN + base64 + PEM_PUBLIC_END;
        } catch (Exception e) {
            // Consolidate any crypto errors into an unchecked exception for simplicity
            throw new IllegalStateException("Failed to generate key pair for subsystem '" + subsystemName + "' using " + algorithmToUse + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void revokeSubsystemKeyPair(String subsystemName) {

    }

    @Override
    public String decryptValue(String subsystemName, String value) {
        throw new UnsupportedOperationException("decryptValue is not implemented: encryption scheme is unspecified");
    }
}
