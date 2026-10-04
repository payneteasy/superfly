package com.payneteasy.superfly.web.security.ratelimit;

import com.github.benmanes.caffeine.cache.Ticker;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class LoginAttemptLimiterTest {

    private final AtomicLong          nanos   = new AtomicLong();
    private final Ticker              ticker  = nanos::get;
    private final LoginAttemptLimiter limiter = new LoginAttemptLimiter(LoginAttemptLimiter.DEFAULT_MAX_FAILURES_PER_IP, ticker);

    @Test
    public void pairIsBlockedBeforeIp() {
        for (int i = 0; i < LoginAttemptLimiter.MAX_FAILURES_PER_IP_USER; i++) {
            assertFalse(limiter.isBlocked("password", "1.1.1.1", "alice"));
            limiter.recordFailure("password", "1.1.1.1", "alice");
        }
        assertTrue(limiter.isBlocked("password", "1.1.1.1", "alice"));
        assertFalse("other user from the same IP is not affected", limiter.isBlocked("password", "1.1.1.1", "bob"));
        assertFalse("same user from another IP is not affected", limiter.isBlocked("password", "2.2.2.2", "alice"));
    }

    @Test
    public void ipIsBlockedAfterSprayingManyUsernames() {
        for (int i = 0; i < LoginAttemptLimiter.DEFAULT_MAX_FAILURES_PER_IP; i++) {
            limiter.recordFailure("password", "1.1.1.1", "user" + i);
        }
        assertTrue(limiter.isBlocked("password", "1.1.1.1", "brand-new-user"));
        assertFalse(limiter.isBlocked("password", "2.2.2.2", "brand-new-user"));
    }

    @Test
    public void successResetsOnlyThePair() {
        for (int i = 0; i < LoginAttemptLimiter.MAX_FAILURES_PER_IP_USER; i++) {
            limiter.recordFailure("password", "1.1.1.1", "alice");
        }
        limiter.recordSuccess("password", "1.1.1.1", "alice");
        assertFalse(limiter.isBlocked("password", "1.1.1.1", "alice"));
    }

    @Test
    public void stepsHaveSeparateCounters() {
        for (int i = 0; i < LoginAttemptLimiter.MAX_FAILURES_PER_IP_USER; i++) {
            limiter.recordFailure("otp", "1.1.1.1", "alice");
        }
        limiter.recordSuccess("password", "1.1.1.1", "alice");
        assertTrue(limiter.isBlocked("otp", "1.1.1.1", "alice"));
    }

    @Test
    public void windowExpires() {
        for (int i = 0; i < LoginAttemptLimiter.DEFAULT_MAX_FAILURES_PER_IP; i++) {
            limiter.recordFailure("password", "1.1.1.1", "alice");
        }
        assertTrue(limiter.isBlocked("password", "1.1.1.1", "alice"));
        nanos.addAndGet(TimeUnit.MINUTES.toNanos(LoginAttemptLimiter.WINDOW_MINUTES) + 1);
        assertFalse(limiter.isBlocked("password", "1.1.1.1", "alice"));
    }

    @Test
    public void usernameIsNormalized() {
        assertEquals("alice", LoginAttemptLimiter.normalizeUsername("  Alice "));
        assertEquals("admin", LoginAttemptLimiter.normalizeUsername("ädmin"));
        assertEquals("admin", LoginAttemptLimiter.normalizeUsername("ADMIN "));
        // utf8_general_ci treats dotless i as I, long s as S and sharp s as s
        assertEquals("admin", LoginAttemptLimiter.normalizeUsername("admın"));
        assertEquals("sam", LoginAttemptLimiter.normalizeUsername("ſam"));
        assertEquals("adsmin", LoginAttemptLimiter.normalizeUsername("adßmin"));
        assertNull(LoginAttemptLimiter.normalizeUsername("   "));
        assertNull(LoginAttemptLimiter.normalizeUsername(null));
    }

    @Test
    public void zeroDisablesIpLimitButNotPairLimit() {
        LoginAttemptLimiter noIpLimit = new LoginAttemptLimiter(0);
        for (int i = 0; i < 100; i++) {
            noIpLimit.recordFailure("password", "1.1.1.1", "user" + i);
        }
        assertFalse(noIpLimit.isBlocked("password", "1.1.1.1", "fresh"));
        for (int i = 0; i < LoginAttemptLimiter.MAX_FAILURES_PER_IP_USER; i++) {
            noIpLimit.recordFailure("password", "1.1.1.1", "alice");
        }
        assertTrue(noIpLimit.isBlocked("password", "1.1.1.1", "alice"));
    }
}
