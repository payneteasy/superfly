package com.payneteasy.superfly.service.impl.remote.check;

import com.payneteasy.superfly.api.CheckOtpResult;
import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.model.SubsystemAuth;
import com.payneteasy.superfly.service.InternalSSOService;
import com.payneteasy.superfly.service.RemoteAuthCryptoService;
import com.payneteasy.superfly.service.RemoteAuthService;
import com.payneteasy.superfly.service.SubsystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import com.payneteasy.superfly.utils.SubsystemTokenHasher;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RemoteAuthServiceImpl implements RemoteAuthService {

    private static final Logger logger = LoggerFactory.getLogger(RemoteAuthServiceImpl.class);

    static final int MAX_OTP_ATTEMPTS = 3;
    static final int MAX_DECRYPTION_FAILURES_PER_MINUTE = 20;

    private final SubsystemService subsystemService;
    private final InternalSSOService internalSSOService;
    private final RemoteAuthCryptoService remoteAuthCryptoService;

    private final Cache<String, RemoteSession> sessionCache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build();

    // Decryption failures per subsystem: limits padding-oracle probing with a valid bearer token.
    private final Cache<String, AtomicInteger> decryptionFailures = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();

    public RemoteAuthServiceImpl(SubsystemService subsystemService,
                                 InternalSSOService internalSSOService,
                                 RemoteAuthCryptoService remoteAuthCryptoService) {
        this.subsystemService = subsystemService;
        this.internalSSOService = internalSSOService;
        this.remoteAuthCryptoService = remoteAuthCryptoService;
    }

    @Override
    public RemoteAuthSession checkPassword(String subsystemName, String username, String passwordEncrypted, String bearerToken, String ipAddress, String userAgent) throws RemoteAuthException {
        // 1. Validate Subsystem and Token
        SubsystemAuth subsystem = validateSubsystem(subsystemName, bearerToken);
        checkDecryptionFailureLimit(subsystemName);

        // 2. Decrypt Password
        String password;
        try {
             // the decrypted key lives only for this call
             password = remoteAuthCryptoService.decryptPassword(
                     passwordEncrypted,
                     subsystemService.getSubsystemPrivateKey(subsystemName),
                     RemoteAuthEncryptionAlgorithm.valueOf(subsystem.getEncryptionAlgorithm())
             );
        } catch (Exception e) {
            logger.warn("Failed to decrypt password: {}", e.getMessage());
            registerDecryptionFailure(subsystemName);
            throw new RemoteAuthException("Decryption failed", "BAD_REQUEST");
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
        // a configured OTP key makes OTP mandatory whatever the stored type and the optional flag say
        boolean keyConfigured = internalSSOService.hasOtpMasterKey(username);
        OTPType otpType = keyConfigured ? OTPType.GOOGLE_AUTH : ssoUser.getOtpType();
        sessionCache.put(sessionToken, new RemoteSession(subsystemName, username, otpType));

        boolean otpRequired = otpType != OTPType.NONE && (keyConfigured || !ssoUser.isOtpOptional());

        return new RemoteAuthSession(sessionToken, otpRequired);
    }

    @Override
    public String checkOtp(String subsystemName, String username, String otpEncrypted, String sessionToken, String bearerToken) throws RemoteAuthException {
        // 1. Validate Subsystem and Token
        SubsystemAuth subsystem = validateSubsystem(subsystemName, bearerToken);
        checkDecryptionFailureLimit(subsystemName);

        // 2. Validate Session Token. The session is taken out of the cache for the duration of the check:
        // a token is single-use on success and can't be used by concurrent requests to brute-force the OTP.
        RemoteSession session = sessionCache.asMap().remove(sessionToken);
        if (session == null) {
            throw new RemoteAuthException("Session expired or invalid", "BAD_USER_OR_PASSWORD_OR_OTP");
        }
        if (!session.subsystemName.equals(subsystemName) || !session.username.equals(username)) {
            throw new RemoteAuthException("Session does not match subsystem or username", "BAD_USER_OR_PASSWORD_OR_OTP");
        }

        // 3. Decrypt OTP
        String otp;
        try {
            otp = remoteAuthCryptoService.decryptOtp(
                    otpEncrypted,
                    subsystemService.getSubsystemPrivateKey(subsystemName),
                    RemoteAuthEncryptionAlgorithm.valueOf(subsystem.getEncryptionAlgorithm())
            );
        } catch (Exception e) {
            logger.warn("Failed to decrypt OTP: {}", e.getMessage());
            registerDecryptionFailure(subsystemName);
            returnAfterFailedAttempt(sessionToken, session);
            throw new RemoteAuthException("Decryption failed", "BAD_REQUEST");
        }

        // 4. Verify OTP
        // every failure reason maps to the same answer: the remote-auth contract does not expose them
        CheckOtpResult.Status otpStatus = internalSSOService.authenticateByOtpType(session.otpType, username, otp);
        if (otpStatus != CheckOtpResult.Status.SUCCESS) {
            returnAfterFailedAttempt(sessionToken, session);
            return "BAD_USER_OR_PASSWORD_OR_OTP";
        }
        return "SUCCESS";
    }

    private void checkDecryptionFailureLimit(String subsystemName) throws RemoteAuthException {
        AtomicInteger failures = decryptionFailures.getIfPresent(subsystemName);
        if (failures != null && failures.get() >= MAX_DECRYPTION_FAILURES_PER_MINUTE) {
            logger.warn("Decryption failure limit exceeded for subsystem {}", subsystemName);
            throw new RemoteAuthException("Decryption failed", "BAD_REQUEST");
        }
    }

    private void registerDecryptionFailure(String subsystemName) {
        decryptionFailures.get(subsystemName, k -> new AtomicInteger()).incrementAndGet();
    }

    private void returnAfterFailedAttempt(String sessionToken, RemoteSession session) {
        if (session.failedOtpAttempts + 1 < MAX_OTP_ATTEMPTS) {
            // put() restarts expireAfterWrite; the 5-minute window is still bounded by MAX_OTP_ATTEMPTS.
            sessionCache.put(sessionToken, session.withFailedOtpAttempt());
        } else {
            logger.warn("Remote auth session for user {} invalidated after {} failed OTP attempts",
                    session.username, MAX_OTP_ATTEMPTS);
        }
    }

    private SubsystemAuth validateSubsystem(String subsystemName, String bearerToken) throws RemoteAuthException {
        SubsystemAuth subsystem = subsystemService.getSubsystemAuth(subsystemName);
        // Same answer for unknown subsystem and wrong token: no subsystem enumeration.
        if (subsystem == null || !SubsystemTokenHasher.matches(bearerToken, subsystem.getSubsystemToken())) {
            throw new RemoteAuthException("Invalid subsystem token", "UNAUTHORIZED");
        }
        return subsystem;
    }

    private static class RemoteSession {
        final String  subsystemName;
        final String  username;
        final OTPType otpType;
        final int     failedOtpAttempts;

        RemoteSession(String subsystemName, String username, OTPType otpType) {
            this(subsystemName, username, otpType, 0);
        }

        private RemoteSession(String subsystemName, String username, OTPType otpType, int failedOtpAttempts) {
            this.subsystemName = subsystemName;
            this.username = username;
            this.otpType = otpType;
            this.failedOtpAttempts = failedOtpAttempts;
        }

        RemoteSession withFailedOtpAttempt() {
            return new RemoteSession(subsystemName, username, otpType, failedOtpAttempts + 1);
        }
    }
}
