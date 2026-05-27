package com.payneteasy.superfly.web.wicket.page.monitoring;

import lombok.Value;

/**
 * Aggregated per-operation SSO metrics row for the Monitoring page table.
 */
@Value
public class OperationRow {
    /** Operation name (e.g. "authenticate", "checkOtp"). */
    String operation;
    /** Total SSO calls (success + error) for this operation. */
    long totalCalls;
    /** Total failed calls for this operation. */
    long errors;
    /** Error rate as a percentage string, e.g. "12.5". */
    String errorRatePct;
    /** Average latency in milliseconds, rounded. */
    long avgLatencyMs;
}
