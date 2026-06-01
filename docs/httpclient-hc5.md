# Apache HttpClient 5 Transport — `superfly-httpclient-hc5`

> **SSO-3** — замена JDK `HttpURLConnection` (`HttpClientImpl`) на Apache HC5  
> Добавляет connection pooling, explicit lifecycle и per-request timeout override.

## Зачем

| Проблема `HttpClientImpl` | Решение в `ApacheHC5HttpClient` |
|---------------------------|--------------------------------|
| Нет connection pooling — каждый вызов = новый TCP+TLS handshake | PoolingHttpClientConnectionManager (по умолчанию 20/20 conn) |
| Нет `AutoCloseable` — resource leak при timeout | `implements AutoCloseable`, `close()` корректно шатдаунит пул |
| Нет eviction idle соединений | `evictIdleConnections(30s)` |

## Зависимость

```xml
<dependency>
    <groupId>com.payneteasy.superfly</groupId>
    <artifactId>superfly-httpclient-hc5</artifactId>
    <version>${superfly.version}</version>
</dependency>
```

Транзитивно тащит: `org.apache.httpcomponents.client5:httpclient5:5.4.4`. SSL-хелперы (`JdkSslSocketFactoryBuilder` и др.) входят в сам модуль `superfly-httpclient-hc5` (пакет `com.payneteasy.httpclient.contrib.ssl`).

## Builder API

```java
ApacheHC5HttpClient client = ApacheHC5HttpClient.builder()
    // SSL/mTLS — опционально; null = JVM default SSL context
    .sslContext(JdkSslSocketFactoryBuilder.buildSslContext(keyStoreUrl, ksPwd, tsUrl, tsPwd))
    // HostnameVerifier — опционально; null = стандартная hostname verification
    .hostnameVerifier(JdkSslSocketFactoryBuilder.buildCnHostnameVerifier("superfly-server"))
    // Connection pool (дефолты ниже)
    .maxConnTotal(20)      // суммарный лимит
    .maxConnPerRoute(20)   // лимит на маршрут (host:port)
    .idleEvictionSec(30)   // eviction idle connections
    .build();
```

### Дефолтные значения

| Параметр | Значение | Обоснование |
|----------|---------|-------------|
| `maxConnTotal` | 20 | Покрывает peak load типичного consumer (paynet ~20-30 RPS) |
| `maxConnPerRoute` | 20 | = total для single-host setup (consumer видит один SSO-host) |
| `idleEvictionSec` | 30 | Освобождает соединения за пределами обычного traffic burst |

> Если consumer обслуживает несколько SSO-серверов (горячее переключение) — увеличьте
> `maxConnTotal` пропорционально количеству хостов, сохраняя `maxConnPerRoute=20`.
> Для high-RPS приложений (> 100 RPS к SSO) — поднимите оба значения, измерив реальный peak.

## Per-request timeout

Timeout'ы передаются через `HttpRequestParameters.timeouts` и применяются через `HttpClientContext` к каждому запросу независимо:

```java
HttpRequestParameters params = HttpRequestParameters.builder()
    .timeouts(new HttpTimeouts(connectMs, readMs))
    .build();

HttpResponse response = client.send(request, params);
```

Поля `sslSocketFactory` и `hostnameVerifier` из `HttpRequestParameters` игнорируются — HC5 использует pooled connections с одним SSL-контекстом, заданным при создании.

## Exception mapping

| HC5 исключение | Mapped to |
|----------------|-----------|
| `ConnectTimeoutException` | `HttpConnectException` |
| `SocketTimeoutException` | `HttpReadException` |
| `IOException` (connection refused / unreachable) | `HttpConnectException` |
| `IOException` (read/write I/O) | `HttpReadException` |

## Lifecycle — AutoCloseable

`ApacheHC5HttpClient` реализует `AutoCloseable`. `close()` корректно завершает все соединения пула:

```java
// try-with-resources
try (ApacheHC5HttpClient client = ApacheHC5HttpClient.builder().build()) {
    HttpResponse response = client.send(request, params);
}

// Spring @Bean: destroyMethod автоматически вызовет close() при остановке контекста
@Bean(destroyMethod = "close")
public IHttpClient ssoHttpClient() throws IOException, GeneralSecurityException {
    return ApacheHC5HttpClient.builder()
        .sslContext(buildSslContext())
        .hostnameVerifier(JdkSslSocketFactoryBuilder.buildCnHostnameVerifier("superfly-server"))
        .build();
}
```

После `close()` любой вызов `send()` бросает `IllegalStateException`.

## Spring @Bean — полный пример

```java
@Configuration
public class SpringUISuperflyClientConfiguration {

    @Bean(destroyMethod = "close")
    public IHttpClient ssoHttpClient() throws IOException, GeneralSecurityException {
        ApacheHC5HttpClient.Builder builder = ApacheHC5HttpClient.builder();
        if (ssoConfig.getKeyStoreResourceUrl() != null) {
            builder.sslContext(buildSslContext());
            if (ssoConfig.getExpectedServerCn() != null) {
                builder.hostnameVerifier(
                    JdkSslSocketFactoryBuilder.buildCnHostnameVerifier(ssoConfig.getExpectedServerCn()));
            }
        }
        return builder.build();
    }

    private SSLContext buildSslContext() throws IOException, GeneralSecurityException {
        URL keyStore   = resolveUrl(ssoConfig.getKeyStoreResourceUrl());
        URL trustStore = resolveUrl(ssoConfig.getTrustStoreResourceUrl());
        return JdkSslSocketFactoryBuilder.buildSslContext(
                keyStore, ssoConfig.getKeyStorePassword(),
                trustStore, ssoConfig.getTrustStorePassword());
    }
}
```

## mTLS интеграция

```
keystore.jks (client cert + key)
    ↓
JdkSslSocketFactoryBuilder.buildSslContext(keyStoreUrl, ksPwd, trustStoreUrl, tsPwd)
    ↓ SSLContext
ApacheHC5HttpClient.builder().sslContext(ctx)
    ↓ SSLConnectionSocketFactory (HC5)
PoolingHttpClientConnectionManager
    ↓
CloseableHttpClient (мТLS соединение к superfly-server)
```

Если CN сертификата сервера не совпадает с hostname соединения (например CN=`superfly-server`, host=`localhost`) — используйте CN hostname verifier:

```java
builder.hostnameVerifier(
    JdkSslSocketFactoryBuilder.buildCnHostnameVerifier("superfly-server"));
```

Безопасно только в связке с кастомным trustStore: цепочка сертификата уже проверена TLS.

## Logging

| Уровень | Что логируется |
|---------|---------------|
| `INFO` | Создание/закрытие клиента, ssl-режим |
| `DEBUG` | Каждый запрос: method/url/timeouts, статус, elapsed, pool stats |
| `WARN` | Timeout (connect/read), ошибки close() |

```xml
<!-- logback.xml — включить DEBUG для пула -->
<logger name="com.payneteasy.superfly.httpclient.hc5" level="DEBUG"/>
<logger name="org.apache.hc.client5" level="DEBUG"/>
```

## Тесты

```
superfly-httpclient-hc5/src/test/java/.../ApacheHC5HttpClientTest.java
```

6 тестов с embedded JDK `HttpServer` (без WireMock):

1. `testSuccessfulPost` — POST 200 + body
2. `testConnectTimeoutThrows` → `HttpConnectException`
3. `testResponseTimeoutThrows` → `HttpReadException`
4. `testPerRequestTimeoutOverride` — per-request override работает
5. `testAutoCloseableShutdown` — после `close()` → `IllegalStateException`
6. `testHeadersForwardedCorrectly` — headers доходят до сервера

## См. также

- [SSL/mTLS Integration Guide](ssl-mtls.md)
- [SSO HTTP Client](sso-http-client.md)
- `JdkSslSocketFactoryBuilder` — `superfly-httpclient-hc5` (пакет `com.payneteasy.httpclient.contrib.ssl`)
- `SSOClientConfig.parametersFor(endpoint)` — per-endpoint timeout config
