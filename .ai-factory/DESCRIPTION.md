# Superfly — централизованный SSO-сервер

## Обзор

Superfly — это централизованный сервер аутентификации и авторизации (SSO).
Позволяет управлять пользователями, ролями и правами доступа из единой точки
для всех подключённых систем. Поддерживает redirect-based и no-redirect SSO,
интеграцию со Spring Security, а также внешние системы (Jira и другие).

## Ключевые функции

- Централизованное управление пользователями, ролями, правами
- Redirect-based и no-redirect SSO
- Интеграция со Spring Security (EE8 / EE10)
- Веб-интерфейс администратора на Apache Wicket
- REST API для удалённого взаимодействия
- HOTP/OTP поддержка для двухфакторной аутентификации
- Email-уведомления (SMTP)
- Криптографический модуль для хэширования паролей
- Поддержка JIRA-интеграции

## Технологический стек

- **Язык:** Java 21
- **Сборка:** Maven (multi-module, 20 модулей)
- **Фреймворк:** Spring Framework 6.2.18
- **Безопасность:** Spring Security 6.4.11
- **Веб-UI:** Apache Wicket 10.6.0 + Spring MVC (Jetty)
- **База данных:** MySQL (хранимые процедуры через jdbc-proc)
- **Logging:** SLF4J 2.0 + Logback
- **CI:** GitHub Actions
- **Servlet API:** javax.servlet 4 (EE8) и jakarta.servlet 6 (EE10) — разделены по модулям

## Модульная структура

| Модуль | Назначение |
|--------|-----------|
| `superfly-remote-api` | REST/RPC API для клиентов |
| `superfly-spi` | SPI-интерфейсы для расширяемости |
| `superfly-service` | Бизнес-логика: пользователи, роли, права |
| `superfly-web` | Веб-приложение (Wicket + Spring MVC) |
| `superfly-client-core` | Клиентская библиотека (без Servlet зависимости) |
| `superfly-client-ee8` | Клиент для javax.servlet (EE8) |
| `superfly-client-ee10` | Клиент для jakarta.servlet (EE10) |
| `superfly-spring-security-core` | Spring Security integration (core) |
| `superfly-spring-security-ee8` | Spring Security для EE8 |
| `superfly-spring-security` | Spring Security для EE10 |
| `superfly-wicket` | Wicket компоненты (EE10) |
| `superfly-wicket-ee8` | Wicket компоненты (EE8) |
| `superfly-crypto` | Шифрование и хэширование паролей |
| `superfly-common` | Shared utilities |
| `superfly-httpclient-ssl` | HTTP client с SSL |
| `superfly-httpclient-hc5` | Apache HC5 транспорт (connection pool, AutoCloseable) |
| `superfly-sql` | SQL-скрипты и миграции |
| `superfly-integration-test` | Интеграционные тесты (против реальной БД) |

## Архитектурные заметки

- Слой данных — исключительно MySQL stored procedures через jdbc-proc (не ORM)
- DAO-классы используют `@JdbcProcedure` аннотации jdbc-proc
- Разделение EE8/EE10 позволяет подключать библиотеку к приложениям как на javax, так и на jakarta
- Веб-сервер — Jetty (embedded), конфигурация через `jetty-env.conf`
- Транзакции управляются через Spring `@Transactional`
- `superfly-client-opt` зависит от `superfly-client-core` (не `superfly-client`):
  дубликаты `ScanningActionDescriptionCollector` / `XmlActionDescriptionCollector`
  перемещены в `superfly-client-core`, из `superfly-client-opt` удалены

## Нефункциональные требования

- **Logging:** SLF4J + Logback, JSON-формат для production (strilog-json-encoder)
- **Безопасность:** хэширование паролей, HOTP, SSL mutual auth
- **Тесты:** JUnit 4, интеграционные тесты против реальной MySQL (не mock)

## Архитектура

Подробные архитектурные правила, примеры и анти-паттерны — в `.ai-factory/ARCHITECTURE.md`.
**Паттерн:** Clean Architecture (реализован через Maven-модули)
