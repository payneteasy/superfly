package com.payneteasy.superfly.hotp;

import com.payneteasy.superfly.api.MessageSendException;
import com.payneteasy.superfly.api.SsoDecryptException;
import com.payneteasy.superfly.api.UserNotFoundException;
import com.payneteasy.superfly.common.utils.CryptoHelper;
import com.payneteasy.superfly.crypto.PublicKeyCrypto;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Transactional
public class HOTPServiceImpl implements HOTPService {

    private static final Logger logger = LoggerFactory.getLogger(HOTPServiceImpl.class);
    private static final String GOOGLE_AUTH_OTP_SECRET = "GOOGLE_AUTH_OTP_SECRET";
    private static final String GOOGLE_AUTH_OTP_SALT = "GOOGLE_AUTH_OTP_SALT";

    private final ThreadLocal<GoogleAuthenticator> googleAuthenticator = ThreadLocal.withInitial(GoogleAuthenticator::new);

    private EmailService emailService;
    private HOTPProvider hotpProvider;
    private PublicKeyCrypto publicKeyCrypto;
    private UserService userService;

    @Autowired
    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    @Autowired
    public void setHotpProvider(HOTPProvider hotpProvider) {
        this.hotpProvider = hotpProvider;
    }

    @Autowired
    public void setPublicKeyCrypto(PublicKeyCrypto publicKeyCrypto) {
        this.publicKeyCrypto = publicKeyCrypto;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
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
        String encryptKey = CryptoHelper.encrypt(
                key,
                GOOGLE_AUTH_OTP_SECRET,
                GOOGLE_AUTH_OTP_SALT
        );
        userService.persistGoogleAuthMasterKeyForUsername(username, encryptKey);
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
        String masterKey = CryptoHelper.decrypt(
                userService.getGoogleAuthMasterKeyByUsername(username),
                GOOGLE_AUTH_OTP_SECRET,
                GOOGLE_AUTH_OTP_SALT
        );
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
