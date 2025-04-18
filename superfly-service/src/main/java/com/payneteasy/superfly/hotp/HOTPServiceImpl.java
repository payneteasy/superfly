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
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class HOTPServiceImpl implements HOTPService {

    private static final Logger logger = LoggerFactory.getLogger(HOTPServiceImpl.class);

    @Getter
    private final ThreadLocal<GoogleAuthenticator> googleAuthenticator = ThreadLocal.withInitial(GoogleAuthenticator::new);

    private UserService userService;
    private CryptoService cryptoService;

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
        String masterKeyEncrypt = userService.getOtpMasterKeyByUsername(username);
        if (masterKeyEncrypt == null) {
            throw new SsoDecryptException("GA master key for " + username + " is null");
        }
        String masterKey;
        try {
            masterKey = cryptoService.decrypt(masterKeyEncrypt);
        } catch (DecryptException e) {
            throw new SsoDecryptException("decrypt error", e);
        }
        return googleAuthenticator.get().authorize(masterKey, verificationCode);
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
