[← Руководство по интеграции](integration-guide.md) · [Back to README](../README.md) · [Выпуск релиза →](releasing.md)


# Миграция на модули Superfly Client EE8 / EE10

## Обзор

С версии 2.0 клиентская часть Superfly разделена на модули по стеку:

- **superfly-client-core** — общая логика и абстракции сессий без зависимостей на Servlet API (подходит и для EE8, и для EE10).
- **superfly-client-ee10** — фильтры, слушатели и `JakartaHttpSessionWrapper` для приложений на **Jakarta EE 10** (`jakarta.servlet.*`).
- **superfly-client-ee8** — те же фильтры и слушатели для приложений на **Java EE 8** (`javax.servlet.*`), плюс `JavaxHttpSessionWrapper`.

Артефакт **superfly-client** оставлен для обратной совместимости: это POM-фасад, который подключает **superfly-client-ee10**. Менять группу/артефакт в существующих проектах не обязательно.

## Выбор артефакта

| Стек приложения | Подключаемый артефакт |
|-----------------|------------------------|
| Jakarta EE 10, Spring 6, сервлеты `jakarta.servlet.*` | `superfly-client-ee10` или по‑прежнему `superfly-client` |
| Java EE 8, сервлеты `javax.servlet.*` | `superfly-client-ee8` |

## Минимальные изменения

### Проекты на Jakarta EE 10 (как раньше)

Зависимость можно не менять:

```xml
<dependency>
    <groupId>com.payneteasy.superfly</groupId>
    <artifactId>superfly-client</artifactId>
    <version>${superfly.version}</version>
</dependency>
```

Либо явно перейти на EE10-адаптер:

```xml
<dependency>
    <groupId>com.payneteasy.superfly</groupId>
    <artifactId>superfly-client-ee10</artifactId>
    <version>${superfly.version}</version>
</dependency>
```

Имена классов и пакетов те же (фильтры в `com.payneteasy.superfly.client.session.*`, `SuperflyLogoutFilter` и т.д.; абстракции сессий `SessionMappingLocator` и `HttpSessionWrapper` — в `com.payneteasy.superfly.common.session`), конфигурация фильтров в `web.xml` или Spring не меняется.

### Проекты на Java EE 8 (javax)

Подключить только EE8-адаптер, без `superfly-client` и без `superfly-client-ee10`:

```xml
<dependency>
    <groupId>com.payneteasy.superfly</groupId>
    <artifactId>superfly-client-ee8</artifactId>
    <version>${superfly.version}</version>
</dependency>
```

Имена классов и пакетов совпадают с EE10-вариантом; в коде используются только `javax.servlet.*`. Конфигурация фильтров (например, `SuperflyLogoutFilter`, `AbstractSessionTouchingFilter`) остаётся той же.

### Опциональные возможности (superfly-client-opt)

Если используется **superfly-client-opt** (например, `XmlActionDescriptionCollector`, `ScanningActionDescriptionCollector`, Spring `HttpClientFactoryBean`):

- Коллекторы действий перенесены в **superfly-client-core**; при зависимости от **superfly-client-opt** они подтягиваются через **superfly-client-core**.
- Для проектов только на EE8 подключайте `superfly-client-ee8` и при необходимости `superfly-client-opt` (он тянет `superfly-client-ee10`; при конфликте javax/jakarta используйте только `superfly-client-ee8` и `superfly-client-core` и подключайте коллекторы из core).

Рекомендация: для EE8-приложений подключать `superfly-client-ee8` и при необходимости `superfly-client-core`; `superfly-client-opt` оставить для проектов на Jakarta, если нужны фабрики и конфигурации под Spring.

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

Для приложений на Jakarta EE 10 используются web-компоненты из модуля `superfly-spring-security`, завязанные на `jakarta.servlet.*` и зависящие от core:

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

Они ожидают стек **Spring 6 / Spring Security 6 + Jakarta Servlet API**.

Рекомендуемая зависимость:

```xml
<dependency>
    <groupId>com.payneteasy.superfly</groupId>
    <artifactId>superfly-spring-security</artifactId>
    <version>${superfly.version}</version>
</dependency>
```

### EE8 / javax

Для Java EE 8 приложений (как Paynet) теперь доступен отдельный экспортный модуль web-адаптера под `javax.servlet.*`, поверх общего core:

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

## Итог

- **Клиент:** для EE10 используйте `superfly-client-ee10` или фасад `superfly-client`; для EE8 — `superfly-client-ee8`.
- **Wicket:** для Wicket 10 (Jakarta) — `superfly-wicket`; для Wicket 8 (EE8) — `superfly-wicket-ee8`.
- **Spring Security:**
  - общий core берётся из `superfly-spring-security-core`;
  - для EE10 можно использовать готовые web-компоненты на `jakarta.servlet.*` из `superfly-spring-security`;
  - для EE8 — использовать общий core и готовый web-адаптер `superfly-spring-security-ee8` на `javax.servlet.*` и Spring Security 5.8.x.

## See Also

- [Руководство по интеграции](integration-guide.md) — подключение к клиентскому приложению
- [Конфигурация](configuration.md) — настройка сервера Superfly
