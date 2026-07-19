package com.payneteasy.superfly.factorybean;

import com.payneteasy.http.client.api.IHttpClient;
import com.payneteasy.superfly.api.transport.ApacheHC5HttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides the {@link IHttpClient} transport used to deliver Superfly notification callbacks.
 *
 * <p>Backed by {@link ApacheHC5HttpClient} (Apache HttpClient 5) with connection pooling and an
 * {@link AutoCloseable} lifecycle — Spring closes the bean on context shutdown. Per-request
 * timeouts are supplied by the caller (see the notification send strategies), so the client itself
 * only carries pool/SSL defaults.
 */
@Configuration
public class HttpClientSpringConfiguration {

    @Bean
    public IHttpClient notificationHttpClient() {
        return ApacheHC5HttpClient.builder().build();
    }
}
