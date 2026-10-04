package com.payneteasy.superfly.web.security.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.HexFormat;
import java.util.regex.Pattern;
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

    private static final Logger logger = LoggerFactory.getLogger(LoginAttemptLimiter.class);

    public static final int DEFAULT_MAX_FAILURES_PER_IP = 20;
    static final int  MAX_FAILURES_PER_IP_USER = 5;
    static final long WINDOW_MINUTES           = 5;
    // Bounds memory; when full, the least recently used counters are evicted first.
    static final long MAX_ENTRIES              = 100_000;

    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");

    // Wicket SSO pages and the security filter must share one instance; the pages are not wired by Spring in tests.
    private static volatile LoginAttemptLimiter shared = new LoginAttemptLimiter(DEFAULT_MAX_FAILURES_PER_IP);

    private final int                          maxFailuresPerIp;
    private final Cache<String, AtomicInteger> failures;

    /** Replaces the shared instance (also drops all counters). @param maxFailuresPerIp 0 disables the IP limit. */
    public static LoginAttemptLimiter install(int maxFailuresPerIp) {
        shared = new LoginAttemptLimiter(maxFailuresPerIp);
        return shared;
    }

    public static LoginAttemptLimiter shared() {
        return shared;
    }

    public LoginAttemptLimiter(int maxFailuresPerIp) {
        this(maxFailuresPerIp, Ticker.systemTicker());
    }

    LoginAttemptLimiter(int maxFailuresPerIp, Ticker ticker) {
        this.maxFailuresPerIp = maxFailuresPerIp;
        failures = Caffeine.newBuilder()
                .ticker(ticker)
                .expireAfterWrite(WINDOW_MINUTES, TimeUnit.MINUTES)
                .maximumSize(MAX_ENTRIES)
                .build();
    }

    public static long windowSeconds() {
        return TimeUnit.MINUTES.toSeconds(WINDOW_MINUTES);
    }

    /** Same as {@link #isBlocked}, plus a WARN; username is PII, so only a short hash of it is logged. */
    public boolean checkBlocked(String step, String ip, String username) {
        boolean blocked = isBlocked(step, ip, username);
        if (blocked) {
            logger.warn("Login rate limit exceeded: step={}, ip={}, user#={}", step, ip, fingerprint(username));
        }
        return blocked;
    }

    public boolean isBlocked(String step, String ip, String username) {
        return (maxFailuresPerIp > 0 && count(ipKey(step, ip)) >= maxFailuresPerIp)
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

    /**
     * Approximates the case- and accent-insensitive DB collation (utf8_general_ci), so that "Admin ", "ädmin" and
     * "admin" share a counter and the username cannot be varied to dodge the pair limit; blank means no username.
     * Characters without a decomposition are folded per code point through upper case, as general_ci does:
     * dotless "ı" and long "ſ" become "i"/"s"; "ß" equals "s" there, so it is mapped explicitly.
     */
    public static String normalizeUsername(String username) {
        if (username == null) {
            return null;
        }
        String stripped = DIACRITICS.matcher(Normalizer.normalize(username.trim(), Normalizer.Form.NFD)).replaceAll("");
        StringBuilder folded = new StringBuilder(stripped.length());
        stripped.codePoints().forEach(cp -> folded.appendCodePoint(
                cp == 'ß' ? 's' : Character.toLowerCase(Character.toUpperCase(cp))));
        return folded.length() == 0 ? null : folded.toString();
    }

    private static String fingerprint(String username) {
        if (username == null) {
            return "-";
        }
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(username.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash, 0, 4);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
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
