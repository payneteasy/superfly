# Анализ superfly-remote-api как чистого контракта (EE8/EE10)

## Результат

Модуль `superfly-remote-api` **пригоден** в качестве общего контракта для EE8 и EE10 без изменений зависимостей.

## Зависимости (pom.xml)

- `jackson-dataformat-xml`, `lombok`, `http-client-api`, `http-client-impl`, `gson`, `slf4j-api`, `logback-classic`
- **Нет** `javax.servlet`, `javax.ws.rs`, `jakarta.*` в POM.

## Импорты в коде

- Во всех исходниках найден **один** импорт из `javax`/`jakarta`: `javax.annotation.Nullable` в `SSOHttpServiceApiClient.java` (параметр `subsystemToken`).
- Аннотация удалена, параметр по-прежнему может быть `null` (документировано в JavaDoc). Модуль не зависит от JSR-305 / Jakarta Annotations.

## Состав модуля (роль контракта)

- **Интерфейсы и DTO**: `SSOService`, запросы (`*Request`), ответы (`SSOUser`, `UserDescription`, `UserStatus` и т.д.), исключения (`SsoException` и подклассы).
- **Сериализация**: `ApiSerializer`, `ApiSerializationManager`, `ExceptionWrapper`, `ExceptionSerializationHelper` — общая логика сериализации запросов/ответов (Jackson/Gson), без привязки к Servlet/JAX-RS.
- **Реализация HTTP-клиента**: `SSOHttpServiceApiClient` — реализация `SSOService` через HTTP; зависит только от `http-client-api`/`impl`, без javax/jakarta.

Имя модуля оставляем `superfly-remote-api` (без переименования в `superfly-remote-contract`), ответственность зафиксирована: контракт и клиентская реализация по HTTP, без веб-API.
