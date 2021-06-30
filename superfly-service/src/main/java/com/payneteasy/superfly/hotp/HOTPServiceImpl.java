package com.payneteasy.superfly.hotp;

import com.payneteasy.superfly.api.MessageSendException;
import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.api.SsoDecryptException;
import com.payneteasy.superfly.api.UserNotFoundException;
import com.payneteasy.superfly.crypto.CryptoService;
import com.payneteasy.superfly.crypto.PublicKeyCrypto;
import com.payneteasy.superfly.crypto.exception.DecryptException;
import com.payneteasy.superfly.crypto.exception.EncryptException;
import com.payneteasy.superfly.email.EmailService;
import com.payneteasy.superfly.email.RuntimeMessagingException;
import com.payneteasy.superfly.model.ui.user.UIUser;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.spi.ExportException;
import com.payneteasy.superfly.spi.HOTPProvider;
import com.payneteasy.superfly.spisupport.HOTPService;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Transactional
public class HOTPServiceImpl implements HOTPService {

    private static final Logger logger = LoggerFactory.getLogger(HOTPServiceImpl.class);

    private final ThreadLocal<GoogleAuthenticator> googleAuthenticator = ThreadLocal.withInitial(GoogleAuthenticator::new);

    private EmailService emailService;
    private HOTPProvider hotpProvider;
    private PublicKeyCrypto publicKeyCrypto;
    private UserService userService;
    private CryptoService cryptoService;

    @Required
    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    @Required
    public void setHotpProvider(HOTPProvider hotpProvider) {
        this.hotpProvider = hotpProvider;
    }

    @Required
    public void setPublicKeyCrypto(PublicKeyCrypto publicKeyCrypto) {
        this.publicKeyCrypto = publicKeyCrypto;
    }

    @Required
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Required
    public void setCryptoService(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    public void sendTableIfSupported(String subsystemIdentifier, long userId) throws MessageSendException, ExportException {
        obtainUserIfNeededAndSendTableIfSupported(subsystemIdentifier, userId, null);
    }

    public void resetTableAndSendIfSupported(String subsystemIdentifier, long userId) throws MessageSendException, ExportException {
        UIUser user = userService.getUser(userId);
        hotpProvider.resetSequence(user.getUsername());
        obtainUserIfNeededAndSendTableIfSupported(subsystemIdentifier, userId, user);
    }

    @Override
    public String resetGoogleAuthMasterKey(String subsystemIdentifier, String username) throws UserNotFoundException, SsoDecryptException {
        String key = googleAuthenticator.get().createCredentials().getKey();
        encryptAndPersistMasterKey(OTPType.GOOGLE_AUTH, username, key);
        return key;
    }

    @Override
    public String getUrlToGoogleAuthQrCode(String secretKey, String issuer, String accountName) {
        return GoogleAuthenticatorQRGenerator.getOtpAuthURL(
                issuer,
                accountName,
                new GoogleAuthenticatorKey.Builder(secretKey).build()
        );
    }

    @Override
    public boolean validateGoogleTimePassword(String username, String password) throws SsoDecryptException {
        if (password == null || !password.matches("^[0-9]{6}$")) {
            return false;
        }
        int verificationCode = Integer.parseInt(password);
        String masterKeyEncrypt = userService.getGoogleAuthMasterKeyByUsername(username);
        if (masterKeyEncrypt == null) {
            throw new SsoDecryptException("GA master key for " + username + " is null");
        }
        String masterKey;
        try {
            masterKey = cryptoService.decrypt(masterKeyEncrypt);
        } catch (DecryptException e) {
            throw new SsoDecryptException(e);
        }
        return googleAuthenticator.get().authorize(masterKey, verificationCode);
    }

    private void obtainUserIfNeededAndSendTableIfSupported(String subsystemIdentifier, long userId, UIUser user) throws MessageSendException, ExportException {
        if (hotpProvider.outputsSequenceForDownload()) {
            if (user == null) {
                user = userService.getUser(userId);
            }
            if (user.getPublicKey() != null && user.getPublicKey().trim().length() > 0) {
                actuallySendTable(subsystemIdentifier, user);
            } else {
                sendNoPublicKey(subsystemIdentifier, user.getEmail());
            }
        }
    }

    protected void sendNoPublicKey(String subsystemIdentifier, String email) throws MessageSendException {
        try {
            emailService.sendNoPublicKeyMessage(subsystemIdentifier, email);
        } catch (RuntimeMessagingException e) {
            logger.error("Could not send a message to " + email, e);
            throw new MessageSendException(e);
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
                    throw new SsoDecryptException(e);
                }
                userService.persistGoogleAuthMasterKeyForUsername(username, encryptKey);
                break;
            case NONE:
            default:
                break;
        }
    }

    protected void actuallySendTable(String subsystemIdentifier, UIUser user) throws MessageSendException, ExportException {
        String filename = hotpProvider.getSequenceForDownloadFileName(user.getUsername());
        // TODO: use streams, not buffers
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            hotpProvider.outputSequenceForDownload(user.getUsername(), baos);
            byte[] bytes = baos.toByteArray();

            baos = new ByteArrayOutputStream();
            publicKeyCrypto.encrypt(bytes, filename, user.getPublicKey(), baos);
        } catch (IOException e) {
            // should not get IOException as we're working in-memory
            throw new IllegalStateException(e);
        }

        try {
            emailService.sendHOTPTable(subsystemIdentifier, user.getEmail(), filename, baos.toByteArray());
        } catch (RuntimeMessagingException e) {
            logger.error("Could not send a message to " + user.getEmail(), e);
            throw new MessageSendException(e);
        }
    }

}
