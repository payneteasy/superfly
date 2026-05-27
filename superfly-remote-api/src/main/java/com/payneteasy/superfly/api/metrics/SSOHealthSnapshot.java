package com.payneteasy.superfly.api.metrics;

import lombok.Value;

import java.time.Duration;
import java.time.Instant;

/**
 * Immutable point-in-time snapshot of SSO service health.
 *
 * <p>Used to expose SSO connectivity status to health-check endpoints
 * (e.g. Spring Boot Actuator {@code HealthIndicator}) without leaking
 * Micrometer types into the caller.
 *
 * <p>Example usage with Spring Boot Actuator:
 * <pre>{@code
 * @Component
 * public class SSOConnectionHealthIndicator implements HealthIndicator {
 *     private final MetricsSSOService metricsSSOService;
 *
 *     @Override
 *     public Health health() {
 *         SSOHealthSnapshot snap = metricsSSOService.getHealthSnapshot();
 *         return snap.isHealthy()
 *             ? Health.up().withDetail("lastSuccess", snap.getLastSuccessAt()).build()
 *             : Health.down().withDetail("totalErrors", snap.getTotalErrors()).build();
 *     }
 * }
 * }</pre>
 */
@Value
public class SSOHealthSnapshot {

    /**
     * Instant of the most recent successful SSO call, or {@code null} if no
     * successful call has been made since the service was created.
     */
    Instant lastSuccessAt;

    /** Total number of SSO calls attempted (successful + failed). */
    long totalCalls;

    /** Total number of SSO calls that ended with an exception. */
    long totalErrors;

    /**
     * Returns {@code true} when the SSO service is considered healthy:
     * <ul>
     *   <li>at least one successful call has been made, AND</li>
     *   <li>the last success was within the past 5 minutes, AND</li>
     *   <li>the error rate is below 50 % (or there have been no calls yet)</li>
     * </ul>
     */
    public boolean isHealthy() {
        if (lastSuccessAt == null) {
            return false;
        }
        boolean notStale = Duration.between(lastSuccessAt, Instant.now()).toMinutes() < 5;
        boolean lowErrorRate = totalCalls == 0
                || (double) totalErrors / totalCalls < 0.5;
        return notStale && lowErrorRate;
    }
}
