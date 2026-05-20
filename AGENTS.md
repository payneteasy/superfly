# AGENTS.md

> Этот файл — структурная карта проекта для AI-агентов и новых разработчиков.
> Обновлять при значительных изменениях структуры проекта.

## Обзор проекта

Superfly — централизованный SSO-сервер для управления пользователями, ролями и правами
доступа в нескольких системах одновременно. Поддерживает Spring Security, Wicket-UI и
EE8/EE10 (javax/jakarta servlet) клиентские библиотеки.

## Технологический стек

- **Язык:** Java 21
- **Фреймворк:** Spring Framework 6.2.18 + Spring Security 6.4.11
- **Веб-UI:** Apache Wicket 10.6.0
- **База данных:** MySQL (stored procedures, jdbc-proc)
- **Logging:** SLF4J 2.0 + Logback
- **Сборка:** Maven (multi-module, 20 модулей)
- **CI:** GitHub Actions

## Структура проекта

```
superfly/
├── pom.xml                        # корневой POM, версии всех зависимостей
├── AGENTS.md                      # этот файл
├── README.md                      # описание проекта
├── mvnw / mvnw.cmd                # Maven wrapper
├── .github/workflows/             # GitHub Actions CI
│   └── maven.yml
│
├── superfly-remote-api/           # REST/RPC API интерфейсы для клиентов
├── superfly-spi/                  # SPI-интерфейсы (HOTP, соль и др.)
├── superfly-spi-support/          # Реализации SPI
├── superfly-service/              # Бизнес-логика: пользователи, роли, права
│   └── src/main/java/com/payneteasy/superfly/
│       ├── dao/                   # DAO через jdbc-proc (stored procedures)
│       ├── service/               # Spring @Service — бизнес-операции
│       ├── model/                 # Доменные модели и UI-модели (UI* prefix)
│       ├── password/              # Хэширование паролей
│       ├── hotp/                  # HOTP/2FA
│       ├── email/                 # Email-уведомления
│       └── spring/                # Spring конфигурация, условия
│
├── superfly-web/                  # Веб-приложение (Wicket + Spring MVC, Jetty)
│   └── src/main/java/com/payneteasy/superfly/web/wicket/
│       ├── page/                  # Wicket страницы (users, roles, actions...)
│       ├── component/             # Переиспользуемые Wicket компоненты
│       └── security/              # Security-интеграция для веб-слоя
│
├── superfly-client-core/          # Клиент без servlet зависимости
├── superfly-client-ee8/           # Клиент для javax.servlet (EE8)
├── superfly-client-ee10/          # Клиент для jakarta.servlet (EE10)
├── superfly-client/               # Клиент (алиас/агрегат)
├── superfly-client-web-security/  # Web security клиент
├── superfly-client-opt/           # Клиент с опциональными фичами
│
├── superfly-spring-security-core/ # Spring Security integration (core, без servlet)
├── superfly-spring-security-ee8/  # Spring Security для EE8 (javax.servlet)
├── superfly-spring-security/      # Spring Security для EE10 (jakarta.servlet)
│
├── superfly-wicket/               # Wicket компоненты (EE10)
├── superfly-wicket-ee8/           # Wicket компоненты (EE8)
│
├── superfly-common/               # Shared utilities
├── superfly-crypto/               # Шифрование, хэширование
├── superfly-httpclient-ssl/       # HTTP client с SSL mutual auth
│
├── superfly-sql/                  # SQL-скрипты и миграции
│   ├── src/                       # Stored procedures
│   └── mi/                        # Миграции: R<version>/<desc>.sql
│
├── superfly-integration-test/     # Интеграционные тесты (против реальной MySQL)
└── superfly-demo/                 # Демо-приложение
```

## Ключевые точки входа

| Файл | Назначение |
|------|-----------|
| `pom.xml` | Корневой POM, версии зависимостей (`spring.version`, `wicket.version` и др.) |
| `superfly-web/src/main/webapp/WEB-INF/jetty-web.xml` | Конфигурация Jetty |
| `superfly-web/src/main/resources/jetty-env.conf` | Переменные окружения Jetty |
| `superfly-service/src/main/java/.../spring/` | Spring Application Context конфигурации |
| `superfly-sql/mi/` | SQL-миграции базы данных |
| `.github/workflows/maven.yml` | CI pipeline |

## Документация

| Документ | Путь | Описание |
|----------|------|---------|
| README | README.md | Landing page проекта |
| Установка и запуск | docs/getting-started.md | Требования, сборка, первый запуск |
| Конфигурация | docs/configuration.md | БД, Jetty, Spring конфиги |
| Доменная модель | docs/domain-model.md | Сущности, связи, перечисления |
| API Reference | docs/api.md | RPC и REST endpoints |
| Руководство по интеграции | docs/integration-guide.md | Подключение клиентских приложений |
| Миграция EE8 / EE10 | docs/migration-client-ee8-ee10.md | Переход на раздельные модули |
| Выпуск релиза | docs/releasing.md | Публикация в Maven Central |

## AI Context файлы

| Файл | Назначение |
|------|-----------|
| AGENTS.md | Карта проекта для AI-агентов |
| .ai-factory/DESCRIPTION.md | Детальное описание стека и модулей |
| .ai-factory/ARCHITECTURE.md | Clean Architecture: слои, правила зависимостей, примеры кода |
| .ai-factory/rules/base.md | Кодовые конвенции проекта |
| .ai-factory/config.yaml | Настройки AI Factory |

## Docker

| Файл | Назначение |
|------|-----------|
| `Dockerfile` | Multi-stage: builder → production (Jetty 12) + development (mvn jetty:run) |
| `compose.yml` | База: app + MySQL 8.0 |
| `compose.override.yml` | Dev-оверрайды: source mount, Maven hot-reload |
| `compose.production.yml` | Hardened production: read_only, cap_drop, resource limits |
| `docker/jetty/ROOT.xml` | Jetty context descriptor с JNDI datasource (env vars) |
| `docker/jetty/entrypoint.sh` | Передаёт DB-параметры как Java system properties |
| `.env.example` | Шаблон переменных окружения |

## Правила для агентов

- Команды git выполнять по одной — не объединять в цепочку:
  - Неправильно: `git checkout master && git pull`
  - Правильно: сначала `git checkout master`, затем `git pull origin master`
- Не мешать javax.servlet и jakarta.servlet в одном модуле — см. EE8/EE10 разделение
- Интеграционные тесты требуют реальный MySQL — не заменять на H2/mock
- Слой данных — только stored procedures через jdbc-proc, без JPA/Hibernate
