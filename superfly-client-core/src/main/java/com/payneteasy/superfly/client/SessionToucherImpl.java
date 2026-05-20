package com.payneteasy.superfly.client;

import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.api.request.TouchSessionsRequest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Default session toucher implementation. Accumulates IDs of sessions
 * and sends them in bulks every minute (by default).
 * Call {@link #startup()} after setting SSOService (e.g. from adapter or Spring @PostConstruct).
 * Call {@link #shutdown()} on context destroy (e.g. from adapter or Spring @PreDestroy).
 *
 * @author Roman Puchkovskiy
 */
public class SessionToucherImpl implements SessionToucher {
    private boolean enabled = true;
    private SSOService ssoService;
    private final int flushPeriodInSeconds;

    private final Queue<Long> queue = new ConcurrentLinkedQueue<>();
    private Timer timer;

    public SessionToucherImpl() {
        this(60); // every minute
    }

    public SessionToucherImpl(int flushPeriodInSeconds) {
        this.flushPeriodInSeconds = flushPeriodInSeconds;
    }

    public void setSsoService(SSOService ssoService) {
        this.ssoService = ssoService;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Starts the toucher timer. Call after dependency injection (e.g. from Jakarta @PostConstruct or Spring).
     */
    public void startup() {
        if (ssoService == null) {
            throw new IllegalStateException("ssoService not set");
        }
        if (enabled) {
            timer = new Timer("session-toucher-timer");
            timer.scheduleAtFixedRate(
                    new ToucherTask(),
                    getFlushPeriodInMillis(),
                    getFlushPeriodInMillis()
            );
        }
    }

    private long getFlushPeriodInMillis() {
        return flushPeriodInSeconds * 1000L;
    }

    /**
     * Stops the toucher timer. Call on context destroy (e.g. from Jakarta @PreDestroy or Spring).
     */
    public void shutdown() {
        if (timer != null) {
            timer.cancel();
        }
    }

    @Override
    public void addSessionId(long sessionId) {
        if (enabled) {
            queue.add(sessionId);
        }
    }

    private class ToucherTask extends TimerTask {
        @Override
        public void run() {
            Set<Long> sessionIds = new HashSet<>();
            Long sessionId;
            do {
                sessionId = queue.poll();
                if (sessionId != null) {
                    sessionIds.add(sessionId);
                }
            } while (sessionId != null);
            if (!sessionIds.isEmpty()) {
                ssoService.touchSessions(TouchSessionsRequest.builder().sessionIds(new ArrayList<>(sessionIds)).build());
            }
        }
    }
}
