package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.crypto.CryptoService;
import com.payneteasy.superfly.dao.SmtpServerDao;
import com.payneteasy.superfly.model.SmtpServerPassword;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Encrypts the SMTP server passwords stored in plain text by earlier versions. Idempotent: only passwords that are
 * not in the CryptoService format are selected, so a repeated start changes nothing.
 */
@Component
public class SmtpServerPasswordEncryptionTask implements SmartInitializingSingleton {

    private static final Logger logger = LoggerFactory.getLogger(SmtpServerPasswordEncryptionTask.class);

    private final SmtpServerDao dao;
    private final CryptoService cryptoService;

    public SmtpServerPasswordEncryptionTask(SmtpServerDao dao, CryptoService cryptoService) {
        this.dao = dao;
        this.cryptoService = cryptoService;
    }

    @Override
    public void afterSingletonsInstantiated() {
        try {
            encryptPlainPasswords();
        } catch (RuntimeException e) {
            // the passwords stay usable in the transitional mode, a DB hiccup must not stop the application
            logger.error("Could not encrypt the SMTP server passwords: {}", e.toString());
        }
    }

    void encryptPlainPasswords() {
        List<SmtpServerPassword> passwords = dao.getSmtpServersWithPlainPassword();
        int encrypted = 0;
        for (SmtpServerPassword password : passwords) {
            try {
                dao.encryptSmtpServerPassword(password.getId(), cryptoService.encrypt(password.getPassword()));
                encrypted++;
            } catch (Exception e) {
                // never log the password; the exception message of the crypto service does not contain it
                logger.error("Could not encrypt the password of SMTP server {}: {}", password.getName(), e.toString());
            }
        }
        if (!passwords.isEmpty()) {
            logger.info("Encrypted {} of {} SMTP server passwords", encrypted, passwords.size());
        }
    }
}
