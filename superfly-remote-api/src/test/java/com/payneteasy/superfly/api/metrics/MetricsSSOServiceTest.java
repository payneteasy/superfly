package com.payneteasy.superfly.api.metrics;

import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.api.request.AuthenticateRequest;

import java.util.Collections;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link MetricsSSOService}.
 *
 * <p>Uses {@link SimpleMeterRegistry} for in-memory metric inspection and
 * EasyMock to stub/verify the delegate {@link SSOService}.
 */
public class MetricsSSOServiceTest {

    /** Combined interface so EasyMock can produce a mock that is both SSOService and AutoCloseable. */
    private interface CloseableSSOService extends SSOService, AutoCloseable {}

    private SimpleMeterRegistry registry;
    private SSOService          delegate;

    @Before
    public void setUp() {
        registry = new SimpleMeterRegistry();
        delegate = EasyMock.createMock(SSOService.class);
    }

    @After
    public void tearDown() {
        registry.close();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private MetricsSSOService decorated() {
        return new MetricsSSOService(delegate, registry);
    }

    private AuthenticateRequest anyAuthRequest() {
        return new AuthenticateRequest("user", "pass");
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    /**
     * After a successful authenticate call the timer must contain exactly one
     * successful sample with tags {operation=authenticate, status=success}.
     */
    @Test
    public void testTimerRecordedOnSuccess() {
        SSOUser user = new SSOUser("test", Collections.emptyMap(), Collections.emptyMap());
        expect(delegate.authenticate(anyObject())).andReturn(user);
        replay(delegate);

        MetricsSSOService svc = decorated();
        svc.authenticate(anyAuthRequest());

        Timer timer = registry.find("sso.call.duration")
                .tags("operation", "authenticate", "status", "success")
                .timer();
        assertNotNull("Timer sso.call.duration{operation=authenticate, status=success} must exist", timer);
        assertEquals("Timer count must be 1", 1, timer.count());

        verify(delegate);
    }

    /**
     * After a failing authenticate call the error counter must be 1 and the
     * original exception must be re-thrown.
     */
    @Test
    public void testErrorCounterOnException() {
        RuntimeException boom = new RuntimeException("SSO is down");
        expect(delegate.authenticate(anyObject())).andThrow(boom);
        replay(delegate);

        MetricsSSOService svc = decorated();
        try {
            svc.authenticate(anyAuthRequest());
            fail("Expected RuntimeException to be rethrown");
        } catch (RuntimeException e) {
            assertSame("Original exception must be rethrown", boom, e);
        }

        Counter errorCounter = registry.find("sso.errors.total")
                .tags("operation", "authenticate")
                .counter();
        assertNotNull("Error counter sso.errors.total{operation=authenticate} must exist", errorCounter);
        assertEquals("Error counter must be 1", 1.0, errorCounter.count(), 0.001);

        verify(delegate);
    }

    /**
     * After a successful call {@code getHealthSnapshot().getLastSuccessAt()} must not be null.
     */
    @Test
    public void testLastSuccessUpdatedAfterCall() {
        expect(delegate.authenticate(anyObject())).andReturn(new SSOUser("test", Collections.emptyMap(), Collections.emptyMap()));
        replay(delegate);

        MetricsSSOService svc = decorated();
        assertNull("lastSuccessAt should be null before any call", svc.getHealthSnapshot().getLastSuccessAt());

        svc.authenticate(anyAuthRequest());
        assertNotNull("lastSuccessAt must be set after a successful call",
                svc.getHealthSnapshot().getLastSuccessAt());

        verify(delegate);
    }

    /**
     * After a recent successful call {@code isHealthy()} must return {@code true}.
     */
    @Test
    public void testHealthySnapshotAfterSuccess() {
        expect(delegate.authenticate(anyObject())).andReturn(new SSOUser("test", Collections.emptyMap(), Collections.emptyMap()));
        replay(delegate);

        MetricsSSOService svc = decorated();
        svc.authenticate(anyAuthRequest());

        assertTrue("isHealthy() must be true after a recent successful call",
                svc.getHealthSnapshot().isHealthy());

        verify(delegate);
    }

    /**
     * When all calls fail the snapshot must be unhealthy (error rate = 100 %).
     */
    @Test
    public void testUnhealthySnapshotAfterAllErrors() {
        // 5 error calls
        RuntimeException err = new RuntimeException("timeout");
        expect(delegate.authenticate(anyObject())).andThrow(err).times(5);
        replay(delegate);

        MetricsSSOService svc = decorated();
        for (int i = 0; i < 5; i++) {
            try {
                svc.authenticate(anyAuthRequest());
            } catch (RuntimeException ignored) {
                // expected
            }
        }

        SSOHealthSnapshot snap = svc.getHealthSnapshot();
        assertEquals("totalCalls must be 5", 5, snap.getTotalCalls());
        assertEquals("totalErrors must be 5", 5, snap.getTotalErrors());
        assertFalse("isHealthy() must be false when all calls failed", snap.isHealthy());

        verify(delegate);
    }

    /**
     * {@code close()} must delegate to the underlying service when it implements {@link AutoCloseable}.
     */
    @Test
    public void testCloseDelegatesIfAutoCloseable() throws Exception {
        CloseableSSOService closeableDelegate = EasyMock.createMock(CloseableSSOService.class);
        closeableDelegate.close();
        expectLastCall().once();
        replay(closeableDelegate);

        MetricsSSOService svc = new MetricsSSOService(closeableDelegate, registry);
        svc.close();

        verify(closeableDelegate);
    }

    /**
     * {@code close()} must be a no-op (no exception) when the delegate does not implement
     * {@link AutoCloseable}.
     */
    @Test
    public void testCloseNoopIfNotAutoCloseable() throws Exception {
        // 'delegate' is a plain SSOService mock (no AutoCloseable)
        replay(delegate);

        MetricsSSOService svc = new MetricsSSOService(delegate, registry);
        // must not throw
        svc.close();

        verify(delegate);
    }
}
