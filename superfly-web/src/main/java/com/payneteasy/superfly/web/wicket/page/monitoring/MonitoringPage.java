package com.payneteasy.superfly.web.wicket.page.monitoring;

import com.payneteasy.superfly.api.metrics.MetricsSSOService;
import com.payneteasy.superfly.api.metrics.SSOHealthSnapshot;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;

import com.payneteasy.superfly.web.wicket.page.BasePage;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Admin monitoring page — shows SSO service health snapshot and per-operation metrics.
 * Accessible only to ROLE_ADMIN.
 */
@Secured("ROLE_ADMIN")
public class MonitoringPage extends BasePage {

    private static final Logger LOG = LoggerFactory.getLogger(MonitoringPage.class);

    private static final DateTimeFormatter INSTANT_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    @SpringBean
    private MetricsSSOService metricsSSOService;

    @SpringBean
    private MeterRegistry meterRegistry;

    public MonitoringPage() {
        super(MonitoringPage.class);

        LOG.debug("MonitoringPage: loading health snapshot and metrics");

        SSOHealthSnapshot snap = metricsSSOService.getHealthSnapshot();
        boolean healthy = snap.isHealthy();

        // --- Health badge ---
        Label healthLabel = new Label("health-status", healthy ? "HEALTHY" : "UNHEALTHY");
        healthLabel.add(AttributeModifier.replace("class",
                Model.of(healthy
                        ? "badge badge-success"
                        : "badge badge-danger")));
        add(healthLabel);

        // --- Last success timestamp ---
        String lastSuccess = snap.getLastSuccessAt() != null
                ? INSTANT_FMT.format(snap.getLastSuccessAt())
                : "—";
        add(new Label("last-success", lastSuccess));

        // --- Per-operation table ---
        List<OperationRow> rows = buildOperationRows();
        LOG.debug("MonitoringPage: built {} operation rows from registry", rows.size());

        add(new ListView<>("operations", rows) {
            @Override
            protected void populateItem(ListItem<OperationRow> item) {
                OperationRow row = item.getModelObject();
                item.add(new Label("op-name",      row.getOperation()));
                item.add(new Label("op-calls",     String.valueOf(row.getTotalCalls())));
                item.add(new Label("op-errors",    String.valueOf(row.getErrors())));
                item.add(new Label("op-error-pct", row.getErrorRatePct() + "%"));
                item.add(new Label("op-avg-ms",    row.getAvgLatencyMs() + " ms"));
            }
        });
    }

    /**
     * Scans the MeterRegistry for all {@code sso.call.duration} timers and
     * aggregates them by operation tag into a sorted list of {@link OperationRow}.
     *
     * <p>Each operation may have two timers: status=success and status=error.
     * We sum counts and weighted latencies to compute per-operation totals.
     */
    private List<OperationRow> buildOperationRows() {
        Collection<Timer> timers = meterRegistry.find("sso.call.duration").timers();
        LOG.debug("MonitoringPage: found {} sso.call.duration timers in registry", timers.size());

        // operation → [totalCount, errorCount, totalDurationNs]
        Map<String, long[]> aggMap = new LinkedHashMap<>();

        for (Timer timer : timers) {
            String operation = timer.getId().getTag("operation");
            String status    = timer.getId().getTag("status");
            if (operation == null) {
                continue;
            }

            long[] agg = aggMap.computeIfAbsent(operation, k -> new long[3]);
            long count       = timer.count();
            long durationNs  = (long) timer.totalTime(TimeUnit.NANOSECONDS);

            agg[0] += count;           // totalCalls
            if ("error".equals(status)) {
                agg[1] += count;       // errorCalls
            }
            agg[2] += durationNs;      // totalDurationNs
        }

        List<OperationRow> rows = new ArrayList<>(aggMap.size());
        for (Map.Entry<String, long[]> entry : aggMap.entrySet()) {
            String op        = entry.getKey();
            long[] agg       = entry.getValue();
            long totalCalls  = agg[0];
            long errors      = agg[1];
            long durationNs  = agg[2];

            String errorRatePct = totalCalls == 0
                    ? "0.0"
                    : String.format("%.1f", 100.0 * errors / totalCalls);

            long avgLatencyMs = totalCalls == 0
                    ? 0L
                    : TimeUnit.NANOSECONDS.toMillis(durationNs / totalCalls);

            rows.add(new OperationRow(op, totalCalls, errors, errorRatePct, avgLatencyMs));
        }

        return rows;
    }

    @Override
    protected String getTitle() {
        return "Monitoring";
    }
}
