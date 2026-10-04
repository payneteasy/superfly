package com.payneteasy.superfly.hotp;

import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.api.exceptions.SsoDecryptException;
import com.payneteasy.superfly.api.UserNotFoundException;
import com.payneteasy.superfly.crypto.CryptoService;
import com.payneteasy.superfly.crypto.exception.DecryptException;
import com.payneteasy.superfly.crypto.exception.EncryptException;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.spisupport.HOTPService;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@Transactional
public class HOTPServiceImpl implements HOTPService {

    private static final Logger logger = LoggerFactory.getLogger(HOTPServiceImpl.class);

    @Getter
    private final ThreadLocal<GoogleAuthenticator> googleAuthenticator = ThreadLocal.withInitial(GoogleAuthenticator::new);

    private UserService userService;
    private CryptoService cryptoService;
    private PlatformTransactionManager transactionManager;

    @Autowired
    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setCryptoService(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    @Override
    public String resetGoogleAuthMasterKey(String subsystemIdentifier, String username) throws UserNotFoundException, SsoDecryptException {
        String key = googleAuthenticator.get().createCredentials().getKey();
        encryptAndPersistMasterKey(OTPType.GOOGLE_AUTH, key, username);
        return key;
    }

    @Override
    public String getUrlToGoogleAuthQrCode(String secretKey, String issuer, String accountName) {
        // Built locally: GoogleAuthenticatorQRGenerator.getOtpAuthURL sends the secret to api.qrserver.com
        boolean hasIssuer = issuer != null && !issuer.isEmpty();
        StringBuilder uri = new StringBuilder("otpauth://totp/");
        if (hasIssuer) {
            uri.append(encode(issuer)).append(':');
        }
        uri.append(encode(accountName)).append("?secret=").append(encode(secretKey));
        if (hasIssuer) {
            uri.append("&issuer=").append(encode(issuer));
        }
        return uri.toString();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    @Override
    public boolean validateGoogleTimePassword(String username, String password) throws SsoDecryptException {
        if (password == null || !password.matches("^[0-9]{6}$")) {
            return false;
        }
        int verificationCode = Integer.parseInt(password);
        String masterKeyEncrypt = userService.getOtpMasterKeyByUsername(username);
        if (masterKeyEncrypt == null) {
            logger.error("GA master key for " + username + " is null");
            throw new SsoDecryptException("GA master key for " + username + " is null");
        }
        String masterKey;
        try {
            masterKey = cryptoService.decrypt(masterKeyEncrypt);
        } catch (DecryptException e) {
            throw new SsoDecryptException("decrypt error", e);
        }
        boolean authorized = googleAuthenticator.get().authorize(masterKey, verificationCode);
        if (authorized && cryptoService.isLegacy(masterKeyEncrypt)) {
            reencryptLegacyKey(username, masterKeyEncrypt, masterKey);
        }
        return authorized;
    }

    // Only after a valid code: CBC has no MAC, so a wrong legacy key may yield garbage that must not be re-saved.
    // The write runs in its own transaction after the caller's one has committed: a failure inside the caller's
    // transaction would mark it rollback-only and fail the login, and an inner transaction started before the
    // commit could wait for a row lock held by the caller.
    private void reencryptLegacyKey(String username, String legacyCiphertext, String masterKey) {
        String encrypted;
        try {
            encrypted = cryptoService.encrypt(masterKey);
        } catch (EncryptException e) {
            logger.warn("Could not re-encrypt legacy OTP master key for user {}: {}", username, e.getClass().getSimpleName());
            return;
        }
        Runnable save = () -> {
            try {
                TransactionTemplate template = new TransactionTemplate(transactionManager, new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
                template.executeWithoutResult(status -> userService.persistOtpMasterKeyIfUnchanged(username, legacyCiphertext, encrypted));
            } catch (Exception e) {
                logger.warn("Could not save re-encrypted OTP master key for user {}: {}", username, e.getClass().getSimpleName());
            }
        };
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    save.run();
                }
            });
        } else {
            save.run();
        }
    }

    @Override
    public void persistOtpKey(OTPType otpType, String username, String key) throws SsoDecryptException {
        userService.updateUserOtpType(username, otpType.code());
        switch (otpType) {
            case GOOGLE_AUTH:
                encryptAndPersistMasterKey(otpType, key, username);
                break;
            case NONE:
            default:
                break;

        }
    }

    private void encryptAndPersistMasterKey(OTPType otpType, String key, String username) throws SsoDecryptException {
        switch (otpType) {
            case GOOGLE_AUTH:
                String encryptKey = null;
                try {
                    encryptKey = cryptoService.encrypt(key);
                } catch (EncryptException e) {
                    throw new SsoDecryptException("encrypt error", e);
                }
                userService.persistOtpMasterKeyForUsername(username, encryptKey);
                break;
            case NONE:
            default:
                break;
        }
    }

}
