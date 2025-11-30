package com.payneteasy.superfly.crypto;

public interface RemoteAuthCryptoService {

    String generateKeyPairForSubsystem(String subsystemName, RemoteAuthEncryptionAlgorithm algorithm);

    void revokeSubsystemKeyPair(String subsystemName);

    String decryptValue(String subsystemName, String value);
}
