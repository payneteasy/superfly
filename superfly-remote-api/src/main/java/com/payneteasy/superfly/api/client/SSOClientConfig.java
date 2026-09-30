package com.payneteasy.superfly.api.client;

import com.payneteasy.http.client.api.HttpRequestParameters;
import com.payneteasy.http.client.api.HttpTimeouts;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

/**
 * Иммутабельная конфигурация {@link SSOHttpServiceApiClient}.
 *
 * <p>Содержит координаты SSO-сервера (baseUrl, subsystem name/token), дефолтные параметры HTTP-запроса
 * и опциональный per-endpoint override таймаутов через {@link #endpointParameters}.
 *
 * <p><b>Per-endpoint timeouts.</b> {@code defaultParameters} применяется ко всем эндпоинтам,
 * для которых не задан override. Если в {@code endpointParameters} есть запись для конкретного
 * {@link Endpoint} — используется она. Это позволяет разнести профили вызовов:
 *
 * <pre>{@code
 * SSOClientConfig config = SSOClientConfig.builder()
 *     .baseUrl("https://superfly.example.com/sso")
 *     .subsystemName("paynet-ui")
 *     .subsystemToken(token)
 *     .defaultParameters(HttpRequestParameters.builder()
 *         .timeouts(new HttpTimeouts(5_000, 30_000)).build())
 *     // critical login path — short timeout
 *     .endpointParameter(Endpoint.AUTHENTICATE,
 *         HttpRequestParameters.builder()
 *             .timeouts(new HttpTimeouts(5_000, 10_000)).build())
 *     // long-poll — large socket timeout, server waits 75s, 15s buffer
 *     .endpointParameter(Endpoint.GET_EVENTS,
 *         HttpRequestParameters.builder()
 *             .timeouts(new HttpTimeouts(5_000, 90_000)).build())
 *     .build();
 * }</pre>
 *
 * <p><b>Безопасность URL.</b> По умолчанию принимаются только {@code https://} URL — соответствие
 * PCI DSS 4.2.1. HTTP-схему можно разрешить через JVM property
 * {@code -Dsuperfly.client.allowInsecureScheme=true} (выводится WARN); этот режим — только для
 * локальной разработки/тестов, использовать в production запрещено.
 */
@Getter
@Slf4j
public final class SSOClientConfig {

    /**
     * Имя JVM property для разрешения HTTP-схемы в {@code baseUrl}. По умолчанию недопустима.
     */
    public static final String ALLOW_INSECURE_PROPERTY = "superfly.client.allowInsecureScheme";

    private final String                                  baseUrl;
    private final String                                  subsystemName;
    @Nullable
    private final String                                  subsystemToken;
    private final HttpRequestParameters                   defaultParameters;
    private final Map<Endpoint, HttpRequestParameters>    endpointParameters;

    @Builder
    private SSOClientConfig(
            String baseUrl,
            String subsystemName,
            @Nullable String subsystemToken,
            HttpRequestParameters defaultParameters,
            @Singular("endpointParameter") Map<Endpoint, HttpRequestParameters> endpointParameters
    ) {
        this.baseUrl            = normalizeBaseUrl(baseUrl);
        this.subsystemName      = Objects.requireNonNull(subsystemName,     "subsystemName must not be null");
        this.subsystemToken     = subsystemToken;
        this.defaultParameters  = Objects.requireNonNull(defaultParameters, "defaultParameters must not be null");
        this.endpointParameters = endpointParameters == null ? Map.of() : Map.copyOf(endpointParameters);
        log.debug("SSOClientConfig built: baseUrl={} subsystem={} overrides={}",
                this.baseUrl, this.subsystemName, this.endpointParameters.keySet());
    }

    /**
     * Возвращает {@link HttpRequestParameters} для заданного эндпоинта.
     * Если в {@link #endpointParameters} есть override — используется он, иначе {@link #defaultParameters}.
     */
    public HttpRequestParameters parametersFor(Endpoint endpoint) {
        HttpRequestParameters override = endpointParameters.get(endpoint);
        HttpRequestParameters params   = override != null ? override : defaultParameters;
        if (log.isDebugEnabled()) {
            HttpTimeouts t = params.getTimeouts();
            if (t != null) {
                log.debug("Resolved endpoint={} → {}(connect={}ms read={}ms)",
                        endpoint, override != null ? "override" : "default",
                        t.getConnectTimeoutMs(), t.getReadTimeoutMs());
            } else {
                log.debug("Resolved endpoint={} → {} (no timeouts set)",
                        endpoint, override != null ? "override" : "default");
            }
        }
        return params;
    }

    /**
     * @return полный URL для эндпоинта: {@code baseUrl + endpoint.path()}.
     */
    public String urlFor(Endpoint endpoint) {
        return baseUrl + endpoint.path();
    }

    private static String normalizeBaseUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("baseUrl must not be null or empty");
        }
        if (url.startsWith("http://")) {
            if (Boolean.getBoolean(ALLOW_INSECURE_PROPERTY)) {
                log.warn("Insecure HTTP scheme in baseUrl='{}' — allowed via -D{}=true (PCI DSS 4.2.1 violation)",
                        url, ALLOW_INSECURE_PROPERTY);
            } else {
                throw new IllegalArgumentException(
                        "baseUrl must use HTTPS (PCI DSS 4.2.1). Got: " + url
                                + ". To allow HTTP set -D" + ALLOW_INSECURE_PROPERTY + "=true");
            }
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
