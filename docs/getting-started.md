[Back to README](../README.md) · [Конфигурация →](configuration.md)

# Установка и запуск

## Требования

| Компонент | Версия |
|-----------|--------|
| Java | 21+ |
| Maven | 3.9+ (или `./mvnw`) |
| MySQL | **5.7** |
| Docker | любая (для базы и образа) |
| Git | любая |

> **Только MySQL 5.7.** На 8.0 схема не устанавливается: `groups` там стало зарезервированным словом.

---

## Быстрый старт (локальная разработка)

```bash
git clone https://github.com/payneteasy/superfly.git
cd superfly
./mvnw -DskipTests package

./dev-env.sh up      # MySQL 5.7, схема и хранимые процедуры
./dev-env.sh app     # приложение на http://localhost:8085/superfly/
```

Откройте `http://localhost:8085/superfly/`, логин `admin`, пароль `123admin123`.

`dev-env.sh` публикует MySQL только на `127.0.0.1:3344`. Остальные команды (`seed`, `sql`, `down`) и ограничения
описаны в [README](../README.md#локальная-разработка). Параметры подключения dev-контура лежат в
`superfly-web/src/main/webapp/WEB-INF/jetty-web.xml`; этот файл не попадает в WAR.

---

## Обновление существующей базы

Миграции лежат в `superfly-sql/mi/R<version>/`. Скрипты читают переменные окружения:

| Переменная | По умолчанию | Описание |
|------------|-------------|---------|
| `SSO_DB_HOST` | `localhost` | Хост БД |
| `SSO_DB_PORT` | `3344` | Порт |
| `SSO_DB_ROOT` | `root` | Root-пользователь |
| `SSO_DB_ROOT_PASSWORD` | dev-значение | Root-пароль (для не-dev базы задайте свой) |
| `SSO_DB_USERNAME` | `sso` | Пользователь приложения |
| `SSO_DB_PASSWORD` | dev-значение | Пароль приложения (для не-dev базы задайте свой) |
| `SSO_DB_DATABASE` | `sso` | Имя базы |

```bash
cd superfly-sql/mi
version_from=R1.7.4 bash all_mi.sh     # с какой версии применять; по умолчанию R1.0.0
cd ../src && ./all-proc.sh             # хранимые процедуры ставятся отдельно от WAR
```

Миграции прерываются на первой ошибке. Что делать при обновлении с конкретных версий — в
[заметках об обновлении](migration-client-ee8-ee10.md#обновление). Версию задеплоенного приложения отдаёт
`/management/version.txt` без авторизации.

---

## Docker-образ

Образ собирается из `Dockerfile` (стадия `production`): WAR на Jetty 12 (ee10), JRE 21, непривилегированный пользователь.
Схему БД образ не ставит — примените миграции и процедуры, как описано выше.

```bash
docker build --target production -t superfly-app .

docker run -d -p 8080:8080 \
  -e DB_HOST=db.example.com -e DB_PORT=3306 -e DB_NAME=sso \
  -e DB_USER=sso -e DB_PASSWORD -e DB_TIMEZONE=UTC \
  superfly-app
```

Параметры `DB_*` читаются из окружения (`docker/jetty/ROOT.xml`), порт Jetty — `JETTY_PORT` (по умолчанию 8080).
`-e DB_PASSWORD` без значения берёт пароль из окружения хоста, чтобы он не попадал в историю команд.

`compose.yml` поднимает приложение и MySQL 5.7 (`.env` создаётся из `.env.example`; `DB_PASSWORD` и
`DB_ROOT_PASSWORD` обязательны). `compose.production.yml` добавляет hardening (`read_only`, `cap_drop`, лимиты ресурсов):

```bash
docker compose -f compose.yml -f compose.production.yml up -d
```

`compose.override.yml` подхватывается автоматически и публикует MySQL на `127.0.0.1`.

---

## Устранение неисправностей

| Симптом | Причина | Решение |
|---------|---------|---------|
| `Connection refused` на 3344 | База не запущена | `./dev-env.sh up` |
| `Table 'sso.users' doesn't exist` | Миграции не накатаны | `./dev-env.sh up` или `all_mi.sh` |
| Схема не ставится, ошибка на `groups` | MySQL 8.0 | Использовать MySQL 5.7 |
| `NullPointerException` при старте | Нет JNDI datasource | Проверить `jetty-web.xml` (dev) или `DB_*` (Docker) |

---

## Следующие шаги

- [Конфигурация](configuration.md) — база данных, политики, CSP
- [Руководство по интеграции](integration-guide.md) — подключение клиентских приложений
- [Миграция EE8/EE10](migration-client-ee8-ee10.md) — javax/jakarta split, breaking changes
