package com.payneteasy.superfly.factorybean;

import com.payneteasy.http.client.api.IHttpClient;
import com.payneteasy.httpclient.contrib.ssl.JdkSslSocketFactoryBuilder;
import com.payneteasy.superfly.api.transport.ApacheHC5HttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * Provides the {@link IHttpClient} transport used to deliver Superfly notification callbacks.
 *
 * <p>Backed by {@link ApacheHC5HttpClient} (Apache HttpClient 5) with connection pooling and an
 * {@link AutoCloseable} lifecycle — Spring closes the bean on context shutdown. Per-request
 * timeouts are supplied by the caller (see the notification send strategies), so the client itself
 * only carries pool/SSL defaults.
 *
 * <p>mTLS is opt-in: set {@code superfly.notification.http.keystore-url} and/or
 * {@code superfly.notification.http.truststore-url} (plus passwords and optional
 * {@code keystore-type}, default PKCS12). Without them the JVM default SSL context is used.
 */
@Configuration
public class HttpClientSpringConfiguration {

    @Value("${superfly.notification.http.keystore-url:}")
    private String keyStoreUrl;

    @Value("${superfly.notification.http.keystore-password:}")
    private String keyStorePassword;

    @Value("${superfly.notification.http.truststore-url:}")
    private String trustStoreUrl;

    @Value("${superfly.notification.http.truststore-password:}")
    private String trustStorePassword;

    @Value("${superfly.notification.http.keystore-type:" + JdkSslSocketFactoryBuilder.DEFAULT_KEYSTORE_TYPE + "}")
    private String keyStoreType;

    @Bean
    public IHttpClient notificationHttpClient() throws Exception {
        ApacheHC5HttpClient.Builder builder = ApacheHC5HttpClient.builder();
        if (!keyStoreUrl.isBlank() || !trustStoreUrl.isBlank()) {
            builder.sslContext(JdkSslSocketFactoryBuilder.buildSslContext(
                    toUrl(keyStoreUrl), keyStorePassword,
                    toUrl(trustStoreUrl), trustStorePassword,
                    keyStoreType));
        }
        return builder.build();
    }

    private static URL toUrl(String value) throws MalformedURLException {
        return value.isBlank() ? null : URI.create(value).toURL();
    }
}
