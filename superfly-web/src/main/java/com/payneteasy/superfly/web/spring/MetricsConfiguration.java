package com.payneteasy.superfly.web.spring;

import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.api.metrics.MetricsSSOService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class MetricsConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(MetricsConfiguration.class);

    @Bean
    public MeterRegistry meterRegistry() {
        LOG.info("Creating SimpleMeterRegistry for SSO metrics");
        return new SimpleMeterRegistry();
    }

    /**
     * Wraps SSOServiceImpl with MetricsSSOService and registers it as @Primary
     * so RemoteApiController gets the instrumented version via type-based injection.
     *
     * Uses @Qualifier("ssoServiceImpl") + SSOService type (not the concrete class) because
     * Spring wraps SSOServiceImpl in a JDK dynamic proxy when AOP/transactions are active —
     * the proxy implements SSOService but is not an instance of the concrete class.
     * @Qualifier bypasses @Primary to avoid a circular dependency on MetricsSSOService itself.
     */
    @Bean
    @Primary
    public MetricsSSOService metricsSSOService(
            @Qualifier("ssoServiceImpl") SSOService ssoService,
            MeterRegistry meterRegistry) {
        LOG.info("MetricsSSOService registered as primary SSOService bean, wrapping {}",
                ssoService.getClass().getSimpleName());
        return new MetricsSSOService(ssoService, meterRegistry);
    }
}
