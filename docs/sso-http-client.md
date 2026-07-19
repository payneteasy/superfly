[← API Reference](api.md) · [Back to README](../README.md) · [Руководство по интеграции →](integration-guide.md)

# SSO HTTP Client

`SSOHttpServiceApiClient` — клиентская реализация `SSOService` в `superfly-remote-api`,
работающая поверх HTTP. Используется paynet и другими подсистемами для вызова RPC-эндпоинтов
SSO-сервера.

## Принципы

- **Минимум зависимостей.** superfly не тянет Micrometer, retry-библиотеки или health-framework.
  Эти cross-cutting concerns решаются на стороне consumer через decorator pattern.
- **Чистый интерфейс `SSOService`.** Domain port не знает о транспорте, метриках, retry.
- **Композиция вместо наследования.** Транспорт инжектируется через конструктор.
- **HTTPS-only by default.** PCI DSS 4.2.1 compliance.

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
                      (legacy, JDK)         (production)
```

Транспорт инжектируется через конструктор — клиент не зависит от конкретной реализации HTTP.
Это позволяет подменять `IHttpClient` без правки `SSOHttpServiceApiClient`: для тестов
(EasyMock/Mockito), для production (Apache HC5 с connection pooling), для отладки.

## Quickstart (5 минут)

```java
// 1. Build transport (production: Apache HC5 + mTLS)
IHttpClient transport = ApacheHC5HttpClient.builder()
    .sslContext(JdkSslSocketFactoryBuilder.buildSslContext(keyStoreUrl, ksPwd, trustStoreUrl, tsPwd))
    .hostnameVerifier(JdkSslSocketFactoryBuilder.buildCnHostnameVerifier("superfly-server"))
    .build();

// 2. Build config
SSOClientConfig config = SSOClientConfig.builder()
    .baseUrl("https://superfly.example.com/sso.service")
    .subsystemName("paynet-ui")
    .subsystemToken(System.getenv("SSO_TOKEN"))
    .defaultParameters(HttpRequestParameters.builder()
        .timeouts(new HttpTimeouts(5_000, 20_000))
        .build())
    .endpointParameter(Endpoint.AUTHENTICATE,
        HttpRequestParameters.builder()
            .timeouts(new HttpTimeouts(3_000, 8_000)).build())
    .endpointParameter(Endpoint.GET_EVENTS,
        HttpRequestParameters.builder()
            .timeouts(new HttpTimeouts(3_000, 90_000)).build())
    .build();

// 3. Build client
SSOService sso = new SSOHttpServiceApiClient(transport, config, new ApiSerializationManager());

// 4. Use
SSOUser user = sso.authenticate(new AuthenticateRequest("john", "secret"));

// 5. On shutdown — закрывает transport pool
((AutoCloseable) sso).close();
```

`SSOHttpServiceApiClient` thread-safe и предназначен для долгого жизненного цикла —
создавайте один экземпляр на приложение и переиспользуйте.

## Per-endpoint таймауты

В production разные эндпоинты имеют разные характеристики:

| Эндпоинт | Назначение | Рекомендуемые connect / socket |
|---|---|---|
| `AUTHENTICATE`, `CHECK_OTP`, `PSEUDO_AUTHENTICATE` | Критический путь логина | **3s / 8s** (fast fail при сбое SSO) |
| `GET_EVENTS` | Long-polling | **3s / 90s** (server держит ≤ 75s + 15s буфер) |
| default (остальные) | Фоновые операции | **5s / 20s** |

`SSOClientConfig.endpointParameter(...)` позволяет задать override per endpoint
(см. Quickstart выше).

> **Важно про GET_EVENTS:** socket timeout должен быть **больше** server-side `waitTimeMs`
> минимум на 10-15 секунд, иначе при сетевой задержке клиент закроет соединение раньше
> чем сервер ответит. Согласуйте с `GetEventsRequest.waitTimeMs`.

## Безопасность

### HTTPS-only

`SSOClientConfig` по умолчанию **запрещает** HTTP-схему — соответствие PCI DSS 4.2.1.
Попытка передать `http://...` в `baseUrl` бросает `IllegalArgumentException`.

Для локальной разработки можно разрешить HTTP через JVM property:

```
-Dsuperfly.client.allowInsecureScheme=true
```

При этом в лог выводится `WARN`. **Использовать в production запрещено.** Рекомендуется
в production setup явно проверять отсутствие этого флага в стартап-скрипте контейнера.

### mTLS

mTLS настраивается через `ApacheHC5HttpClient.Builder.sslContext(...)` — SSL-контекст
создаётся **один раз** на клиент, не per-request:

```java
SSLContext ssl = JdkSslSocketFactoryBuilder.buildSslContext(
    keyStoreUrl,   keyStorePassword,
    trustStoreUrl, trustStorePassword);

ApacheHC5HttpClient transport = ApacheHC5HttpClient.builder()
    .sslContext(ssl)
    .hostnameVerifier(JdkSslSocketFactoryBuilder.buildCnHostnameVerifier("superfly-cn"))
    .build();
```

См. [SSL/mTLS Integration](ssl-mtls.md) и [HC5 transport](httpclient-hc5.md).

### Logging policy

- `DEBUG` логи **не содержат тело** запроса/ответа — только длину (`body.length=N`).
- `DEBUG` логи **не содержат пароли** и `subsystemToken` (покрыто тестом
  `SSOHttpServiceApiClientSecurityTest`).
- Если будете добавлять новые логи — соблюдайте эту policy, не логируйте payload.

### Token lifecycle

`subsystemToken` хранится immutable в `SSOClientConfig` и компилируется в заголовки один раз.
**Ротация токена = рестарт приложения** — это явное архитектурное решение для простоты.
Heap-dump risk минимизируется на уровне продакшен-окружения (restricted `/tmp` perms,
`-XX:+HeapDumpOnOutOfMemoryError` в защищённую директорию).

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
`PolicyValidationException`, …). Если wrapper не распознан — fallback к status-based
исключению (`SsoBadRequestException`, `SsoUnauthorizedException`, `SsoServerException`).

Все исключения наследуются от `SsoException extends RuntimeException` — checked обработка
не требуется.

## Lifecycle

Клиент реализует `AutoCloseable`. `close()` делегирует в transport (`IHttpClient`), если
тот тоже `AutoCloseable` — это критично для production setup с Apache HC5, где транспорт
держит connection pool.

В Spring-приложении — оберните в `@Bean(destroyMethod = "close")`:

```java
@Bean(destroyMethod = "close")
public SSOService ssoService(IHttpClient transport, ApiSerializationManager serializer) {
    return new SSOHttpServiceApiClient(transport, ssoConfig(), serializer);
}
```

## Метрики на стороне consumer

`SSOService` — это интерфейс. superfly **сам не предоставляет** метрики, чтобы не тянуть
зависимости. Consumer добавляет инструментирование composition-pattern'ом за ~30 строк:

```java
// в paynet / другом consumer'е
public class MetricsSSOService implements SSOService {

    private final SSOService delegate;
    private final MeterRegistry registry;

    public MetricsSSOService(SSOService delegate, MeterRegistry registry) {
        this.delegate = delegate;
        this.registry = registry;
    }

    @Override
    public SSOUser authenticate(AuthenticateRequest req) {
        return measure("authenticate", () -> delegate.authenticate(req));
    }

    // ... остальные методы аналогично — try/finally с Timer.Sample

    private <T> T measure(String op, java.util.function.Supplier<T> body) {
        Timer.Sample sample = Timer.start(registry);
        boolean ok = false;
        try {
            T result = body.get();
            ok = true;
            return result;
        } finally {
            sample.stop(Timer.builder("sso.call.duration")
                .tag("operation", op).tag("status", ok ? "success" : "error")
                .register(registry));
        }
    }
}

// composition:
SSOService raw = new SSOHttpServiceApiClient(transport, config, serializer);
SSOService instrumented = new MetricsSSOService(raw, meterRegistry);
```

> **Совет:** различайте инфраструктурные ошибки от user-rejected (`UserNotFoundException`,
> `PolicyValidationException`) — иначе error-rate dashboard будет показывать failure при
> штатных невалидных логинах. Сделать через два разных catch-block (или instanceof check)
> с разными значениями тега `status`: `error` vs `rejected`.

Аналогично делается **retry** для идемпотентных операций (`getEvents`, `getUsersWithActions`):
`RetryingSSOService implements SSOService` оборачивает другой `SSOService`. superfly не
включает retry потому что **idempotency специфична для каждого consumer'а** (paynet может
retry'ить `getEvents` 3 раза, jira-integration — один раз).

## Ссылки

- [API Reference](api.md) — серверная сторона: эндпоинты и формат ответов
- [SSL/mTLS Integration](ssl-mtls.md) — настройка взаимной аутентификации
- [Apache HC5 transport](httpclient-hc5.md) — connection pool детали
- [Integration Guide](integration-guide.md) — Spring Security обвязка
