[← Руководство по интеграции](integration-guide.md) · [Back to README](../README.md) · [Выпуск релиза →](releasing.md)


# Миграция на модули Superfly Client EE8 / EE10

## Обзор

С версии 2.0 клиентская часть Superfly разделена на модули по стеку:

- **superfly-client-core** — общая логика и абстракции сессий без зависимостей на Servlet API (подходит и для EE8, и для EE10).
- **superfly-client-ee10** — фильтры, слушатели и `JakartaHttpSessionWrapper` для приложений на **Jakarta EE 10** (`jakarta.servlet.*`).
- **superfly-client-ee8** — те же фильтры и слушатели для приложений на **Java EE 8** (`javax.servlet.*`), плюс `JavaxHttpSessionWrapper`.

Фасадных артефактов **superfly-client** и **superfly-spring-security** (без суффикса) больше нет — подключайте модуль под свой стек (см. таблицу ниже).

**Java:** все модули, включая EE8, собираются и работают на **JDK 21** (`release` не понижается). EE8-модули нужны приложениям, которые остаются на javax-стеке (`javax.servlet.*`, Spring 5, Wicket 8), а не приложениям на старых JDK.

## Breaking changes

Относительно версий до 2.0-3:

1. **`SSOHttpServiceApiClient`** (`superfly-remote-api`): класс стал `final` (наследоваться нельзя — оборачивайте через `SSOService`-декоратор), а конструктор `(HttpRequestParameters, baseUrl, subsystemName, subsystemToken, ApiSerializationManager)` заменён на `(IHttpClient, SSOClientConfig, ApiSerializationManager)`. Транспорт теперь внедряется снаружи, клиент `AutoCloseable`. Переход — в [SSO HTTP Client](sso-http-client.md).
2. **`http-client-impl` больше не приходит транзитивно** из `superfly-remote-api` (он зависит только от `http-client-api`). Нужна реализация — подключите `superfly-httpclient-hc5` (`ApacheHC5HttpClient`) или добавьте `com.payneteasy.http-client:http-client-impl` явно. `superfly-client-web-security` по-прежнему тянет `http-client-impl` для URL-конструктора `ExternalFormSecurityFilter`.
3. **Только HTTPS:** `SSOClientConfig` (и URL-конструктор `ExternalFormSecurityFilter`) отвергают `http://` с `IllegalArgumentException`. Для локальной разработки — `-Dsuperfly.client.allowInsecureScheme=true` (в лог пишется WARN); в production не использовать.
4. **Удалены `HttpClientFactoryBean` и `StoresAndSSLConfig`** (`superfly-client-opt`, Apache Commons HttpClient 3.x) и модуль `superfly-httpclient-ssl`. Замена: `IHttpClient` на `ApacheHC5HttpClient` и SSL-хелперы `JdkSslSocketFactoryBuilder` из `superfly-httpclient-hc5` (пакет `com.payneteasy.httpclient.contrib.ssl`). В `superfly-client-opt` бин `httpClientFactoryBean` заменён бином `notificationHttpClient`; mTLS включается свойствами `superfly.notification.http.*` (см. [SSL / mTLS](ssl-mtls.md#mtls-для-уведомлений-superfly-client-opt)). Тип keystore по умолчанию теперь `PKCS12` (был JKS; JKS-файлы JDK по-прежнему открывает, явный тип — параметр `keystore-type`).
5. **HC5-транспорт:** автоматические повторы и редиректы отключены; per-request `sslSocketFactory`/`hostnameVerifier`/`trustManager` в `HttpRequestParameters` дают `IllegalArgumentException` (SSL задаётся в `ApacheHC5HttpClient.builder()`). См. [Apache HC5 Transport](httpclient-hc5.md).
6. **Фасады удалены:** `superfly-client` → `superfly-client-ee10` (Jakarta) или `superfly-client-ee8` (javax); `superfly-spring-security` → `superfly-spring-security-ee10`.
7. **Remote-auth (`check-password`/`check-otp`):** новые ключи подсистем — `RSA_OAEP`, а на ошибки расшифровки действует лимит 20 в минуту на подсистему. Коды и формат ответов прежние; подробности и примеры шифрования — в [API Reference](api.md#шифрование).
8. **Long-poll `getEvents`:** `waitTimeMs` ограничен сервером 75 секундами.
9. **Курсор `getEvents` (без обратной совместимости):** `GetEventsRequest.lastEventTime` (`Date`) заменён на `lastEventId` (`Long`), builder `lastEventTime(Date)` → `lastEventId(Long)`, JSON-поле `"lastEventTime"` → `"lastEventId"` (число). `null` — с начала. Сервер отдаёт события с `eventId > lastEventId` по возрастанию `eventId`; следующий запрос делайте с максимальным `SSOEvent.eventId` из последнего ответа. Обновляйте клиент и сервер одновременно.
10. **Проверка подсистемы:** подсистема не может указать чужой `subsystemIdentifier`/`subsystemHint`/`GetEventsRequest.subsystemName`/`roleGrants[].subsystemIdentifier` — `202` + `SsoAuthException` ([подробнее](api.md#подмена-подсистемы)). `GetEventsRequest.subsystemName` теперь учитывается.
11. **Remote-auth:** неполный путь вроде `POST /sso/check/check-password` даёт `404` (`type: NOT_FOUND`) вместо `500`, ошибки без `Accept` или с `*/*` теперь JSON (раньше XML).
12. **Старт `getEvents` с хвоста:** новый метод `SSOService.getLastEventId()` (`Long`, эндпоинт `getLastEventId`) — максимальный `eventId` событий вызывающей подсистемы, `0` без событий. Клиенты, которые раньше стартовали с `lastEventTime = now()`, теперь стартуют с `lastEventId = getLastEventId()`; `null` переиграет всю историю. Реализации `SSOService` вне superfly (моки, обёртки) должны добавить метод.

## Обновление

Порядок обновления сервера:

1. **БД.** При обновлении с 1.7-36/37/38 запустите миграции начиная с R1.7.4 — она идемпотентна и добавляет `events.subsystem_id`, индекс и внешний ключ:

   ```bash
   cd superfly-sql/mi
   version_from=R1.7.4 bash all_mi.sh
   ```

   Скрипты подключаются переменными `SSO_DB_*` (см. [Установка и запуск](getting-started.md#обновление-существующей-базы)). Миграции теперь прерываются на первой ошибке — не игнорируйте ненулевой код возврата. Хранимые процедуры переустанавливаются отдельно (`superfly-sql/src/all-proc.sh`); после смены курсора `getEvents` это обязательно — сигнатура `ui_get_events` теперь `(i_last_event_id bigint, i_limit int, i_subsystem_name varchar(32))`.
2. **Пароли.** При обновлении с 2.0-1/2.0-2 под политикой `none` сбросьте пароли пользователей: хеширование с солью (`users.salt`) под `none` восстановлено, а в 2.0-1/2.0-2 пароли под `none` могли записываться без соли — такие хеши могут не пройти проверку. Под политикой `pcidss` (значение в `web.xml` по умолчанию) ничего делать не нужно.
3. **События.** События без подсистемы больше не отдаются клиентам, запрашивающим события по имени подсистемы. `PASSWORD_RESET` теперь пишется отдельной записью на каждую подсистему, в которой у пользователя есть роли.
4. **Remote-auth.** Существующие подсистемы остаются на `RSA` (PKCS#1). Чтобы перейти на OAEP — перегенерируйте ключ на странице редактирования подсистемы и переведите клиента на OAEP ([как шифровать](api.md#шифрование)).
5. **Зависимости.** Spring Security EE10-стека обновлён 6.4.13 → 6.5.11 (EE8 остаётся на 5.8.16), BouncyCastle (`bcprov`, `bcutil`, `bcpkix`) выровнен на 1.85.
6. **CSP.** Origin `landingUrl`/`subsystemUrl` всех подсистем добавляются в `form-action`, а `loginFormCssUrl` — в `style-src`. Список обновляется в течение 5 минут или сразу после правки подсистемы в админке.

## Выбор артефакта

| Стек приложения | Подключаемый артефакт |
|-----------------|------------------------|
| Jakarta EE 10, Spring 6, сервлеты `jakarta.servlet.*` | `superfly-client-ee10` |
| Java EE 8, сервлеты `javax.servlet.*` | `superfly-client-ee8` |

## Минимальные изменения

### Проекты на Jakarta EE 10

Артефакт `superfly-client` удалён — замените его на EE10-адаптер:

```xml
<dependency>
    <groupId>com.payneteasy.superfly</groupId>
    <artifactId>superfly-client-ee10</artifactId>
    <version>${superfly.version}</version>
</dependency>
```

Имена классов и пакетов те же (фильтры в `com.payneteasy.superfly.client.session.*`, `SuperflyLogoutFilter` и т.д.; абстракции сессий `SessionMappingLocator` и `HttpSessionWrapper` — в `com.payneteasy.superfly.common.session`), конфигурация фильтров в `web.xml` или Spring не меняется.

### Проекты на Java EE 8 (javax)

EE8-адаптер собирается и запускается на JDK 21; нужен тем, кто остаётся на `javax.servlet.*`. Подключить только его, без `superfly-client-ee10`:

```xml
<dependency>
    <groupId>com.payneteasy.superfly</groupId>
    <artifactId>superfly-client-ee8</artifactId>
    <version>${superfly.version}</version>
</dependency>
```

Имена классов и пакетов совпадают с EE10-вариантом; в коде используются только `javax.servlet.*`. Конфигурация фильтров (например, `SuperflyLogoutFilter`, `AbstractSessionTouchingFilter`) остаётся той же.

### Опциональные возможности (superfly-client-opt)

Если используется **superfly-client-opt** (например, `XmlActionDescriptionCollector`, `ScanningActionDescriptionCollector`, Spring-конфигурация `HttpClientSpringConfiguration`):

- Коллекторы действий перенесены в **superfly-client-core**; при зависимости от **superfly-client-opt** они подтягиваются через **superfly-client-core**.
- `superfly-client-opt` не зависит от Servlet API (зависит от `superfly-client-core`, `superfly-remote-api` и `superfly-httpclient-hc5`), поэтому его можно подключать и в EE8-, и в EE10-проектах.

## Сессии и обёртки

- **SessionMappingLocator**, **HttpSessionWrapper** и реализация маппинга сессий (`HashMapBackedSessionMapping`) живут в **superfly-common** (без javax/jakarta).
- Реализации под конкретный API:
  - **JakartaHttpSessionWrapper** — в **superfly-client-ee10** (`jakarta.servlet.http.HttpSession`).
  - **JavaxHttpSessionWrapper** — в **superfly-client-ee8** (`javax.servlet.http.HttpSession`).

При использовании фильтра привязки сессии (например, в Spring Security) в EE8-проекте регистрируйте в маппинге **JavaxHttpSessionWrapper**, в EE10 — **JakartaHttpSessionWrapper**.

## Wicket-интеграция

### Jakarta EE 10 / Wicket 10

Для приложений на Wicket 10 (`jakarta.servlet.*`) используйте существующий модуль:

```xml
<dependency>
    <groupId>com.payneteasy.superfly</groupId>
    <artifactId>superfly-wicket</artifactId>
    <version>${superfly.version}</version>
</dependency>
```

Публичный контракт, ориентированный на внешние приложения:

- `com.payneteasy.superfly.wicket.PageInterceptingRequestMapper`
- `com.payneteasy.superfly.wicket.PageInterceptingRequestMapperLogic`
- `com.payneteasy.superfly.wicket.InterceptionDecisions`

`SessionStoreUrlWebRequestCodingStrategy` удалён как устаревший и неиспользуемый.

### Java EE 8 / Wicket 8

Для приложений на Wicket 8 (`javax.servlet.*`) доступен отдельный экспортный модуль:

```xml
<dependency>
    <groupId>com.payneteasy.superfly</groupId>
    <artifactId>superfly-wicket-ee8</artifactId>
    <version>${superfly.version}</version>
</dependency>
```

API и пакеты те же (`com.payneteasy.superfly.wicket.*`), классы совместимы с Wicket 8.  
Типовой пример использования можно посмотреть в Paynet (`PaynetUIApplication`, настройка `PageInterceptingRequestMapper`).

> **Важно (с 2.0-3):** зависимость на `org.apache.wicket:wicket` (8.x) в `superfly-wicket-ee8`
> объявлена со `scope=provided` и **не приносится транзитивно**. Wicket 8 (вместе с его
> встроенным jQuery) подключает само приложение (см. [«Контракт provided-scope»](#контракт-provided-scope-для-ee8-потребителей)).

## Spring Security-интеграция

### Общий слой (core)

Общий набор типов (не зависящих от Servlet API), пригодных как для EE8, так и для EE10, вынесен в отдельный модуль:

- пакет `com.payneteasy.superfly.security.authentication.*` — токены (`SSOUserAuthenticationToken`, `UsernamePasswordCheckedToken`, `OTPCheckedToken`, `OtpUsernamePasswordCheckedToken`, `SSOAuthenticationRequest` и др.);
- пакет `com.payneteasy.superfly.security.*`:
  - `StringTransformer`, `UppercaseTransformer`;
  - `RoleSource`, `SSOActionRoleSource`, `SSORoleRoleSource`, `CompoundRoleSource`;
  - `CompoundAuthenticationProvider`, `SuperflyUsernamePasswordAuthenticationProvider`, `SuperflyOTPAuthenticationProvider`, `SuperflySelectRoleAuthenticationProvider`, `SuperflyMultiMockAuthenticationProvider`, `SuperflyMockAuthenticationProvider`, `SuperflySSOAuthenticationProvider`;
  - валидаторы и post-processor-ы: `CompoundAuthenticationValidator`, `AuthenticationPostProcessor`, `SSOUserShortCircuitingPostProcessor`, `IdAuthenticationPostProcessor`, `CompoundLatestAuthUnwrappingPostProcessor`;
- карта действий: `com.payneteasy.superfly.security.mapbuilder.*` (`ActionsSource`, `CollectingActionsSource`, `ResourceActionsSource`, `SeparateActionsMapBuilder`).

Эти классы находятся в артефакте:

```xml
<dependency>
    <groupId>com.payneteasy.superfly</groupId>
    <artifactId>superfly-spring-security-core</artifactId>
    <version>${superfly.version}</version>
</dependency>
```

и могут использоваться как в EE8-, так и в EE10-приложениях (версии Spring/Spring Security задаёт само приложение и целевой стек — EE8 или EE10).

### EE10 / Jakarta (web-слой)

Для приложений на Jakarta EE 10 используются web-компоненты из модуля `superfly-spring-security-ee10`, завязанные на `jakarta.servlet.*` и зависящие от core:

- фильтры и entry point-ы:
  - `SuperflyUsernamePasswordAuthenticationProcessingFilter`
  - `SuperflyOTPAuthenticationProcessingFilter`
  - `SuperflySelectRoleAuthenticationProcessingFilter`
  - `SuperflySSOAuthenticationProcessingFilter`
  - `MultiStepLoginUrlAuthenticationEntryPoint`
  - `TwoStepAuthenticationProcessingFilter`
  - `TwoStepAuthenticationProcessingFilterEntryPoint`
  - `InsufficientAuthenticationHandlingFilter`
- CSRF и сессии:
  - `CsrfValidator`, `CsrfValidatorImpl`
  - `SSOUserSessionBindFilter`
  - `UnauthorizedFailureHandler`.

Они ожидают стек **Spring 6 / Spring Security 6 + Jakarta Servlet API**. Модуль зависит от `superfly-spring-security-core` и приносит его транзитивно — отдельно core подключать не нужно.

Рекомендуемая зависимость:

```xml
<dependency>
    <groupId>com.payneteasy.superfly</groupId>
    <artifactId>superfly-spring-security-ee10</artifactId>
    <version>${superfly.version}</version>
</dependency>
```

### EE8 / javax

Для Java EE 8 приложений теперь доступен отдельный экспортный модуль web-адаптера под `javax.servlet.*`, поверх общего core:

1. Подключить core-типы:

   ```xml
   <dependency>
       <groupId>com.payneteasy.superfly</groupId>
       <artifactId>superfly-spring-security-core</artifactId>
       <version>${superfly.version}</version>
   </dependency>
   ```

2. Подключить EE8 web-адаптер:

   ```xml
   <dependency>
       <groupId>com.payneteasy.superfly</groupId>
       <artifactId>superfly-spring-security-ee8</artifactId>
       <version>${superfly.version}</version>
   </dependency>
   ```

   Внутри используются:

   - `javax.servlet.*` (Servlet API 4.0.x);
   - Spring 5.x / Spring Security 5.8.x (javax-совместимые артефакты).

   > **Важно (с 2.0-3):** framework-зависимости в `superfly-spring-security-ee8` объявлены
   > со `scope=provided` и **не приносятся транзитивно** в ваш проект. Spring 5.x / Spring
   > Security 5.8.x вы подключаете сами (см. [«Контракт provided-scope»](#контракт-provided-scope-для-ee8-потребителей)).

3. Использовать те же классы web-слоя, что и в Jakarta-варианте, но из модуля EE8:

- фильтры и entry point-ы:
  - `SuperflyUsernamePasswordAuthenticationProcessingFilter`
  - `SuperflyOTPAuthenticationProcessingFilter`
  - `SuperflySelectRoleAuthenticationProcessingFilter`
  - `SuperflySSOAuthenticationProcessingFilter`
  - `MultiStepLoginUrlAuthenticationEntryPoint`
  - `TwoStepAuthenticationProcessingFilter`
  - `TwoStepAuthenticationProcessingFilterEntryPoint`
  - `InsufficientAuthenticationHandlingFilter`
- CSRF и сессии:
  - `CsrfValidator`, `CsrfValidatorImpl`
  - `SSOUserSessionBindFilter`
  - `UnauthorizedFailureHandler`.

При этом важно:

- **не тащить в classpath `jakarta.servlet.*`** — все web-компоненты в EE8-приложении должны работать только на `javax.servlet.*`;
- переиспользовать общий core (`authentication.*`, провайдеры, валидаторы, mapbuilder’ы) независимо от стека.

Типовой пример такой интеграции уже реализован в Paynet (`SpringUIWebSecurityConfiguration`, `CustomUsernamePasswordAuthenticationProcessingFilter` и др.), который использует Superfly как удалённый SSO-сервер; теперь вместо кастомных фильтров можно переходить на стандартные классы из `superfly-spring-security-ee8`.

## Контракт provided-scope для EE8-потребителей

Начиная с **2.0-3**, экспортные EE8-модули перестали навязывать свой framework-стек
потребителю. Это меняет публикуемый контракт `superfly-spring-security-ee8` и
`superfly-wicket-ee8`.

### Что изменилось

Раньше эти модули объявляли Spring 5.x / Spring Security 5.8.x / Wicket 8 со `scope=compile`,
из-за чего они **транзитивно навязывались** любому потребителю. Эти javax-линии (Spring 5.3.x,
Spring Security 5.8.x, Wicket 8 + встроенный старый jQuery) находятся на EOL, известные CVE по ним
не закрыты и закрыты, скорее всего, не будут. Теперь все framework-зависимости в обоих модулях
объявлены со `scope=provided`:

- они остаются на собственном compile/test-classpath модуля (компиляция адаптеров против javax-5.x работает);
- но **не попадают** на compile/runtime-classpath потребителя — то есть устаревший javax-стек
  больше не «прилетает» к вам вместе с Superfly.

Тот же приём уже применялся к `javax.servlet-api`: его всегда приносило само приложение/контейнер.

> **Боевой EE10-сервер не затронут.** `superfly-web` работает на Spring 6 / Wicket 10; эти CVE
> там не используются. Изменение касается только публикуемых EE8-библиотек для внешних
> javax-потребителей.

### Что теперь обязан добавить потребитель

Поскольку Spring 5.x / Spring Security 5.8.x / Wicket 8 больше не приходят транзитивно,
**EE8-приложение должно объявить их явно** (версии — javax-совместимые линии). Минимальный набор:

```xml
<!-- Spring Security EE8 web-адаптер: framework-стек теперь provided, добавьте его сами -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-web</artifactId>
    <version>5.8.16</version>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-config</artifactId>
    <version>5.8.16</version>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-core</artifactId>
    <version>5.8.16</version>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-web</artifactId>
    <version>5.3.39</version>
</dependency>
<!-- spring-core / spring-beans / spring-context / spring-aop / spring-expression 5.3.x
     придут транзитивно вместе с указанными выше артефактами -->

<!-- Wicket EE8 (если используется superfly-wicket-ee8): -->
<dependency>
    <groupId>org.apache.wicket</groupId>
    <artifactId>wicket</artifactId>
    <version>8.18.0</version>
    <type>pom</type>
</dependency>
```

Если у приложения уже есть собственный javax-совместимый Spring/Wicket-стек (как у Paynet),
ничего добавлять не нужно — его и подхватит provided-зависимость.

### Гарантия от регрессии

В оба EE8-модуля встроен `maven-enforcer` (`bannedDependencies`), который **роняет сборку**, если
какая-либо из этих зависимостей снова окажется в scope `compile`/`runtime` (т.е. начнёт течь
транзитивно). Остаточные находки OWASP по самим provided-либам подавлены **узкими version-pinned
suppressions** (`src/main/dependency-check/suppressions.xml`) с обоснованием «provided, не
отгружается транзитивно, не рантайм EE10-сервера». Пины строго привязаны к 5.x/Wicket-8, поэтому
открытые находки по in-line EE10-стеку (Spring 6 / Spring Security 6.5.x) не маскируются.

## Итог

- **Клиент:** для EE10 используйте `superfly-client-ee10`; для EE8 — `superfly-client-ee8`. Фасада `superfly-client` больше нет.
- **Wicket:** для Wicket 10 (Jakarta) — `superfly-wicket`; для Wicket 8 (EE8) — `superfly-wicket-ee8`.
- **Spring Security:**
  - общий core берётся из `superfly-spring-security-core`;
  - для EE10 можно использовать готовые web-компоненты на `jakarta.servlet.*` из `superfly-spring-security-ee10`;
  - для EE8 — использовать общий core и готовый web-адаптер `superfly-spring-security-ee8` на `javax.servlet.*` и Spring Security 5.8.x.
- **provided-scope (с 2.0-3):** framework-стек EE8-модулей (Spring 5.x / Spring Security 5.8.x / Wicket 8) больше не приходит транзитивно — потребитель подключает его сам; см. [«Контракт provided-scope»](#контракт-provided-scope-для-ee8-потребителей).

## See Also

- [SSO HTTP Client](sso-http-client.md) — новый конструктор `SSOHttpServiceApiClient`
- [Apache HC5 Transport](httpclient-hc5.md) и [SSL / mTLS](ssl-mtls.md) — транспорт и mTLS
- [API Reference](api.md) — пути, формат ошибок, шифрование remote-auth
- [Руководство по интеграции](integration-guide.md) — подключение к клиентскому приложению
- [Конфигурация](configuration.md) — настройка сервера Superfly
