package com.payneteasy.superfly.api.transport;

import com.payneteasy.http.client.api.HttpMethod;
import com.payneteasy.http.client.api.HttpRequest;
import com.payneteasy.http.client.api.HttpRequestParameters;
import com.payneteasy.http.client.api.HttpResponse;
import com.payneteasy.http.client.api.HttpTimeouts;
import com.payneteasy.http.client.api.exceptions.HttpConnectException;
import com.payneteasy.http.client.api.exceptions.HttpReadException;
import com.sun.net.httpserver.HttpServer;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import javax.net.ssl.HostnameVerifier;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ApacheHC5HttpClientHardeningTest {

    private HttpServer server;
    private int port;

    @Before
    public void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 10);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        port = server.getAddress().getPort();
    }

    @After
    public void tearDown() {
        server.stop(0);
    }

    private static HttpRequestParameters timeouts(int connectMs, int readMs) {
        return HttpRequestParameters.builder().timeouts(new HttpTimeouts(connectMs, readMs)).build();
    }

    private HttpRequest post(String path) {
        return HttpRequest.builder()
                .url("http://localhost:" + port + path)
                .method(HttpMethod.POST)
                .body("{}".getBytes(StandardCharsets.UTF_8))
                .build();
    }

    @Test
    public void postOn503IsNotRetried() throws Exception {
        AtomicInteger hits = new AtomicInteger();
        server.createContext("/fail", exchange -> {
            hits.incrementAndGet();
            exchange.sendResponseHeaders(503, -1);
            exchange.close();
        });

        try (ApacheHC5HttpClient client = ApacheHC5HttpClient.builder().build()) {
            HttpResponse response = client.send(post("/fail"), timeouts(5_000, 5_000));
            assertEquals(503, response.getStatusCode());
        }
        assertEquals(1, hits.get());
    }

    @Test
    public void redirectIsNotFollowed() throws Exception {
        AtomicInteger targetHits = new AtomicInteger();
        server.createContext("/target", exchange -> {
            targetHits.incrementAndGet();
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        server.createContext("/redirect", exchange -> {
            exchange.getResponseHeaders().add("Location", "http://localhost:" + port + "/target");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        });

        try (ApacheHC5HttpClient client = ApacheHC5HttpClient.builder().build()) {
            HttpResponse response = client.send(post("/redirect"), timeouts(5_000, 5_000));
            assertEquals(302, response.getStatusCode());
        }
        assertEquals(0, targetHits.get());
    }

    @Test
    public void poolSlotWaitIsBoundedByConnectTimeout() throws Exception {
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch occupied = new CountDownLatch(1);
        server.createContext("/slow", exchange -> {
            occupied.countDown();
            try {
                release.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });

        try (ApacheHC5HttpClient client = ApacheHC5HttpClient.builder().maxConnTotal(1).maxConnPerRoute(1).build()) {
            Thread holder = new Thread(() -> {
                try {
                    client.send(post("/slow"), timeouts(5_000, 10_000));
                } catch (Exception ignored) {
                    // released in finally
                }
            });
            holder.start();
            assertTrue(occupied.await(5, TimeUnit.SECONDS));

            long start = System.nanoTime();
            // the exhausted pool surfaces as an I/O error (HttpReadException), bounded by connectTimeout
            assertThrows(Exception.class, () -> client.send(post("/slow"), timeouts(500, 10_000)));
            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            assertTrue("waited " + elapsedMs + "ms", elapsedMs < 3_000);

            release.countDown();
            holder.join(5_000);
        }
    }

    @Test
    public void perRequestSslSettingsAreRejected() throws Exception {
        HostnameVerifier verifier = (host, session) -> true;
        HttpRequestParameters withVerifier = HttpRequestParameters.builder()
                .timeouts(new HttpTimeouts(1_000, 1_000)).hostnameVerifier(verifier).build();
        HttpRequestParameters withSocketFactory = HttpRequestParameters.builder()
                .sslSocketFactory(javax.net.ssl.SSLContext.getDefault().getSocketFactory()).build();

        try (ApacheHC5HttpClient client = ApacheHC5HttpClient.builder().build()) {
            assertThrows(IllegalArgumentException.class, () -> client.send(post("/x"), withVerifier));
            assertThrows(IllegalArgumentException.class, () -> client.send(post("/x"), withSocketFactory));
        }
    }

    /**
     * Relies on 10.255.255.1 being non-routable (SYN dropped). On networks that reject it immediately
     * the failure is still a connect failure, only the timing assertion is loose.
     */
    @Test
    public void connectTimeoutIsHonoured() throws Exception {
        try (ApacheHC5HttpClient client = ApacheHC5HttpClient.builder().build()) {
            HttpRequest request = HttpRequest.builder()
                    .url("http://10.255.255.1:81/x").method(HttpMethod.POST).build();
            long start = System.nanoTime();
            try {
                client.send(request, timeouts(500, 1_000));
                fail("expected connect failure");
            } catch (HttpReadException e) {
                // a transparent proxy/firewall accepted the TCP connection, so there is nothing to time out on
                Assume.assumeNoException("environment accepts TCP to a non-routable address", e);
            } catch (HttpConnectException expected) {
                // expected
            }
            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            assertTrue("elapsed " + elapsedMs + "ms", elapsedMs < 5_000);
        }
    }
}
