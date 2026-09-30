package com.payneteasy.superfly.api.transport;

import com.payneteasy.http.client.api.HttpHeader;
import com.payneteasy.http.client.api.HttpHeaders;
import com.payneteasy.http.client.api.HttpMethod;
import com.payneteasy.http.client.api.HttpRequest;
import com.payneteasy.http.client.api.HttpRequestParameters;
import com.payneteasy.http.client.api.HttpResponse;
import com.payneteasy.http.client.api.HttpTimeouts;
import com.payneteasy.http.client.api.exceptions.HttpConnectException;
import com.payneteasy.http.client.api.exceptions.HttpReadException;
import com.sun.net.httpserver.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/**
 * Unit-тесты {@link ApacheHC5HttpClient} с JDK {@link HttpServer} в качестве embedded HTTP-сервера.
 */
public class ApacheHC5HttpClientTest {

    private HttpServer  server;
    private int         serverPort;
    private ApacheHC5HttpClient client;

    @Before
    public void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 10);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        serverPort = server.getAddress().getPort();
        client = ApacheHC5HttpClient.builder().build();
    }

    @After
    public void tearDown() {
        if (client != null) client.close();
        if (server != null) server.stop(0);
    }

    /**
     * Contract test: дефолты pool size — single-host симметрия (20/20).
     * Изменение этих констант должно потребовать осознанного обновления теста и причины в PR
     * (см. ROADMAP.md SSO-3-tune: paynet — single SSO-host setup, ≈ 20–30 RPS).
     */
    @Test
    public void testDefaultPoolSizes() {
        assertEquals(20, ApacheHC5HttpClient.DEFAULT_MAX_CONN_TOTAL);
        assertEquals(20, ApacheHC5HttpClient.DEFAULT_MAX_CONN_PER_ROUTE);
        assertEquals(30, ApacheHC5HttpClient.DEFAULT_IDLE_EVICTION_SEC);
    }

    @Test
    public void testSuccessfulPost() throws Exception {
        server.createContext("/api/login", exchange -> {
            byte[] resp = "{\"result\":\"ok\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, resp.length);
            exchange.getResponseBody().write(resp);
            exchange.close();
        });

        HttpResponse response = client.send(
                HttpRequest.builder()
                        .url("http://localhost:" + serverPort + "/api/login")
                        .method(HttpMethod.POST)
                        .body("{\"user\":\"test\"}".getBytes(StandardCharsets.UTF_8))
                        .build(),
                HttpRequestParameters.builder().timeouts(new HttpTimeouts(5_000, 5_000)).build());

        assertEquals(200, response.getStatusCode());
        assertEquals("{\"result\":\"ok\"}", new String(response.getBody(), StandardCharsets.UTF_8));
    }

    @Test(expected = HttpConnectException.class)
    public void testConnectTimeoutThrows() throws Exception {
        int freePort;
        try (ServerSocket ss = new ServerSocket(0)) { freePort = ss.getLocalPort(); }

        ApacheHC5HttpClient freshClient = ApacheHC5HttpClient.builder().build();
        try {
            freshClient.send(
                    HttpRequest.builder().url("http://localhost:" + freePort + "/test").method(HttpMethod.GET).build(),
                    HttpRequestParameters.builder().timeouts(new HttpTimeouts(200, 200)).build());
            fail("Expected HttpConnectException");
        } finally {
            freshClient.close();
        }
    }

    @Test(expected = HttpReadException.class)
    public void testResponseTimeoutThrows() throws Exception {
        server.createContext("/api/slow", exchange -> {
            try { Thread.sleep(3_000); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
            byte[] resp = "slow".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, resp.length);
            exchange.getResponseBody().write(resp);
            exchange.close();
        });

        client.send(
                HttpRequest.builder().url("http://localhost:" + serverPort + "/api/slow").method(HttpMethod.GET).build(),
                HttpRequestParameters.builder().timeouts(new HttpTimeouts(5_000, 150)).build());
        fail("Expected HttpReadException");
    }

    @Test
    public void testPerRequestTimeoutOverride() throws Exception {
        server.createContext("/api/fast", exchange -> {
            byte[] resp = "fast".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, resp.length);
            exchange.getResponseBody().write(resp);
            exchange.close();
        });
        server.createContext("/api/delayed", exchange -> {
            try { Thread.sleep(500); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
            byte[] resp = "delayed".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, resp.length);
            exchange.getResponseBody().write(resp);
            exchange.close();
        });

        HttpResponse fast = client.send(
                HttpRequest.builder().url("http://localhost:" + serverPort + "/api/fast").method(HttpMethod.GET).build(),
                HttpRequestParameters.builder().timeouts(new HttpTimeouts(5_000, 5_000)).build());
        assertEquals(200, fast.getStatusCode());

        try {
            client.send(
                    HttpRequest.builder().url("http://localhost:" + serverPort + "/api/delayed").method(HttpMethod.GET).build(),
                    HttpRequestParameters.builder().timeouts(new HttpTimeouts(5_000, 100)).build());
            fail("Expected HttpReadException from per-request timeout");
        } catch (HttpReadException e) {
            // expected
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testAutoCloseableShutdown() throws Exception {
        ApacheHC5HttpClient shortLived = ApacheHC5HttpClient.builder().build();
        shortLived.close();
        shortLived.send(
                HttpRequest.builder().url("http://localhost:" + serverPort + "/test").method(HttpMethod.GET).build(),
                HttpRequestParameters.builder().timeouts(new HttpTimeouts(5_000, 5_000)).build());
    }

    @Test
    public void testHeadersForwardedCorrectly() throws Exception {
        AtomicReference<String> capturedToken = new AtomicReference<>();
        AtomicReference<String> capturedCt    = new AtomicReference<>();

        server.createContext("/api/auth", exchange -> {
            capturedToken.set(exchange.getRequestHeaders().getFirst("X-Subsystem-Token"));
            capturedCt.set(exchange.getRequestHeaders().getFirst("Content-Type"));
            byte[] resp = "ok".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, resp.length);
            exchange.getResponseBody().write(resp);
            exchange.close();
        });

        HttpResponse response = client.send(
                HttpRequest.builder()
                        .url("http://localhost:" + serverPort + "/api/auth")
                        .method(HttpMethod.POST)
                        .headers(new HttpHeaders(List.of(
                                new HttpHeader("X-Subsystem-Token", "token-abc"),
                                new HttpHeader("Content-Type", "application/x-java-serialized-object"))))
                        .body(new byte[]{1, 2, 3})
                        .build(),
                HttpRequestParameters.builder().timeouts(new HttpTimeouts(5_000, 5_000)).build());

        assertEquals(200, response.getStatusCode());
        assertEquals("token-abc", capturedToken.get());
        assertNotNull(capturedCt.get());
    }
}
