package com.payneteasy.superfly.service.impl.remote.check;

import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.service.InternalSSOService;
import com.payneteasy.superfly.service.RemoteAuthCryptoService;
import com.payneteasy.superfly.service.RemoteAuthService;
import com.payneteasy.superfly.service.SubsystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class RemoteAuthServiceImpl implements RemoteAuthService {

    private static final Logger logger = LoggerFactory.getLogger(RemoteAuthServiceImpl.class);

    private final SubsystemService subsystemService;
    private final InternalSSOService internalSSOService;
    private final RemoteAuthCryptoService remoteAuthCryptoService;

    private final Cache<String, RemoteSession> sessionCache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build();

    public RemoteAuthServiceImpl(SubsystemService subsystemService,
                                 InternalSSOService internalSSOService,
                                 RemoteAuthCryptoService remoteAuthCryptoService) {
        this.subsystemService = subsystemService;
        this.internalSSOService = internalSSOService;
        this.remoteAuthCryptoService = new RemoteAuthCryptoServiceStub();
    }

    @Override
    public RemoteAuthSession checkPassword(String subsystemName, String username, String passwordEncrypted, String bearerToken, String ipAddress, String userAgent) throws RemoteAuthException {
        // 1. Validate Subsystem and Token
        UISubsystem subsystem = validateSubsystem(subsystemName, bearerToken);

        // 2. Decrypt Password
        String password;
        try {
             password = remoteAuthCryptoService.decryptPassword(
                     passwordEncrypted,
                     subsystem.getPrivateKey(),
                     RemoteAuthEncryptionAlgorithm.valueOf(subsystem.getEncryptionAlgorithm())
             );
        } catch (Exception e) {
            logger.error("Failed to decrypt password", e);
            throw new RemoteAuthException("Decryption failed", "INTERNAL_ERROR", e);
        }

        // 3. Authenticate User
        // Using internalSSOService to check credentials and get SSOUser
        // Note: This creates a session in the DB as well (AuthSession).
        // If we want to avoid creating a full session until OTP, we might need a different method,
        // but for now we reuse the existing logic.
        SSOUser ssoUser = internalSSOService.authenticate(username, password, subsystemName, ipAddress, userAgent);

        if (ssoUser == null) {
            // Could be bad password, user blocked, etc.
            // For security, we might return generic bad credentials.
            throw new RemoteAuthException("Authentication failed", "BAD_USER_OR_PASSWORD_OR_OTP");
        }

        if (ssoUser.hasAction("action_temp_password")) {
            throw new RemoteAuthException("User should change password", "USER_SHOULD_CHANGE_PASSWORD");
        }

        // 4. Generate Session Token and Cache
        String sessionToken = UUID.randomUUID().toString();
        sessionCache.put(sessionToken, new RemoteSession(username, ssoUser.getOtpType()));

        boolean otpRequired = ssoUser.getOtpType() != OTPType.NONE && !ssoUser.isOtpOptional();

        return new RemoteAuthSession(sessionToken, otpRequired);
    }

    @Override
    public String checkOtp(String subsystemName, String username, String otpEncrypted, String sessionToken, String bearerToken) throws RemoteAuthException {
        // 1. Validate Subsystem and Token
        UISubsystem subsystem = validateSubsystem(subsystemName, bearerToken);

        // 2. Validate Session Token
        RemoteSession session = sessionCache.getIfPresent(sessionToken);
        if (session == null) {
            throw new RemoteAuthException("Session expired or invalid", "BAD_USER_OR_PASSWORD_OR_OTP");
        }
        if (!session.username.equals(username)) {
             throw new RemoteAuthException("Username mismatch", "BAD_USER_OR_PASSWORD_OR_OTP");
        }

        // 3. Decrypt OTP
        String otp;
        try {
            otp = remoteAuthCryptoService.decryptOtp(
                    otpEncrypted,
                    subsystem.getPrivateKey(),
                    RemoteAuthEncryptionAlgorithm.valueOf(subsystem.getEncryptionAlgorithm())
            );
        } catch (Exception e) {
             logger.error("Failed to decrypt OTP", e);
             throw new RemoteAuthException("Decryption failed", "INTERNAL_ERROR", e);
        }

        // 4. Verify OTP
        boolean otpValid = internalSSOService.authenticateByOtpType(session.otpType, username, otp);
        if (!otpValid) {
             return "BAD_USER_OR_PASSWORD_OR_OTP";
        }
        return "SUCCESS";
    }

    private UISubsystem validateSubsystem(String subsystemName, String bearerToken) throws RemoteAuthException {
        UISubsystem subsystem = subsystemService.getSubsystemByName(subsystemName);
        if (subsystem == null) {
            throw new RemoteAuthException("Subsystem not found", "INTERNAL_ERROR"); // Or 404 equivalent
        }
        // Assuming bearerToken is just the token value.
        // The format in header is "Bearer <token>", but the controller should extract <token>.
        if (subsystem.getSubsystemToken() == null || !subsystem.getSubsystemToken().equals(bearerToken)) {
            throw new RemoteAuthException("Invalid subsystem token", "INTERNAL_ERROR"); // Or 401
        }
        return subsystem;
    }

    private static class RemoteSession {
        final String  username;
        final OTPType otpType;

        RemoteSession(String username, OTPType otpType) {
            this.username = username;
            this.otpType = otpType;
        }
    }
}

