# SSO-2: Метрики и health indicator (`MetricsSSOService`)

> Модуль: `superfly-remote-api`  
> Требует: `io.micrometer:micrometer-core` (optional dep; тянется только если нужен)

---

## Обзор

`MetricsSSOService` — decorator поверх `SSOService`, который записывает Micrometer-метрики
для каждого SSO-вызова. Позволяет:

- отслеживать latency и error rate в реальном времени;
- строить Actuator health indicator без зависимости на Micrometer в consumer-модуле;
- подключать Prometheus/Grafana без изменения бизнес-кода.

---

## Метрики

| Имя | Тип | Теги | Описание |
|-----|-----|------|----------|
| `sso.call.duration` | Timer | `operation`, `status` | Время каждого SSO-вызова. `status=success` / `status=error` |
| `sso.errors.total` | Counter | `operation` | Число вызовов, завершившихся исключением |
| `sso.last.success.epoch.ms` | Gauge | — | Epoch-мс последнего успешного вызова; 0 до первого успеха |

Значения `operation` соответствуют именам методов `SSOService` (camelCase):
`authenticate`, `checkOtp`, `getEvents`, `registerUser`, и т.д.

---

## Как подключить (plain Java, без Spring Boot)

```java
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import com.payneteasy.superfly.api.metrics.MetricsSSOService;

// Обычный SSOService (например SSOHttpServiceApiClient)
SSOService rawService = buildRawSsoService();

// Оборачиваем в decorator
MeterRegistry registry = new SimpleMeterRegistry(); // или PrometheusMeterRegistry
SSOService ssoService = new MetricsSSOService(rawService, registry);

// Используем как обычный SSOService
SSOUser user = ssoService.authenticate(request);
```

`MetricsSSOService` реализует `AutoCloseable` и делегирует `close()` в underlying service
если тот тоже `AutoCloseable`:

```java
// в Spring: @Bean(destroyMethod = "close")
// вручную:
((AutoCloseable) ssoService).close();
```

---

## Получение health snapshot

```java
MetricsSSOService metrics = (MetricsSSOService) ssoService;
SSOHealthSnapshot snap = metrics.getHealthSnapshot();

System.out.println("Healthy: " + snap.isHealthy());
System.out.println("Last success: " + snap.getLastSuccessAt());
System.out.println("Errors: " + snap.getTotalErrors() + "/" + snap.getTotalCalls());
```

`isHealthy()` возвращает `true` если:
- был хотя бы один успешный вызов, И
- последний успех — не старше 5 минут, И
- error rate < 50%

---

## Spring Boot Actuator: `SSOConnectionHealthIndicator`

Добавьте в `paynet-ui-web` (требует `spring-boot-starter-actuator`):

```java
package com.payneteasy.paynet.ui.web.spring.health;

import com.payneteasy.superfly.api.metrics.MetricsSSOService;
import com.payneteasy.superfly.api.metrics.SSOHealthSnapshot;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class SSOConnectionHealthIndicator implements HealthIndicator {

    private final MetricsSSOService metricsSSOService;

    public SSOConnectionHealthIndicator(MetricsSSOService metricsSSOService) {
        this.metricsSSOService = metricsSSOService;
    }

    @Override
    public Health health() {
        SSOHealthSnapshot snap = metricsSSOService.getHealthSnapshot();

        if (snap.isHealthy()) {
            return Health.up()
                    .withDetail("lastSuccessAt",  snap.getLastSuccessAt())
                    .withDetail("totalCalls",     snap.getTotalCalls())
                    .withDetail("errorRate",      errorRate(snap))
                    .build();
        } else {
            return Health.down()
                    .withDetail("lastSuccessAt", snap.getLastSuccessAt())
                    .withDetail("totalErrors",   snap.getTotalErrors())
                    .withDetail("totalCalls",    snap.getTotalCalls())
                    .withDetail("errorRate",     errorRate(snap))
                    .build();
        }
    }

    private String errorRate(SSOHealthSnapshot snap) {
        if (snap.getTotalCalls() == 0) return "n/a";
        long pct = snap.getTotalErrors() * 100 / snap.getTotalCalls();
        return pct + "%";
    }
}
```

Для инжекции `MetricsSSOService` вместо `SSOService` в `SpringUISuperflyClientConfiguration`:

```java
// Раньше:
@Bean(destroyMethod = "close")
public SSOService ssoService(IHttpClient ssoHttpClient) { ... }

// Теперь:
@Bean(destroyMethod = "close")
public MetricsSSOService ssoService(IHttpClient ssoHttpClient, MeterRegistry meterRegistry) {
    SSOService raw = createRawSsoService(ssoHttpClient);
    return new MetricsSSOService(raw, meterRegistry);
}
```

Actuator endpoint: `GET /actuator/health/sSOConnection`

---

## Prometheus

Пример конфигурации `application.properties` для Spring Boot:

```properties
management.endpoints.web.exposure.include=health,prometheus
management.endpoint.health.show-details=always
```

Запрос к `/actuator/prometheus` вернёт:

```
# HELP sso_call_duration_seconds_count Total number of SSO calls
# TYPE sso_call_duration_seconds_count counter
sso_call_duration_seconds_count{operation="authenticate",status="success"} 42.0
sso_call_duration_seconds_count{operation="authenticate",status="error"} 2.0

# HELP sso_errors_total Total SSO errors
# TYPE sso_errors_total counter
sso_errors_total{operation="authenticate"} 2.0
sso_errors_total{operation="getEvents"} 0.0

# HELP sso_last_success_epoch_ms Epoch-milliseconds of last successful SSO call
# TYPE sso_last_success_epoch_ms gauge
sso_last_success_epoch_ms 1.716800000000E12
```

Пример Grafana query:

```promql
# P95 latency по операциям (последние 5 минут)
histogram_quantile(0.95,
  sum(rate(sso_call_duration_seconds_bucket[5m])) by (le, operation)
)

# Error rate в %
100 * rate(sso_errors_total[5m]) / rate(sso_call_duration_seconds_count[5m])
```

---

## Связанные документы

- [SSO HTTP Client](sso-http-client.md) — детальное описание `SSOHttpServiceApiClient` (SSO-1a/1b)
- [Apache HC5 transport](httpclient-hc5.md) — connection pool transport (SSO-3)
- [SSL/mTLS](ssl-mtls.md) — настройка mTLS между paynet и superfly

---

## Дорожная карта

- **SSO-3** ✅ `ApacheHC5HttpClient` — замена транспорта (connection pool, `AutoCloseable`)
- **SSO-4** (planned) — retry-декоратор поверх `MetricsSSOService`
