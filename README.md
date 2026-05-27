[![Build Status](https://github.com/payneteasy/superfly/actions/workflows/maven.yml/badge.svg)](https://github.com/payneteasy/superfly/actions/workflows/maven.yml)

# Superfly

> Централизованный SSO-сервер для управления пользователями, ролями и правами доступа.

Superfly позволяет зарегистрировать пользователей один раз и управлять их правами доступа
из единой точки для всех подключённых систем. Поддерживает Spring Security, Apache Wicket
и оба поколения Servlet API (Java EE 8 / Jakarta EE 10).

## Быстрый старт

```bash
# Клонировать и собрать
git clone https://github.com/payneteasy/superfly.git
cd superfly
./mvnw -DskipTests package

# Настроить БД (MySQL)
# Отредактировать superfly-web/src/main/webapp/WEB-INF/jetty-web.xml
# Запустить сервер (Jetty)
./mvnw -pl superfly-web jetty:run
```

После запуска веб-интерфейс доступен по адресу `http://localhost:8080`.

## Ключевые возможности

- **Единый вход (SSO)** — пользователь вводит пароль один раз для всех систем
- **Centralized RBAC** — пользователи, роли и права в одном месте
- **Spring Security** — готовая интеграция для Java EE 8 и Jakarta EE 10
- **Wicket UI** — веб-интерфейс администратора
- **HOTP / OTP** — двухфакторная аутентификация
- **No-redirect режим** — прозрачная аутентификация без редиректов
- **REST API** — не привязан к Spring Security

## Подключение к клиентскому приложению

```xml
<!-- Jakarta EE 10 (Spring 6) -->
<dependency>
    <groupId>com.payneteasy.superfly</groupId>
    <artifactId>superfly-spring-security</artifactId>
    <version>2.0-3-SNAPSHOT</version>
</dependency>

<!-- Java EE 8 (javax.servlet) -->
<dependency>
    <groupId>com.payneteasy.superfly</groupId>
    <artifactId>superfly-spring-security-ee8</artifactId>
    <version>2.0-3-SNAPSHOT</version>
</dependency>

<!-- Optional: SSL/HTTP client factory beans (EE8- and EE10-compatible, no Servlet API) -->
<dependency>
    <groupId>com.payneteasy.superfly</groupId>
    <artifactId>superfly-client-opt</artifactId>
    <version>2.0-3-SNAPSHOT</version>
</dependency>
```

---

## Документация

| Руководство | Описание |
|-------------|----------|
| [Установка и запуск](docs/getting-started.md) | Требования, сборка, первый запуск |
| [Конфигурация](docs/configuration.md) | База данных, Jetty, параметры запуска |
| [Доменная модель](docs/domain-model.md) | Сущности, связи, перечисления |
| [API Reference](docs/api.md) | RPC и REST endpoints, форматы запросов |
| [Руководство по интеграции](docs/integration-guide.md) | Подключение к клиентскому приложению |
| [SSO HTTP Client](docs/sso-http-client.md) | Клиентский `SSOHttpServiceApiClient`, per-endpoint timeouts |
| [SSL / mTLS](docs/ssl-mtls.md) | TLS-соединение, hostname verification, кастомный CA |
| [Миграция EE8 / EE10](docs/migration-client-ee8-ee10.md) | Переход на раздельные модули |
| [Выпуск релиза](docs/releasing.md) | Публикация в Maven Central |

## Лицензия

[Apache License 2.0](LICENSE)
