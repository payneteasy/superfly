package com.payneteasy.superfly.service.impl.remote.check;

import com.payneteasy.superfly.service.RemoteAuthCryptoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Stub implementation of RemoteAuthCryptoService.
 */
@Service
public class RemoteAuthCryptoServiceStub implements RemoteAuthCryptoService {

    private static final Logger logger = LoggerFactory.getLogger(RemoteAuthCryptoServiceStub.class);

    public RemoteAuthCryptoServiceStub() {
    }

    @Override
    public KeyPairData generateKeyPair(RemoteAuthEncryptionAlgorithm algorithm) {
        return null;
    }

    public String decryptPassword(String encryptedPassword, String privateKey, RemoteAuthEncryptionAlgorithm encryptionAlgorithm) {
        logger.warn("decryptPassword is a STUB. Returning encryptedPassword as is.");
        // In a real implementation, this would decrypt the password.
        // For now, we assume the input is already the plain password or we just return it to test flow.
        return encryptedPassword;
    }

    public String decryptOtp(String encryptedOtp, String privateKey, RemoteAuthEncryptionAlgorithm encryptionAlgorithm) {
        logger.warn("decryptOtp is a STUB. Returning encryptedOtp as is.");
        return encryptedOtp;
    }
}

