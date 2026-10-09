package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.crypto.CryptoService;
import com.payneteasy.superfly.dao.SubsystemDao;
import com.payneteasy.superfly.model.SubsystemPrivateKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Encrypts the subsystem private keys stored in plain text by earlier versions. Idempotent: only keys that are not
 * in the CryptoService format are selected, so a repeated start changes nothing.
 */
@Component
public class SubsystemPrivateKeyEncryptionTask implements SmartInitializingSingleton {

    private static final Logger logger = LoggerFactory.getLogger(SubsystemPrivateKeyEncryptionTask.class);

    private final SubsystemDao dao;
    private final CryptoService cryptoService;

    public SubsystemPrivateKeyEncryptionTask(SubsystemDao dao, CryptoService cryptoService) {
        this.dao = dao;
        this.cryptoService = cryptoService;
    }

    @Override
    public void afterSingletonsInstantiated() {
        try {
            encryptPlainKeys();
        } catch (RuntimeException e) {
            // the keys stay readable in the transitional mode, a DB hiccup must not stop the application
            logger.error("Could not encrypt the subsystem private keys: {}", e.toString());
        }
    }

    void encryptPlainKeys() {
        List<SubsystemPrivateKey> keys = dao.getSubsystemsWithPlainPrivateKey();
        int encrypted = 0;
        for (SubsystemPrivateKey key : keys) {
            try {
                dao.encryptSubsystemPrivateKey(key.getId(), cryptoService.encrypt(key.getPrivateKey()));
                encrypted++;
            } catch (Exception e) {
                // never log the key; the exception message of the crypto service does not contain it
                logger.error("Could not encrypt the private key of subsystem {}: {}", key.getName(), e.toString());
            }
        }
        if (!keys.isEmpty()) {
            logger.info("Encrypted {} of {} subsystem private keys", encrypted, keys.size());
        }
    }
}
