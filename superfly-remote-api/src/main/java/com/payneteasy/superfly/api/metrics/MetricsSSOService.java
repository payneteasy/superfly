package com.payneteasy.superfly.api.metrics;

import com.payneteasy.superfly.api.SSOEvent;
import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.api.SSOUserWithActions;
import com.payneteasy.superfly.api.UserDescription;
import com.payneteasy.superfly.api.UserNotFoundException;
import com.payneteasy.superfly.api.UserRegisterRequest;
import com.payneteasy.superfly.api.UserStatus;
import com.payneteasy.superfly.api.exceptions.BadPublicKeyException;
import com.payneteasy.superfly.api.exceptions.MessageSendException;
import com.payneteasy.superfly.api.exceptions.PolicyValidationException;
import com.payneteasy.superfly.api.exceptions.SsoDecryptException;
import com.payneteasy.superfly.api.exceptions.UserExistsException;
import com.payneteasy.superfly.api.request.ChangeTempPasswordRequest;
import com.payneteasy.superfly.api.request.ChangeUserRoleRequest;
import com.payneteasy.superfly.api.request.CheckOtpRequest;
import com.payneteasy.superfly.api.request.CompleteUserRequest;
import com.payneteasy.superfly.api.request.ExchangeSubsystemTokenRequest;
import com.payneteasy.superfly.api.request.GetEventsRequest;
import com.payneteasy.superfly.api.request.GetGoogleAuthQrCodeRequest;
import com.payneteasy.superfly.api.request.GetUserDescriptionRequest;
import com.payneteasy.superfly.api.request.GetUserStatusesRequest;
import com.payneteasy.superfly.api.request.GetUsersWithActionsRequest;
import com.payneteasy.superfly.api.request.HasOtpMasterKeyRequest;
import com.payneteasy.superfly.api.request.PasswordResetRequest;
import com.payneteasy.superfly.api.request.PseudoAuthenticateRequest;
import com.payneteasy.superfly.api.request.ResetGoogleAuthMasterKeyRequest;
import com.payneteasy.superfly.api.request.SendSystemDataRequest;
import com.payneteasy.superfly.api.request.TouchSessionsRequest;
import com.payneteasy.superfly.api.request.UpdateUserDescriptionRequest;
import com.payneteasy.superfly.api.request.UpdateUserIsOtpOptionalValueRequest;
import com.payneteasy.superfly.api.request.UpdateUserOtpTypeRequest;
import com.payneteasy.superfly.api.request.AuthenticateRequest;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Decorator for {@link SSOService} that records per-operation Micrometer metrics:
 * <ul>
 *   <li>{@code sso.call.duration{operation,status}} — Timer per operation (success/error)</li>
 *   <li>{@code sso.errors.total{operation}} — Counter of failed calls per operation</li>
 *   <li>{@code sso.last.success.epoch.ms} — Gauge: epoch-ms of the last successful call</li>
 * </ul>
 *
 * <p>The delegate is closed on {@link #close()} if it implements {@link AutoCloseable}.
 */
public class MetricsSSOService implements SSOService, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(MetricsSSOService.class);

    private static final String METRIC_DURATION = "sso.call.duration";
    private static final String METRIC_ERRORS   = "sso.errors.total";
    private static final String METRIC_LAST_OK  = "sso.last.success.epoch.ms";

    private static final String TAG_OPERATION = "operation";
    private static final String TAG_STATUS    = "status";

    private final SSOService   delegate;
    private final MeterRegistry registry;

    /** Epoch-ms of the most recent successful SSO call; 0 if none yet. */
    private final AtomicLong lastSuccessEpochMs = new AtomicLong(0L);

    /** Total number of SSO calls attempted (success + error). */
    private final AtomicLong totalCalls = new AtomicLong(0L);

    /** Total number of SSO calls that threw an exception. */
    private final AtomicLong totalErrors = new AtomicLong(0L);

    /**
     * @param delegate non-null SSOService to instrument
     * @param registry non-null MeterRegistry to publish metrics to
     */
    public MetricsSSOService(SSOService delegate, MeterRegistry registry) {
        this.delegate = delegate;
        this.registry = registry;

        Gauge.builder(METRIC_LAST_OK, lastSuccessEpochMs, AtomicLong::get)
                .description("Epoch-milliseconds of the last successful SSO call")
                .register(registry);

        LOG.info("MetricsSSOService initialized with registry {}", registry.getClass().getSimpleName());
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Records timing and counters for a completed SSO call.
     *
     * @param operation operation name (used as metric tag)
     * @param sample    Timer sample started at the beginning of the call
     * @param success   {@code true} if the call succeeded without exception
     */
    private void recordCompletion(String operation, Timer.Sample sample, boolean success) {
        String status = success ? "success" : "error";
        sample.stop(
                Timer.builder(METRIC_DURATION)
                        .tag(TAG_OPERATION, operation)
                        .tag(TAG_STATUS, status)
                        .register(registry)
        );
        totalCalls.incrementAndGet();
        if (success) {
            lastSuccessEpochMs.set(System.currentTimeMillis());
            LOG.debug("SSO {} completed in {}", operation, status);
        } else {
            registry.counter(METRIC_ERRORS, TAG_OPERATION, operation).increment();
            totalErrors.incrementAndGet();
            LOG.debug("SSO {} failed", operation);
        }
    }

    // -------------------------------------------------------------------------
    // Health snapshot
    // -------------------------------------------------------------------------

    /**
     * Returns a point-in-time health snapshot based on accumulated counters.
     */
    public SSOHealthSnapshot getHealthSnapshot() {
        long calls  = totalCalls.get();
        long errors = totalErrors.get();
        long lastMs = lastSuccessEpochMs.get();
        return new SSOHealthSnapshot(
                lastMs == 0L ? null : Instant.ofEpochMilli(lastMs),
                calls,
                errors
        );
    }

    // -------------------------------------------------------------------------
    // SSOService implementation — each method follows the same pattern:
    //   1. Timer.start
    //   2. try { ... ok=true; return result } finally { recordCompletion }
    // -------------------------------------------------------------------------

    @Override
    public SSOUser authenticate(AuthenticateRequest authenticate) {
        boolean ok = false;
        Timer.Sample sample = Timer.start(registry);
        try {
            SSOUser result = delegate.authenticate(authenticate);
            ok = true;
            return result;
        } finally {
            recordCompletion("authenticate", sample, ok);
        }
    }

    @Override
    public boolean checkOtp(CheckOtpRequest request) throws SsoDecryptException {
        boolean ok = false;
        Timer.Sample sample = Timer.start(registry);
        try {
            boolean result = delegate.checkOtp(request);
            ok = true;
            return result;
        } finally {
            recordCompletion("checkOtp", sample, ok);
        }
    }

    @Override
    public boolean hasOtpMasterKey(HasOtpMasterKeyRequest request) {
        boolean ok = false;
        Timer.Sample sample = Timer.start(registry);
        try {
            boolean result = delegate.hasOtpMasterKey(request);
            ok = true;
            return result;
        } finally {
            recordCompletion("hasOtpMasterKey", sample, ok);
        }
    }

    @Override
    public SSOUser pseudoAuthenticate(PseudoAuthenticateRequest request) {
        boolean ok = false;
        Timer.Sample sample = Timer.start(registry);
        try {
            SSOUser result = delegate.pseudoAuthenticate(request);
            ok = true;
            return result;
        } finally {
            recordCompletion("pseudoAuthenticate", sample, ok);
        }
    }

    @Override
    public void sendSystemData(SendSystemDataRequest request) {
        boolean ok = false;
        Timer.Sample sample = Timer.start(registry);
        try {
            delegate.sendSystemData(request);
            ok = true;
        } finally {
            recordCompletion("sendSystemData", sample, ok);
        }
    }

    @Override
    public List<SSOUserWithActions> getUsersWithActions(GetUsersWithActionsRequest request) {
        boolean ok = false;
        Timer.Sample sample = Timer.start(registry);
        try {
            List<SSOUserWithActions> result = delegate.getUsersWithActions(request);
            ok = true;
            return result;
        } finally {
            recordCompletion("getUsersWithActions", sample, ok);
        }
    }

    @Override
    public void updateUserOtpType(UpdateUserOtpTypeRequest request) {
        boolean ok = false;
        Timer.Sample sample = Timer.start(registry);
        try {
            delegate.updateUserOtpType(request);
            ok = true;
        } finally {
            recordCompletion("updateUserOtpType", sample, ok);
        }
    }

    @Override
    public void registerUser(UserRegisterRequest request)
            throws UserExistsException, PolicyValidationException,
                   BadPublicKeyException, MessageSendException {
        boolean ok = false;
        Timer.Sample sample = Timer.start(registry);
        try {
            delegate.registerUser(request);
            ok = true;
        } finally {
            recordCompletion("registerUser", sample, ok);
        }
    }

    @Override
    public void changeTempPassword(ChangeTempPasswordRequest request) throws PolicyValidationException {
        boolean ok = false;
        Timer.Sample sample = Timer.start(registry);
        try {
            delegate.changeTempPassword(request);
            ok = true;
        } finally {
            recordCompletion("changeTempPassword", sample, ok);
        }
    }

    @Override
    public UserDescription getUserDescription(GetUserDescriptionRequest request) {
        boolean ok = false;
        Timer.Sample sample = Timer.start(registry);
        try {
            UserDescription result = delegate.getUserDescription(request);
            ok = true;
            return result;
        } finally {
            recordCompletion("getUserDescription", sample, ok);
        }
    }

    @Override
    public String resetGoogleAuthMasterKey(ResetGoogleAuthMasterKeyRequest request)
            throws UserNotFoundException, SsoDecryptException {
        boolean ok = false;
        Timer.Sample sample = Timer.start(registry);
        try {
            String result = delegate.resetGoogleAuthMasterKey(request);
            ok = true;
            return result;
        } finally {
            recordCompletion("resetGoogleAuthMasterKey", sample, ok);
        }
    }

    @Override
    public String getUrlToGoogleAuthQrCode(GetGoogleAuthQrCodeRequest request) {
        boolean ok = false;
        Timer.Sample sample = Timer.start(registry);
        try {
            String result = delegate.getUrlToGoogleAuthQrCode(request);
            ok = true;
            return result;
        } finally {
            recordCompletion("getUrlToGoogleAuthQrCode", sample, ok);
        }
    }

    @Override
    public void updateUserIsOtpOptionalValue(UpdateUserIsOtpOptionalValueRequest request) {
        boolean ok = false;
        Timer.Sample sample = Timer.start(registry);
        try {
            delegate.updateUserIsOtpOptionalValue(request);
            ok = true;
        } finally {
            recordCompletion("updateUserIsOtpOptionalValue", sample, ok);
        }
    }

    @Override
    public void updateUserDescription(UpdateUserDescriptionRequest request)
            throws UserNotFoundException, BadPublicKeyException {
        boolean ok = false;
        Timer.Sample sample = Timer.start(registry);
        try {
            delegate.updateUserDescription(request);
            ok = true;
        } finally {
            recordCompletion("updateUserDescription", sample, ok);
        }
    }

    @Override
    public void resetPassword(PasswordResetRequest reset)
            throws UserNotFoundException, PolicyValidationException {
        boolean ok = false;
        Timer.Sample sample = Timer.start(registry);
        try {
            delegate.resetPassword(reset);
            ok = true;
        } finally {
            recordCompletion("resetPassword", sample, ok);
        }
    }

    @Override
    public List<UserStatus> getUserStatuses(GetUserStatusesRequest request) {
        boolean ok = false;
        Timer.Sample sample = Timer.start(registry);
        try {
            List<UserStatus> result = delegate.getUserStatuses(request);
            ok = true;
            return result;
        } finally {
            recordCompletion("getUserStatuses", sample, ok);
        }
    }

    @Override
    public SSOUser exchangeSubsystemToken(ExchangeSubsystemTokenRequest request) {
        boolean ok = false;
        Timer.Sample sample = Timer.start(registry);
        try {
            SSOUser result = delegate.exchangeSubsystemToken(request);
            ok = true;
            return result;
        } finally {
            recordCompletion("exchangeSubsystemToken", sample, ok);
        }
    }

    @Override
    public void touchSessions(TouchSessionsRequest request) {
        boolean ok = false;
        Timer.Sample sample = Timer.start(registry);
        try {
            delegate.touchSessions(request);
            ok = true;
        } finally {
            recordCompletion("touchSessions", sample, ok);
        }
    }

    @Override
    public void completeUser(CompleteUserRequest request) {
        boolean ok = false;
        Timer.Sample sample = Timer.start(registry);
        try {
            delegate.completeUser(request);
            ok = true;
        } finally {
            recordCompletion("completeUser", sample, ok);
        }
    }

    @Override
    public void changeUserRole(ChangeUserRoleRequest request) {
        boolean ok = false;
        Timer.Sample sample = Timer.start(registry);
        try {
            delegate.changeUserRole(request);
            ok = true;
        } finally {
            recordCompletion("changeUserRole", sample, ok);
        }
    }

    @Override
    public List<SSOEvent> getEvents(GetEventsRequest request) {
        boolean ok = false;
        Timer.Sample sample = Timer.start(registry);
        try {
            List<SSOEvent> result = delegate.getEvents(request);
            ok = true;
            return result;
        } finally {
            recordCompletion("getEvents", sample, ok);
        }
    }

    // -------------------------------------------------------------------------
    // AutoCloseable
    // -------------------------------------------------------------------------

    /**
     * Closes the underlying delegate if it implements {@link AutoCloseable}.
     *
     * @throws Exception if the delegate's close method throws
     */
    @Override
    public void close() throws Exception {
        if (delegate instanceof AutoCloseable) {
            LOG.debug("MetricsSSOService: closing delegate {}", delegate.getClass().getSimpleName());
            ((AutoCloseable) delegate).close();
        } else {
            LOG.debug("MetricsSSOService: delegate {} is not AutoCloseable, nothing to close",
                    delegate.getClass().getSimpleName());
        }
    }
}
