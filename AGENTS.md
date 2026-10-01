# AGENTS.md

> Этот файл — структурная карта проекта для AI-агентов и новых разработчиков.
> Обновлять при значительных изменениях структуры проекта.

## Обзор проекта

Superfly — централизованный SSO-сервер для управления пользователями, ролями и правами
доступа в нескольких системах одновременно. Поддерживает Spring Security, Wicket-UI и
EE8/EE10 (javax/jakarta servlet) клиентские библиотеки.

## Технологический стек

- **Язык:** Java 21 (включая EE8-модули: `release` не понижается)
- **Фреймворк:** Spring Framework 6.2.19 + Spring Security 6.5.11 (EE8-модули: Spring 5.3.39 / Spring Security 5.8.16, provided)
- **Веб-UI:** Apache Wicket 10.9.1 (superfly-wicket-ee8 — Wicket 8.18.0, provided)
- **База данных:** MySQL (stored procedures, jdbc-proc)
- **Logging:** SLF4J 2.0 + Logback
- **Сборка:** Maven (multi-module, 19 модулей)
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
├── superfly-client-web-security/  # Web security клиент
├── superfly-client-opt/           # Клиент с опциональными фичами
│
├── superfly-spring-security-core/ # Spring Security integration (core, без servlet)
├── superfly-spring-security-ee8/  # Spring Security для EE8 (javax.servlet)
├── superfly-spring-security-ee10/ # Spring Security для EE10 (jakarta.servlet); зависит от -core
│
├── superfly-wicket/               # Wicket компоненты (EE10)
├── superfly-wicket-ee8/           # Wicket компоненты (EE8)
│
├── superfly-common/               # Shared utilities
├── superfly-crypto/               # Шифрование, хэширование
├── superfly-httpclient-hc5/       # HTTP client (Apache HC5) + SSL/mTLS хелперы
│
├── superfly-sql/                  # SQL-скрипты и миграции
│   ├── src/                       # Stored procedures
│   └── mi/                        # Миграции: R<version>/<desc>.sql
│
└── superfly-integration-test/     # Интеграционные тесты (против реальной MySQL)
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
| `Dockerfile` | Multi-stage: builder → production (Jetty 12 ee10, JRE 21); стадии development нет |
| `compose.yml` | База: app + MySQL 5.7 |
| `compose.override.yml` | Dev-оверрайд: публикует MySQL на 127.0.0.1 (приложение — `./dev-env.sh app`) |
| `compose.production.yml` | Hardened production: read_only, cap_drop, resource limits |
| `docker/jetty/ROOT.xml` | Jetty context descriptor с JNDI datasource (env vars) |
| `docker/jetty/entrypoint.sh` | Передаёт в Jetty только порт; `DB_*` читаются из окружения в `ROOT.xml` |
| `.env.example` | Шаблон переменных окружения |

## Правила для агентов

- Команды git выполнять по одной — не объединять в цепочку:
  - Неправильно: `git checkout master && git pull`
  - Правильно: сначала `git checkout master`, затем `git pull origin master`
- Не мешать javax.servlet и jakarta.servlet в одном модуле — см. EE8/EE10 разделение
- Интеграционные тесты требуют реальный MySQL — не заменять на H2/mock
- Слой данных — только stored procedures через jdbc-proc, без JPA/Hibernate
