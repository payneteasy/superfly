package com.payneteasy.superfly.api.transport;

import com.payneteasy.http.client.api.HttpHeader;
import com.payneteasy.http.client.api.HttpHeaders;
import com.payneteasy.http.client.api.HttpMethod;
import com.payneteasy.http.client.api.HttpRequest;
import com.payneteasy.http.client.api.HttpRequestParameters;
import com.payneteasy.http.client.api.HttpResponse;
import com.payneteasy.http.client.api.HttpTimeouts;
import com.payneteasy.http.client.api.IHttpClient;
import com.payneteasy.http.client.api.exceptions.HttpConnectException;
import com.payneteasy.http.client.api.exceptions.HttpReadException;
import com.payneteasy.http.client.api.exceptions.HttpWriteException;
import org.apache.hc.client5.http.ConnectTimeoutException;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * {@link IHttpClient} и {@link AutoCloseable} реализация поверх Apache HttpClient 5.
 *
 * <p>Преимущества перед {@code HttpClientImpl} (JDK HttpURLConnection):
 * <ul>
 *   <li>Connection pooling — устраняет накладные расходы на TCP-handshake при каждом вызове</li>
 *   <li>Explicit lifecycle — {@link #close()} корректно шатдаунит пул; безопасен в try-with-resources</li>
 *   <li>Per-request timeout — {@link HttpTimeouts} из {@link HttpRequestParameters} отображается
 *       в HC5 {@link RequestConfig} при каждом вызове</li>
 * </ul>
 *
 * <p>SSL/mTLS настраивается один раз при создании через {@link Builder#sslContext(SSLContext)}.
 * Per-request поля {@code sslSocketFactory} и {@code hostnameVerifier} из {@link HttpRequestParameters}
 * игнорируются — HC5 использует pooled connections с одним SSL-контекстом.
 *
 * <p>Использование:
 * <pre>{@code
 * try (ApacheHC5HttpClient client = ApacheHC5HttpClient.builder()
 *         .sslContext(JdkSslSocketFactoryBuilder.buildSslContext(...))
 *         .hostnameVerifier(JdkSslSocketFactoryBuilder.buildCnHostnameVerifier("superfly-server"))
 *         .build()) {
 *     HttpResponse response = client.send(request, params);
 * }
 * }</pre>
 */
public class ApacheHC5HttpClient implements IHttpClient, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(ApacheHC5HttpClient.class);

    // ── connection pool defaults ──────────────────────────────────────────────

    /** Максимальное суммарное число соединений в пуле. */
    public static final int DEFAULT_MAX_CONN_TOTAL      = 50;

    /** Максимальное число соединений на один маршрут (host:port). */
    public static final int DEFAULT_MAX_CONN_PER_ROUTE  = 25;

    /** Время простоя (сек) после которого соединение вытесняется из пула. */
    public static final int DEFAULT_IDLE_EVICTION_SEC   = 30;

    // ── state ─────────────────────────────────────────────────────────────────

    private final CloseableHttpClient               httpClient;
    private final PoolingHttpClientConnectionManager connManager;
    private volatile boolean                         closed = false;

    // ── constructor ───────────────────────────────────────────────────────────

    private ApacheHC5HttpClient(Builder builder) {
        SSLConnectionSocketFactory sslSF = buildSslSocketFactory(builder.sslContext, builder.hostnameVerifier);

        this.connManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(sslSF)
                .setMaxConnTotal(builder.maxConnTotal)
                .setMaxConnPerRoute(builder.maxConnPerRoute)
                .build();

        this.httpClient = HttpClients.custom()
                .setConnectionManager(connManager)
                .evictIdleConnections(TimeValue.of(builder.idleEvictionSec, TimeUnit.SECONDS))
                .build();

        LOG.info("ApacheHC5HttpClient created: maxConnTotal={} maxConnPerRoute={} idleEvictionSec={} ssl={}",
                builder.maxConnTotal, builder.maxConnPerRoute, builder.idleEvictionSec,
                builder.sslContext != null ? "configured" : "default");
    }

    // ── IHttpClient ───────────────────────────────────────────────────────────

    @Override
    public HttpResponse send(HttpRequest request, HttpRequestParameters params)
            throws HttpConnectException, HttpReadException, HttpWriteException {

        if (closed) {
            throw new IllegalStateException("ApacheHC5HttpClient is already closed");
        }

        String url = request.getUrl();
        long startMs = System.currentTimeMillis();

        LOG.debug("send: method={} url={} timeouts={}", request.getMethod(), url, formatTimeouts(params));

        ClassicHttpRequest hcRequest = buildHcRequest(request);
        HttpClientContext context = buildContext(params);

        try {
            HttpResponse response = httpClient.execute(hcRequest, context, httpResponse -> {
                int status = httpResponse.getCode();
                String reason = httpResponse.getReasonPhrase();
                List<HttpHeader> headers = mapResponseHeaders(httpResponse.getHeaders());
                byte[] body = readBody(httpResponse.getEntity());

                long elapsed = System.currentTimeMillis() - startMs;
                logPoolStats();
                LOG.debug("send: status={} reason={} bodyLen={} elapsed={}ms url={}",
                        status, reason, body.length, elapsed, url);

                return new HttpResponse(status, reason, headers, body);
            });

            return response;

        } catch (ConnectTimeoutException e) {
            LOG.warn("send: connect timeout after {}ms url={}", System.currentTimeMillis() - startMs, url);
            throw new HttpConnectException("Connect timeout to " + url, e);

        } catch (SocketTimeoutException e) {
            LOG.warn("send: response timeout after {}ms url={}", System.currentTimeMillis() - startMs, url);
            throw new HttpReadException("Response timeout from " + url, e);

        } catch (IOException e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("connect") || msg.contains("refused") || msg.contains("unreachable")) {
                LOG.warn("send: connect error after {}ms url={} error={}", System.currentTimeMillis() - startMs, url, e.getMessage());
                throw new HttpConnectException("Connection failed to " + url, e);
            }
            LOG.warn("send: read/write error after {}ms url={} error={}", System.currentTimeMillis() - startMs, url, e.getMessage());
            throw new HttpReadException("I/O error communicating with " + url, e);
        }
    }

    // ── AutoCloseable ─────────────────────────────────────────────────────────

    @Override
    public void close() {
        if (closed) {
            LOG.debug("close: already closed, ignoring");
            return;
        }
        closed = true;
        LOG.info("ApacheHC5HttpClient closing: shutting down connection pool and HTTP client");
        try {
            httpClient.close();
        } catch (IOException e) {
            LOG.warn("close: error closing httpClient: {}", e.getMessage());
        }
        connManager.close();
        LOG.info("ApacheHC5HttpClient closed");
    }

    // ── Builder ───────────────────────────────────────────────────────────────

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private SSLContext       sslContext       = null;
        private HostnameVerifier hostnameVerifier = null;
        private int              maxConnTotal     = DEFAULT_MAX_CONN_TOTAL;
        private int              maxConnPerRoute  = DEFAULT_MAX_CONN_PER_ROUTE;
        private int              idleEvictionSec  = DEFAULT_IDLE_EVICTION_SEC;

        private Builder() {}

        /** SSLContext для mTLS/TLS. {@code null} → JVM default SSL context. */
        public Builder sslContext(SSLContext sslContext) {
            this.sslContext = sslContext;
            return this;
        }

        /** HostnameVerifier для проверки CN сертификата сервера. {@code null} → стандартная проверка. */
        public Builder hostnameVerifier(HostnameVerifier hostnameVerifier) {
            this.hostnameVerifier = hostnameVerifier;
            return this;
        }

        /** Максимальное суммарное число соединений в пуле. */
        public Builder maxConnTotal(int maxConnTotal) {
            this.maxConnTotal = maxConnTotal;
            return this;
        }

        /** Максимальное число соединений на один маршрут. */
        public Builder maxConnPerRoute(int maxConnPerRoute) {
            this.maxConnPerRoute = maxConnPerRoute;
            return this;
        }

        /** Время простоя (сек) после которого соединение вытесняется. */
        public Builder idleEvictionSec(int idleEvictionSec) {
            this.idleEvictionSec = idleEvictionSec;
            return this;
        }

        public ApacheHC5HttpClient build() {
            return new ApacheHC5HttpClient(this);
        }
    }

    // ── internals ─────────────────────────────────────────────────────────────

    private static SSLConnectionSocketFactory buildSslSocketFactory(
            SSLContext sslContext, HostnameVerifier hostnameVerifier) {

        SSLConnectionSocketFactoryBuilder sslBuilder = SSLConnectionSocketFactoryBuilder.create();
        if (sslContext != null) {
            sslBuilder.setSslContext(sslContext);
            LOG.debug("buildSslSocketFactory: using custom SSLContext");
        }
        if (hostnameVerifier != null) {
            sslBuilder.setHostnameVerifier(hostnameVerifier);
            LOG.debug("buildSslSocketFactory: using custom HostnameVerifier={}", hostnameVerifier.getClass().getSimpleName());
        }
        return sslBuilder.build();
    }

    private static ClassicHttpRequest buildHcRequest(HttpRequest request) {
        String methodName = request.getMethod() != null ? request.getMethod().name() : HttpMethod.GET.name();
        BasicClassicHttpRequest hcRequest = new BasicClassicHttpRequest(methodName, request.getUrl());

        HttpHeaders headers = request.getHeaders();
        if (headers != null) {
            for (HttpHeader header : headers.asList()) {
                hcRequest.addHeader(header.getName(), header.getValue());
            }
        }

        byte[] body = request.getBody();
        if (body != null && body.length > 0) {
            hcRequest.setEntity(new ByteArrayEntity(body, null));
        }

        return hcRequest;
    }

    private static HttpClientContext buildContext(HttpRequestParameters params) {
        HttpClientContext context = HttpClientContext.create();
        if (params != null && params.getTimeouts() != null) {
            HttpTimeouts timeouts = params.getTimeouts();
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(Timeout.ofMilliseconds(timeouts.getConnectTimeoutMs()))
                    .setResponseTimeout(Timeout.ofMilliseconds(timeouts.getReadTimeoutMs()))
                    .build();
            context.setRequestConfig(requestConfig);
            LOG.debug("buildContext: connectTimeout={}ms responseTimeout={}ms",
                    timeouts.getConnectTimeoutMs(), timeouts.getReadTimeoutMs());
        }
        return context;
    }

    private static byte[] readBody(HttpEntity entity) throws IOException {
        if (entity == null) {
            return new byte[0];
        }
        byte[] bytes = EntityUtils.toByteArray(entity);
        return bytes != null ? bytes : new byte[0];
    }

    private static List<HttpHeader> mapResponseHeaders(Header[] headers) {
        if (headers == null) {
            return List.of();
        }
        List<HttpHeader> result = new ArrayList<>(headers.length);
        for (Header h : headers) {
            result.add(new HttpHeader(h.getName(), h.getValue()));
        }
        return result;
    }

    private void logPoolStats() {
        if (LOG.isDebugEnabled()) {
            var stats = connManager.getTotalStats();
            LOG.debug("pool: available={} leased={} pending={} max={}",
                    stats.getAvailable(), stats.getLeased(), stats.getPending(), stats.getMax());
        }
    }

    private static String formatTimeouts(HttpRequestParameters params) {
        if (params == null || params.getTimeouts() == null) {
            return "none";
        }
        HttpTimeouts t = params.getTimeouts();
        return "connect=" + t.getConnectTimeoutMs() + "ms read=" + t.getReadTimeoutMs() + "ms";
    }
}
