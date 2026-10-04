package com.payneteasy.superfly.web.security.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;

import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Counts failed login attempts per client IP and per IP + username inside a fixed window, in memory of this node.
 * <p>
 * The pair limit (5) is below the account lockout threshold (6 wrong passwords), so a single IP is throttled
 * before it can lock someone else's account. The IP limit slows password spraying over many usernames.
 * Counters are kept per login step so that a successful password step does not reset the OTP counter.
 */
public class LoginAttemptLimiter {

    static final int  MAX_FAILURES_PER_IP      = 20;
    static final int  MAX_FAILURES_PER_IP_USER = 5;
    static final long WINDOW_MINUTES           = 5;
    // Bounds memory; when full, the least recently used counters are evicted first.
    static final long MAX_ENTRIES              = 100_000;

    private final Cache<String, AtomicInteger> failures;

    public LoginAttemptLimiter() {
        this(Ticker.systemTicker());
    }

    LoginAttemptLimiter(Ticker ticker) {
        failures = Caffeine.newBuilder()
                .ticker(ticker)
                .expireAfterWrite(WINDOW_MINUTES, TimeUnit.MINUTES)
                .maximumSize(MAX_ENTRIES)
                .build();
    }

    public static long windowSeconds() {
        return TimeUnit.MINUTES.toSeconds(WINDOW_MINUTES);
    }

    public boolean isBlocked(String step, String ip, String username) {
        return count(ipKey(step, ip)) >= MAX_FAILURES_PER_IP
                || (username != null && count(pairKey(step, ip, username)) >= MAX_FAILURES_PER_IP_USER);
    }

    public void recordFailure(String step, String ip, String username) {
        increment(ipKey(step, ip));
        if (username != null) {
            increment(pairKey(step, ip, username));
        }
    }

    /** Resets only the IP + username counter: otherwise one's own valid account would clear the IP counter. */
    public void recordSuccess(String step, String ip, String username) {
        if (username != null) {
            failures.invalidate(pairKey(step, ip, username));
        }
    }

    /** Lowercase and trim, so that "Admin " and "admin" share a counter; blank means no username. */
    public static String normalizeUsername(String username) {
        if (username == null) {
            return null;
        }
        String normalized = username.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    private int count(String key) {
        AtomicInteger counter = failures.getIfPresent(key);
        return counter == null ? 0 : counter.get();
    }

    private void increment(String key) {
        failures.get(key, k -> new AtomicInteger()).incrementAndGet();
    }

    private static String ipKey(String step, String ip) {
        return step + "|ip|" + ip;
    }

    private static String pairKey(String step, String ip, String username) {
        return step + "|pair|" + ip + "|" + username;
    }
}
