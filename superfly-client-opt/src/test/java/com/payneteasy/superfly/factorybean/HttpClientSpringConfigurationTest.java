package com.payneteasy.superfly.factorybean;

import com.payneteasy.http.client.api.IHttpClient;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

public class HttpClientSpringConfigurationTest {

    private static AnnotationConfigApplicationContext context(Map<String, Object> props) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", props));
        ctx.register(HttpClientSpringConfiguration.class);
        ctx.refresh();
        return ctx;
    }

    @Test
    public void defaultsWithoutStores() {
        try (AnnotationConfigApplicationContext ctx = context(Map.of())) {
            assertNotNull(ctx.getBean(IHttpClient.class));
        }
    }

    @Test
    public void truststoreIsLoadedWhenConfigured() throws Exception {
        File ts = File.createTempFile("pr125-ts", ".p12");
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(null, null);
            try (FileOutputStream out = new FileOutputStream(ts)) {
                ks.store(out, "testpass".toCharArray());
            }
            try (AnnotationConfigApplicationContext ctx = context(Map.of(
                    "superfly.notification.http.truststore-url", ts.toURI().toString(),
                    "superfly.notification.http.truststore-password", "testpass"))) {
                assertNotNull(ctx.getBean(IHttpClient.class));
            }
        } finally {
            ts.delete();
        }
    }

    @Test
    public void wrongPasswordFailsFast() throws Exception {
        File ts = File.createTempFile("pr125-ts", ".p12");
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(null, null);
            try (FileOutputStream out = new FileOutputStream(ts)) {
                ks.store(out, "testpass".toCharArray());
            }
            assertThrows(RuntimeException.class, () -> context(Map.of(
                    "superfly.notification.http.truststore-url", ts.toURI().toString(),
                    "superfly.notification.http.truststore-password", "wrong")));
        } finally {
            ts.delete();
        }
    }
}
