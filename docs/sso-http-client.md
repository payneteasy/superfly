[← API Reference](api.md) · [Back to README](../README.md) · [Руководство по интеграции →](integration-guide.md)

# SSO HTTP Client

`SSOHttpServiceApiClient` — это клиентская реализация `SSOService` в `superfly-remote-api`,
работающая поверх HTTP. Используется paynet и другими подсистемами для вызова RPC-эндпоинтов
SSO-сервера (`/sso.service/*`).

## Архитектура

```
SSOService (interface)
    ▲
    │ implements
SSOHttpServiceApiClient ──uses──> IHttpClient
                                      │
                            ┌─────────┴──────────┐
                            ▼                    ▼
                      HttpClientImpl       ApacheHC5HttpClient
                      (JDK HttpURLConn.)    (planned, SSO-3)
```

Транспорт инжектируется через конструктор — клиент не зависит от конкретной реализации HTTP.
Это позволяет подменять `IHttpClient` без правки `SSOHttpServiceApiClient`: для тестов
(EasyMock/Mockito), для production (Apache HC5 с connection pooling), для отладки (logging
decorator).

## Базовое использование

```java
SSOClientConfig config = SSOClientConfig.builder()
    .baseUrl("https://superfly.example.com/sso.service")
    .subsystemName("paynet-ui")
    .subsystemToken(System.getenv("SSO_TOKEN"))
    .defaultParameters(HttpRequestParameters.builder()
        .timeouts(new HttpTimeouts(5_000, 30_000))  // connect=5s, read=30s
        .build())
    .build();

try (SSOHttpServiceApiClient client = new SSOHttpServiceApiClient(
        new HttpClientImpl(),  // transport
        config,
        new ApiSerializationManager())) {

    SSOUser user = client.authenticate(
        new AuthenticateRequest("john", "secret"));
}
```

`SSOHttpServiceApiClient` thread-safe и предназначен для долгого жизненного цикла —
создавайте один экземпляр на приложение и переиспользуйте.

## Per-endpoint таймауты

В production разные эндпоинты имеют разные характеристики и должны иметь разные таймауты:

| Эндпоинт | Назначение | Рекомендуемый socket timeout |
|---|---|---|
| `authenticate`, `checkOtp`, `pseudoAuthenticate` | Критический путь логина | **10s** (быстрый fail при сбое SSO) |
| `getEvents` | Long-polling | **90s** (server держит ≤ 75s + 15s буфер) |
| `sendSystemData`, `touchSessions` и пр. | Фоновые операции | **30s** (default) |

`SSOClientConfig.endpointParameter(...)` позволяет задать override per endpoint:

```java
SSOClientConfig config = SSOClientConfig.builder()
    .baseUrl(url)
    .subsystemName(subsystem)
    .subsystemToken(token)
    .defaultParameters(HttpRequestParameters.builder()
        .timeouts(new HttpTimeouts(5_000, 30_000)).build())

    .endpointParameter(Endpoint.AUTHENTICATE,
        HttpRequestParameters.builder()
            .timeouts(new HttpTimeouts(5_000, 10_000)).build())

    .endpointParameter(Endpoint.GET_EVENTS,
        HttpRequestParameters.builder()
            .timeouts(new HttpTimeouts(5_000, 90_000)).build())

    .build();
```

> Согласуйте `GET_EVENTS` socket timeout с `waitTimeMs` в `GetEventsRequest`:
> `waitTimeMs` должен быть **меньше** socket timeout минимум на 10-15 секунд, иначе
> при сетевой задержке клиент закроет соединение раньше чем сервер ответит.

## mTLS

mTLS настраивается через `HttpRequestParameters.sslSocketFactory` и `hostnameVerifier`.
Постройте `SSLSocketFactory` через `JdkSslSocketFactoryBuilder` (из `superfly-httpclient-ssl`)
и передайте в `HttpRequestParameters`:

```java
SSLSocketFactory ssl = JdkSslSocketFactoryBuilder.buildSocketFactory(
    keyStoreUrl,   keyStorePassword,
    trustStoreUrl, trustStorePassword);

HttpRequestParameters params = HttpRequestParameters.builder()
    .timeouts(new HttpTimeouts(5_000, 30_000))
    .sslSocketFactory(ssl)
    .hostnameVerifier(JdkSslSocketFactoryBuilder.buildCnHostnameVerifier("superfly-cn"))
    .build();
```

См. [SSL/mTLS Integration](ssl-mtls.md).

## Безопасность URL (HTTPS-only)

`SSOClientConfig` по умолчанию **запрещает** HTTP-схему — соответствие PCI DSS 4.2.1.
Попытка передать `http://...` в `baseUrl` бросает `IllegalArgumentException`.

Для локальной разработки/тестов можно разрешить HTTP через JVM property:

```
-Dsuperfly.client.allowInsecureScheme=true
```

При этом в логи выводится `WARN`. **Использовать в production запрещено.**

## Обработка ошибок

Сервер возвращает ошибки в формате `ExceptionWrapper`:

```json
{
  "exceptionClass": "com.payneteasy.superfly.api.exceptions.UserExistsException",
  "message": "User foo already exists",
  "detailMessage": "..."
}
```

`SSOHttpServiceApiClient` парсит wrapper для **любого** не-200 ответа (включая 4xx/5xx)
и бросает типизированное доменное исключение (`UserExistsException`,
`PolicyValidationException`, `SsoSystemException`, …). Если wrapper не распознан —
fallback к status-based исключению (`SsoBadRequestException`, `SsoUnauthorizedException`,
`SsoServerException`, …).

Все исключения наследуются от `SsoException extends RuntimeException` — checked обработка
не требуется.

## Lifecycle

Клиент реализует `AutoCloseable`. `close()` делегирует в transport (`IHttpClient`), если
тот тоже `AutoCloseable` — это критично для production setup с Apache HC5 или OkHttp,
где транспорт держит connection pool.

В Spring-приложении — оберните в `@Bean(destroyMethod = "close")` или используйте
`DisposableBean`:

```java
@Bean(destroyMethod = "close")
public SSOHttpServiceApiClient ssoClient(...) { ... }
```

## Декораторы (паттерн composition)

Поскольку `SSOService` — интерфейс, можно надевать decorators для cross-cutting concerns:

```java
SSOService base = new SSOHttpServiceApiClient(transport, config, serializer);
SSOService withMetrics = new MetricsSSOService(base, meterRegistry);
SSOService withRetry   = new RetryingSSOService(withMetrics, retryPolicy);  // for idempotent only
```

`MetricsSSOService` (SSO-2) и `RetryingSSOService` (SSO-4) — отдельные milestone из
[roadmap](../.ai-factory/ROADMAP.md). Apache HC5 транспорт — SSO-3.

## Ссылки

- [API Reference](api.md) — серверная сторона: эндпоинты и формат ответов
- [SSL/mTLS Integration](ssl-mtls.md) — настройка взаимной аутентификации
- [Integration Guide](integration-guide.md) — Spring Security обвязка
